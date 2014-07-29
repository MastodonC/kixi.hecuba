(ns kixi.hecuba.http_test
  (:require [clojure.test :refer :all]
            [kixi.amon-schema :as amon]
            [schema_gen.core :as sg]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [clj-time.core :as t]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.tools.logging  :as log]
            ))

(def ^:private poster-map (atom {:programme-name ""
                                 :project-name ""
                                 :entity-property-code ""
                                 :device-description ""}))

(def ^:private getter-map (atom {:programme-name ""
                                 :project-name ""
                                 :entity-property-code ""
                                 :device-description ""}))

(def ^:private programme-loc (atom ""))
(def ^:private project-loc (atom ""))
(def ^:private entity-loc (atom ""))
(def ^:private device-loc (atom ""))
(def ^:private measurement-loc (atom ""))
(def ^:private type (atom ""))

(def ^:private gen-date-time
  (gen/fmap (partial apply t/date-time)
            (gen/tuple (gen/choose 2012 2014)
                       (gen/choose 1 12)
                       (gen/choose 1 28)
                       (gen/choose 0 23)
                       (gen/choose 0 59)
                       (gen/choose 0 59))))

(defn- get-date []
  (str (subs (str (last (gen/sample gen-date-time))) 0 19) "Z"))

(defn- sample-Measurement []
  (let [sample (last (sg/generate-examples amon/BaseMeasurement))]
    {:measurements (conj [] (assoc-in sample [:timestamp] (get-date)))}))

;; --------------------
;; post data and then call get-data.
;; -------------------

(defn- post-programme []
  (reset! programme-loc 
          (get-in (:headers
                   (let [sample (last (sg/generate-examples amon/BaseProgramme))]
                     (reset! poster-map (assoc-in @poster-map [:programme-name] (:name sample)))
                     (client/post "http://127.0.0.1:8010/4/programmes/" {:accept :json :content-type :json :basic-auth ["support@mastodonc.com" "password"] :body (json/write-str sample)})))
                  ["Location"]))
  (log/info "Programmes: " @programme-loc))

