(ns migrate
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.measurements :as measurements]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.time :as time]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [kixi.hecuba.data.parents :as parents]
            [kixi.hecuba.data.entities.search :as search]
            [clojure.edn :as edn]
            [cheshire.core :as json]
            [kixi.hecuba.data.entities :as entities]
            [kixi.hecuba.data.projects :as projects]
            [kixi.hecuba.data.profiles :as profiles-data]
            [kixi.hecuba.api.profiles :as profiles-api]
            [kixi.hecuba.api.profiles.schema :as profiles-schema]
            [clojure.java.io :as io]))

(defn do-map
  "Map with side effects."
  [f & lists]
  (apply mapv f lists) nil)

(defn convert-metadata
  "Reads stringified metadata into a clojure map."
  [_ m]
  (let [metadata (:metadata m)]
    (if metadata
      (clojure.walk/stringify-keys (read-string metadata))
      nil)))

(def processed-file "/tmp/processed_sensors.txt")

(defn file-exists? [filename]
  (.exists (clojure.java.io/as-file filename)))

(defn all-measurements
  "Returns a sequence of all the measurements for a sensor
  matching (type,device_id). The sequence pages to the database in the
  background. The page size is a clj-time Period representing a range
  in the timestamp column. page size defaults to (clj-time/hours 1)"
  ([store sensor_id & [opts]]
   (let [{:keys [type device_id]} sensor_id
         {:keys [page start end] :or {page (t/hours 1)}} opts
         [start end] (measurements/resolve-start-end store type device_id start end)]
     (when (and start end)
       (let  [next-start (t/plus start page)]
         (db/with-session [session (:hecuba-session store)]
           (lazy-cat (db/execute session
                                 (hayt/select :measurements
                                              (hayt/where [[= :device_id device_id]
                                                           [= :type type]
                                                           [= :month (time/get-month-partition-key start)]
                                                           [>= :timestamp start]
                                                           [< :timestamp next-start]]))
                                 nil)
                     (when (t/before? next-start end)
                       (all-measurements store sensor_id (merge opts {:start next-start :end end}))))))))))