(defn- post-project []
  (reset! project-loc 
          (get-in (:headers 
                   (let [sample (last (sg/generate-examples amon/BaseProject))]
                     (reset! poster-map (assoc-in @poster-map [:project-name] (:name sample)))
                     (client/post (apply str "http://127.0.0.1:8010" @programme-loc "/projects/") {:accept :json :content-type :json :basic-auth ["support@mastodonc.com" "password"] :body (json/write-str (assoc-in sample [:programme_id]  (clojure.string/replace @programme-loc  #"/4/programmes/(.*?)" "$1")))})))
                  ["Location"]))
  (log/info "Project: " @project-loc))

(defn- post-entity []
  (reset! entity-loc 
          (get-in (:headers
                   (let [sample (last (sg/generate-examples amon/BaseEntity))]
                      (reset! poster-map (assoc-in @poster-map [:entity-property-code] (:property_code sample)))
                     (client/post "http://127.0.0.1:8010/4/entities/" {:accept :json :content-type :json :basic-auth ["support@mastodonc.com" "password"] :body (json/write-str (assoc-in sample [:project_id]  (clojure.string/replace @project-loc  #"/4/projects/(.*?)" "$1")))})))
                  ["Location"]))
  (log/info "Entity: " @entity-loc))

(defn- post-device []
  (reset! device-loc 
          (get-in (:headers 
                   (let [sample (last (sg/generate-examples amon/BaseDevice))
                         reading-type (:type (first (:readings sample)))]
                     (reset! type reading-type)
                     (reset! poster-map (assoc-in @poster-map [:device-description] (:description sample)))
                     (client/post (apply str "http://127.0.0.1:8010" @entity-loc "/devices/") {:accept :json :content-type :json :basic-auth ["support@mastodonc.com" "password"] :body (json/write-str (assoc-in sample [:entity_id]  (clojure.string/replace @entity-loc  #"/4/entities/(.*?)" "$1")))})))
                  ["Location"]))
  (log/info "Devices: " @device-loc))

(defn- post-measurement []
  (client/post (apply str "http://127.0.0.1:8010" @device-loc "/measurements/") {:accept :json :content-type :json :basic-auth ["support@mastodonc.com" "password"] :body (json/write-str (assoc-in (sample-Measurement) [:measurements 0 :type] @type))})
  (log/info "Posting Measurements"))

                                        ;using a specific example -- schema for this may be too hard
(defn- post-dataset []
  (client/post (apply str "http://127.0.0.1:8010" @entity-loc "/datasets/") { :basic-auth ["support@mastodonc.com" "password"] :body  (json/write-str {:entity_id "9ac7f5635832d843dda594f58525239263ffdd37" :operation "divide" :name "systemEfficiencyOverall" :members ["interpolatedHeatConsumption-b4f0c7e2b15ba9636f3fb08379cc4b3798a226bb" "interpolatedElectricityConsumption-268e93a5249c24482ac1519b77f6a45f36a6231d"]})})
  (log/info "Posting dataset"))

;post one of everything
(defn post-data []
  (log/info "Posting data ....\n")
  (post-programme)
  (post-project)
  (post-entity)
  (post-device)
  (post-measurement)
  (post-dataset)
  )

;; ---------------------------------
;; get data back after calling post-data
;; --------------------------------

(defn- update-atoms []
  (reset! programme-loc (clojure.string/replace @programme-loc  #"/4(/.*?)" "$1"))
  (reset! project-loc (clojure.string/replace @project-loc  #"/4(/.*?)" "$1"))
  (reset! entity-loc (clojure.string/replace @entity-loc  #"/4(/.*?)" "$1"))
  (reset! device-loc (clojure.string/replace @device-loc  #"/4(/.*?)" "$1")))

(defn- get-programme []
  (log/info "\nProgramme data:\n")
  (let [output (json/read-json (:body (client/get (apply str "http://127.0.0.1:8010/4" @programme-loc) {:accept :json :content-type :json :basic-auth ["support@mastodonc.com" "password"]})))
        prog-name (:name output)]
    (reset! getter-map (assoc-in @getter-map [:programme-name] prog-name))
     (log/info output)))

(defn- get-project []
  (log/info "\nProject data:\n")
  (let [output (json/read-json (:body (client/get (apply str "http://127.0.0.1:8010/4" @programme-loc @project-loc) {:accept :json :content-type :json :basic-auth ["support@mastodonc.com" "password"]})))
        proj-name (:name output)]
    (reset! getter-map (assoc-in @getter-map [:project-name] proj-name))
     (log/info output)))

(defn- get-entity []
  (log/info "\nEntity data:\n")
  (let [output (json/read-json (:body (client/get (apply str "http://127.0.0.1:8010/4" @entity-loc) {:accept :json :content-type :json :basic-auth ["support@mastodonc.com" "password"]})))
        prop-code (:property_code output)]
    (reset! getter-map (assoc-in @getter-map [:entity-property-code] prop-code))
     (log/info output)))

(defn- get-device []
  (log/info "\nDevice data:\n")
  (let [output (json/read-json (:body (client/get (apply str "http://127.0.0.1:8010/4" @device-loc) {:accept :json :content-type :json :basic-auth ["support@mastodonc.com" "password"]})))
        device-desc (:description output)]
     (reset! getter-map (assoc-in @getter-map [:device-description] device-desc))
    (log/info output)))

(defn- get-data []
  (log/info "\nRetrieving generated data:")
  (update-atoms)
  (get-programme)
  (get-project)
  (get-entity)
  (get-device)
  )

;;---------------------------------
;; overall test to POST and GET a full set of data and check equality
;;--------------------------------
(deftest ^:http-tests test-hecuba []
  (post-data)
  (get-data)
  (log/info "\nTesting for POST and GET equality ... \n")
  (is (= @poster-map @getter-map)))

;; devices quick-check
;;-----------------------------
(defn- test-post-device [coll]
  (let [sample  (assoc-in coll [:entity_id]  (clojure.string/replace @entity-loc  #"/4/entities/(.*?)" "$1"))]
    (client/post (apply str "http://127.0.0.1:8010" @entity-loc "/devices/") {:content-type :json :basic-auth ["support@mastodonc.com" "password"] :body (json/write-str sample)}))
  )

(def ^:private postable-device?
  (prop/for-all [v (sg/generate amon/BaseDevice)]
                (test-post-device v)))

(deftest ^:http-tests test-devices-bug []
  (reset! entity-loc "/4/entities/821e6367f385d82cc71b2afd9dc2df3b2ec5b81c")
  (is (true? (get (tc/quick-check 10 postable-device?) :result)))
  )
;;-----------------------------
;; entity quick-check
;;-----------------------------
(defn- test-post-entity [coll]
  (let [sample  (assoc-in coll [:project_id]  "ba776928f94b3aaa1e444569276ee5b66d6b21f7")]
    (log/info "\n-----------------STARTING EXAMPLE--------------\n" sample)
    (client/post "http://127.0.0.1:8010/4/entities/" {:accept :json :content-type :json :basic-auth ["support@mastodonc.com" "password"] :body (json/write-str sample)}))
  )

(def ^:private postable-entity?
  (prop/for-all [v (sg/generate amon/BaseEntity)]
                (test-post-entity v)))

(deftest ^:http-tests test-entity-bug
  (is (true? (get (tc/quick-check 5 postable-entity?) :result)))
  )
;;-----------------------------