(defn migrate-reading-metadata
  "Works on a lazy sequence of all measurements for all sensors in the database and
  populates (new) reading_metadata with data coming from (old) metadata."
  [{:keys [store]}]
  (log/info "Migrating reading metadata.")
  (db/with-session [session (:hecuba-session store)]
    (let [all-sensors (db/execute session (hayt/select :sensors))
          processed-sensors (if (file-exists? processed-file)
                              (map clojure.edn/read-string (clojure.string/split-lines (slurp processed-file)))
                              [])
          sensors (remove (set processed-sensors) all-sensors)]
      (doseq [s sensors]
        (let [measurements (all-measurements store s)
              measurements-with-metadata (->> measurements
                                              (map #(update-in % [:reading_metadata] convert-metadata %))
                                              (map #(dissoc % :metadata)))]
          (when measurements-with-metadata
            (db/with-session [session (:hecuba-session store)]
              (let [{:keys [min-date max-date]} (measurements/insert-measurements store s 100 measurements-with-metadata)]
                (sensors/update-sensor-metadata session s min-date max-date)))))
        (spit "/tmp/processed_sensors.txt" (str s "\n") :append true))))
  (log/info "Finished migrating reading metadata."))

(defn fill-sensor-bounds
  "Gets very first measurement row for each sensor and updates lower_ts in sensor_metadata with its timestamp.
  Upper_ts is populated with current date."
  [{:keys [store]}]
  (log/info "Populating sensor bounds.")
  (db/with-session [session (:hecuba-session store)]
    (let [sensors (db/execute session (hayt/select :sensors))]
      (doseq [s sensors]
        (let [where [[= :device_id (:device_id s)]
                     [= :type (:type s)]]
              first-ts (:timestamp (first (db/execute session
                                                      (hayt/select :measurements
                                                                   (hayt/where where)
                                                                   (hayt/limit 1)))))
              last-ts  (:timestamp (first (db/execute session
                                                      (hayt/select :measurements
                                                                   (hayt/where where)
                                                                   (hayt/order-by [:type :desc])
                                                                   (hayt/limit 1)))))]
          (db/execute session (hayt/update :sensor_metadata
                                           (hayt/set-columns :upper_ts last-ts
                                                             :lower_ts first-ts)
                                           (hayt/where where)))))))
  (log/info "Finished populating sensor bounds."))

(defn migrate-types
  "Migrates sensor types from sensor_id column (old type column) to
  type column. Takes < 2 minutes"
  [{:keys [store]}]
  (db/with-session [session (:hecuba-session store)]
    (let [sensors (db/execute session (hayt/select :sensors))]
      (doseq [s sensors]
        (let [where [[= :device_id (:device_id s)]
                     [= :sensor_id (:sensor_id s)]]
              typ   (:sensor_id s)]
          (db/execute session (hayt/update :sensors
                                           (hayt/set-columns :type typ)
                                           (hayt/where where)))))
      (search/refresh-search (:hecuba-session store) (:search-session store)))))

(defn reset-broken-location
  "Some devices have a strigified edn in location (should be json). This deletes it."
  [{:keys [store]}]
  (db/with-session [session (:hecuba-session store)]
    (let [devices (db/execute session (hayt/select :devices))]
      (doseq [d devices]
        (let [where    [[= :id (:id d)]]
              location (:location d)]
          (when (seq location)
            (try
              (json/decode location) ;; don't have to do anything when location is stringified json
              (catch Throwable t
                (log/errorf "Could not parse location %s for device %s. Attempting to parse as edn."
                            location (:id d))
                (try
                  (let [location-edn (edn/read-string location)
                        location-json (json/encode location-edn)]
                    (db/execute session (hayt/update :devices
                                                     (hayt/set-columns :location location-json)
                                                     (hayt/where where))))
                  (catch Throwable t
                    (log/errorf "Could not parse location %s for device %s as edn."
                                location (:id d)))))))))
      (search/refresh-search (:hecuba-session store) (:search-session store)))))

(defn migrate-doc [doc]
  (let [doc-edn (json/decode doc true)
        privacy (:privacy doc-edn)]
    (if (seq privacy)
      (-> doc-edn
          (assoc :public? (if (= privacy "Private") false true))
          (assoc :path (str "media-resources/" (:token doc-edn) "/documents/" (:file_name doc-edn))))
      doc-edn)))

;; Deletes old documents first and then adds new ones. This is a workaround for the issue
;; where setting documents column to a new list throws an error  "extraneous input 'documents' expecting K_WHERE" :
;; UPDATE entities SET documents = ['{"token":"683f2d71c33af03e6d77f8bac1bfdff97c34076a","privacy":"Public","file_size":18025,"id":1557,"name":"SAP Summary","attachable_type":"Property","public?":true,"content_type":"application/pdf","attachable_id":53,"file_name"}'] documents WHERE id = '0a737cfd3225b484fb94b5040701f9345d05ef40';
(defn migrate-documents
  "Migrate documents from old format of storage to the new one."
  [{:keys [store]}]
  (db/with-session [session (:hecuba-session store)]
    (let [entities (db/execute session (hayt/select :entities))]
      (doseq [e entities]
        (let [id    (:id e)
              where [[= :id id]]]
          (when-let [documents (seq (:documents e))]
            (let [updated-docs (mapv migrate-doc documents)]
              ;; Delete all existing documents
              (entities/update session id {:documents nil :user_id "support@mastodonc.com"})
              ;; Insert new documents
              (doseq [doc updated-docs]
                (entities/add-document session id doc)))))))
    (search/refresh-search (:hecuba-session store) (:search-session store))))

(defn doc-report
  "Produce a report of all docs in the database: their old and new path"
  [{:keys [store]}]
  (db/with-session [session (:hecuba-session store)]
    (let [entities (entities/get-all session)
          documents (apply concat (keep (fn [entity]
                                          (when-let [documents (seq (:documents entity))]
                                            (let [id (:entity_id entity)]
                                              (map (fn [doc]
                                                     {:id id
                                                      :old-path (:file_name doc)
                                                      :new-path (str "media-resources/" id "/documents/" (:file_name doc))}) documents))))
                                        entities))]
      (spit "/Users/annapawlicka/report.txt" (prn-str documents)))))

(def old->new-project-ids {"17" "ca9c6ce23a3e9474d5b000da988dc7103161d55d"  "21" "d573bc7b802df9674ac59059af50823c638094f0"
                           "25" "ec6f8ac8bca57de217a4aace80871a21da2adeee"  "33" "5aa650f40d2f60fbe95e77af88151d988585920f"
                           "37" "c01f3a6a30fd0a9bc7c41e3297bae5f8c592209a"  "41" "c73ea0bab2ca5b9ecc93eb5073d23fa595eb011b"
                           "45" "623967950cf70f80da46303bc10fe3b0083b53f4"  "53" "d7334379c54a6ffe46186b115347155304b757e5"
                           "77" "25fdad74c3736e60b0257ac3f06e2726577dc1f0"  "85" "15a62e5a80628dfb4e6d049076eec6671e5fee0a"
                           "109" "6c558451253a8b0d1f5a36d3320c0bebcf1f0cc0" "113" "f9ae76d7b8a5b5fbb6d61b495826f90e7b908ad2"
                           "117" "35858df1507f80e7ecf849e2dd66563e1ca3f63b" "121" "ba06eaa5270240a65be3bd5be8e3f38ef29151c9"
                           "133" "0cb3e3c40cba48ecc0758c1d75086ce12fb3ced4" "137" "04daf64f4d53ffffac74bad88e537e49d055afac"
                           "141" "81942f37074127ca02d4d9292d5bf19ea06a86e5" "145" "e680e8381a9553276dbb31e658d0ee478f97d6bf"
                           "149" "d27ef6e830b00a204facda3011baa827fc97b579" "153" "14b17f751460cfb30ee9ba52a4e27220c3a6d659"
                           "157" "6e4f95bf5a8afcc710cde5ed8d7b441957d877f2" "165" "37e7e4deedba789a50c246a329475eedb79cab83"
                           "177" "04aa168bb70c63b60f5a4b746ba13f8dc1782272" "181" "53031742de5a558e55ab141aa205815bce67f8a3"
                           "201" "69044bf7984c08c677939138ef8c260e44b27b8d" "205" "528defcd773aa3b1f0f64eae46e54e7e4b33f1f2"
                           "209" "543b08f8fa51022e6bb0837e30b233eeaaaaaeac" "213" "f3f25a91875289ebddafad073825ae92b02fccfe"
                           "217" "d24182b1f26e84521569a11687aac383fb036bbe" "221" "713d0eb201a577397d567aec47fa64a5c9b08d3c"
                           "225" "201c507debd5191f194c15972430d50ce13e92c3" "229" "cde1c503da846e1fa0b4f46647eb83a4791d4938"
                           "237" "032b8c0ae96805375c1c7779ab01639ce3ad6156" "241" "fdfe5be0778f557f8cdc085694a5aa2706b68ddd"
                           "245" "bafc72262e93749cb0bf540df218756c244cf54a" "249" "b70e121a61ab341b80c57e55552102ff893c3dd1"
                           "253" "d24d65baa2d25658df844e372c6c73992dc98014" "269" "5f3f56b05dcd1a885997d92d82d3070103cc0964"
                           "273" "703507f7787ab414993e640117cced8651c57368" "281" "dbb77b7059bee93a7b8105a5057b310a8804fe76"
                           "289" "569f443eeb6a432ec68cda746ddfa7470f0b4cbe" "301" "6a9f9b929ccf31037abe3d82ab6b4093683dd9a5"
                           "309" "14faff90ab1ea584c9b117a2dc2544b82c1175e8" "313" "0e747ea5d02ccc6b64b6b2616a013ea4a782125f"
                           "317" "f3ebcec83ac4c78f7e6fd934466885e5d9612a2b" "321" "4e9fa7c712d8d5f9be8674cb940f402b894ad418"
                           "325" "50933deefef6c3639a0a8bb3f32f15a170f501ec" "333" "cc7c384c0059d41c6217f5a9620f538f0d383f0a"
                           "337" "08e7c9dceb6d12f5afc2d68b3c508d3dd3ad1739" "345" "4e6ec003585906009f904a120849d92117efccf3"
                           "349" "c5034b9d2f958a4d1987e8d2761e8d349e9778d0" "353" "444c492a75cb93028e7ca24380ff01e13ad9f782"
                           "369" "53c7715703c0774645d23031956366d955f73faa" "463" "285e4efeb1de05726e2a6f2cafe1628fd7d88d32"
                           "464" "1b9f9db263d451d8bb4bd7222e1b6d3f68d17051"})

(def old-project-ids #{"17" "21" "25" "33" "37" "41" "45" "53" "77" "85" "109" "113" "117" "121"
                       "133" "137" "141" "145" "149" "153" "157" "165" "177" "181" "201" "205"
                       "209" "213" "217" "221" "225" "229" "237" "241" "245" "249" "253" "269"
                       "273" "281" "289" "301" "309" "313" "317" "321" "325" "333" "337""345"
                       "349" "353" "369" "463" "464"})

(defn property_code-entity_id-lookup [entities]
  (into {} (map #(hash-map (:property_code %) (:entity_id %)) entities)))

(defn extract-profile-data [profile]
  (let [profile-data-keys (into #{} profiles-schema/profile-data-schema)]
    (-> (into {} (keep (fn [[k v]]
                         (when (and (contains? profile-data-keys k)
                                    (seq (str v)))
                           (hash-map k (str v)))) profile))
        (assoc :event_type (:category profile))
        (dissoc :id :property_id))))

(defn clean-and-insert-profiles [{:keys [store]} url]
  (db/with-session [session (:hecuba-session store)]
    (let [entities                (entities/get-all session)
          entity_id-lookup        (property_code-entity_id-lookup entities)
          entities-with-profiles  (with-open [rdr (io/reader url)]
                                    (doall
                                     (keep (fn [[k v]]
                                             (let [old-entity-id  (last (re-find #"\S+: \S+: (\S+)" k))
                                                   entity-data    (json/parse-string v true)
                                                   old-project-id (str (-> entity-data :project_id))
                                                   profiles       (:profiles entity-data)]
                                               ;; Ignore entities not in Retrofit for the Future and without profiles
                                               (when (and (contains? old-project-ids old-project-id)
                                                          (seq profiles))
                                                 (let [new-project-id (get old->new-project-ids old-project-id)
                                                       ;; try matching on property_code
                                                       property_code (:property_code entity-data)
                                                       new-entity-id (get entity_id-lookup property_code)]
                                                   (when new-entity-id ;; in case we didn't match all entities
                                                     (hash-map :entity_id    new-entity-id
                                                               :entity-data  (dissoc entity-data :profiles)
                                                               :profiles     profiles))))))
                                           (partition 2 (line-seq rdr)))))]
      (log/info "Number of entities to update: " (count entities-with-profiles))
      (doseq [entity entities-with-profiles] ;; 22 profiles
        (let [profiles-to-insert (map #(-> (hash-map :entity_id (:entity_id entity)
                                                     :profile_data (extract-profile-data %))
                                           (kixi.hecuba.storage.uuid/add-profile-id))
                                      (:profiles entity))]
          (doseq [profile profiles-to-insert]
            (log/info "Attempting to store profile with id" (:profile_id profile))
            (profiles-api/store-profile profile "support@mastodonc.com" store)))))))


;; R4F id: "c94a2f01d89708fb406fed83665ccb1c36e441a5"
(defn delete-r4f-profiles [{:keys [store]}]
  (db/with-session [session (:hecuba-session store)]
    (let [project-ids        (into #{} (map :project_id (projects/get-all session "c94a2f01d89708fb406fed83665ccb1c36e441a5")))
          all-entities       (entities/get-all session)
          r4f-entities       (filter #(contains? project-ids (:project_id %)) all-entities)
          entity-ids         (into #{} (map :entity_id r4f-entities))
          profiles-to-delete (->> (mapcat #(profiles-data/get-profiles % session) entity-ids)
                                  (map :profile_id))
          _(log/info "Number of profiles to delete: " (count profiles-to-delete))
          report             (reduce (fn [{:keys [deleted failed]} profile_id]
                                       (if-let [error (seq (profiles-data/delete session profile_id))]
                                         {:deleted deleted :failed (conj failed {:id profile_id :error error})}
                                         {:deleted (conj deleted profile_id) :failed failed}))
                                     {:deleted [] :failed []}
                                     profiles-to-delete)]

      (log/info "Report: " report)
      ;; Refresh ES
      (doseq [entity r4f-entities]
        (-> entity
            (search/searchable-entity session)
            (search/->elasticsearch (:search-session store)))))))
