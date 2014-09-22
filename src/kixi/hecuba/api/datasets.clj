(ns kixi.hecuba.api.datasets
  (:require
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [kixi.hecuba.storage.db :as db]
   [qbits.hayt :as hayt]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.data.misc :as misc]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.api.devices :as d]
   [kixi.hecuba.web-paths :as p]
   [cheshire.core :as json]
   [kixi.hecuba.data.entities :as entities]
   [kixi.hecuba.data.sensors :as sensors]
   [kixi.hecuba.data.datasets :as datasets]))

(def ^:private entity-dataset-resource (p/resource-path-string :entity-dataset-resource))

(defn all-datasets [store]
  (db/with-session [session (:hecuba-session store)]
    (datasets/get-all session)))

(defn sensors-for-dataset
  "Returns all the sensors for the given dataset."
  [{:keys [members]} store]
  (db/with-session [session (:hecuba-session store)]
    (let [parsed-sensors (map (fn [s] (into [] (next (re-matches #"(\w+)-(\w+)" s)))) members)
          sensor (fn [[type device_id]]
                   (sensors/get-by-id session device_id type))]
      (->> (mapcat sensor parsed-sensors)
           (map #(misc/merge-sensor-metadata store %))))))

(defn synthetic-device [entity_id description]
  (hash-map :description     description
            :parent_id       (str (uuid))
            :entity_id       entity_id
            :location        "{\"name\": \"Synthetic\", \"latitude\": \"0\", \"longitude\": \"0\"}"
            :privacy         "private"
            :metering_point_id (str (uuid))
            :synthetic       true))

(defn synthetic-sensor [operation type device_id unit]
  (let [period (case operation
                 :sum "PULSE"
                 :subtract "PULSE"
                 :divide "INSTANT")]
    {:device_id  device_id
     :type       type
     :unit       unit
     :period     period
     :status     "N/A"
     :synthetic  true}))

(defn synthetic-sensor-metadata [type device_id & [range]]
  (merge {:type      type
          :device_id device_id}
         (when-let [{:keys [start-date end-date]} range]
           {:calculated_datasets {"start" start-date "end" end-date}})))

(defn- entity_id-from [ctx]
  (get-in ctx [:request :route-params :entity_id]))

(defn- name-from [ctx]
  (get-in ctx [:request :route-params :name]))

(defmulti create-output-sensors (fn [store device_id unit members operation range] operation))

(defmethod create-output-sensors :subtract [store device_id unit type operation range]
  (db/with-session [session (:hecuba-session store)]
    (let [synthetic (synthetic-sensor operation type device_id unit)]
      (db/execute session (hayt/insert :sensors (hayt/values synthetic)))
      (db/execute session (hayt/insert :sensor_metadata (hayt/values (synthetic-sensor-metadata type device_id range))))

      (when-let [default  (d/calculated-sensor synthetic)]
        (db/execute session (hayt/insert :sensors (hayt/values default)))
        (db/execute session (hayt/insert :sensor_metadata (hayt/values (synthetic-sensor-metadata (:type default) device_id))))))))

(defmethod create-output-sensors :divide [store device_id unit type operation range]
  (db/with-session [session (:hecuba-session store)]
    (let [synthetic (synthetic-sensor operation type device_id unit)]
      (db/execute session (hayt/insert :sensors (hayt/values synthetic)))
      (db/execute session (hayt/insert :sensor_metadata (hayt/values (synthetic-sensor-metadata type device_id range))))

      (when-let [default  (d/calculated-sensor synthetic)]
        (db/execute session (hayt/insert :sensors (hayt/values default)))
        (db/execute session (hayt/insert :sensor_metadata (hayt/values (synthetic-sensor-metadata (:type default) device_id))))))))

(defmethod create-output-sensors :sum [store device_id unit type operation range]
  (db/with-session [session (:hecuba-session store)]
    (let [synthetic (synthetic-sensor operation type device_id unit)]
      (db/execute session (hayt/insert :sensors (hayt/values synthetic)))
      (db/execute session (hayt/insert :sensor_metadata (hayt/values (synthetic-sensor-metadata type device_id range))))

      (when-let [default  (d/calculated-sensor synthetic)]
        (db/execute session (hayt/insert :sensors (hayt/values default)))
        (db/execute session (hayt/insert :sensor_metadata (hayt/values (synthetic-sensor-metadata (:type default) device_id))))))))

(defn index-malformed? [ctx]
  (let [request (:request ctx)
        method  (:request-method request)
        {:keys [route-params request-method]} request
        entity_id (:entity_id route-params)]
    (case method
      :post (let [body (decode-body request)
                  {:keys [operation]} body]
              (if (some #{operation} ["divide" "sum" "subtract"])
                [false {:body body}]
                true))
      :get (if (seq entity_id) false true)
      false)))

(defn index-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request body]} ctx
          {:keys [route-params request-method]} request
          entity_id (:entity_id route-params)
          entity    (entities/get-by-id session entity_id)]
      (case request-method
        :post (let [{:keys [name operation members]} body
                    op           (keyword (.toLowerCase operation))
                    members      (into #{} members)
                    sensors      (sensors-for-dataset body store)]
                (when (and (not (nil? entity))
                           (= (count members) (count sensors)))
                  {::items {:entity_id entity_id :sensors sensors :operation op :members members :name name}}))
        :get (let [items (datasets/get-by-id session entity_id)]
               {::items items})))))

(defn stringify [k] (name k))

(defn index-post! [store ctx]
   (db/with-session [session (:hecuba-session store)]
     (let [{:keys [sensors operation members entity_id name]} (::items ctx)
           unit         (case operation
                          :sum (:unit (first sensors))
                          :subtract (:unit (first sensors))
                          :divide  (str (:unit (first sensors)) "/" (:unit (last sensors))))
           device        (synthetic-device entity_id "Synthetic")
           device_id     (sha1/gen-key :device (assoc device :description name))
           operation-str (stringify operation)
           range         (when-let [[start end] (misc/range-for-all-sensors sensors)]
                           {:start-date start :end-date end})]
       (db/execute session (hayt/insert :devices (hayt/values (assoc device :id device_id))))
       (create-output-sensors store device_id unit name operation range)
       (db/execute session (hayt/insert :datasets (hayt/values {:entity_id entity_id
                                                                :name      name
                                                                :members   members
                                                                :operation operation-str
                                                                :device_id device_id})))
       (hash-map ::name name
                 ::entity_id entity_id))))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [items (::items ctx)]
      (util/render-items (:request ctx) items))))

(defn index-handle-created [ctx]
  (let [entity_id   (::entity_id ctx)
        name        (::name ctx)]
    (if (and entity_id name)
      (let [location (format entity-dataset-resource entity_id name)]
        (ring-response {:headers {"Location" location}
                        :body (json/encode {:location location
                                            :status "OK"
                                            :version "4"})}))
      (ring-response {:status 400 :body (json/encode "Provide valid entity_id, sensors and operation.")}))))

(defresource index [store]
  :allowed-methods       #{:get :post}
  :available-media-types #{"application/edn" "application/json"}
  :known-content-type?   #{"application/edn" "application/json"}
  :authorized?           (authorized? store)
  :malformed?            index-malformed?
  :exists?               (partial index-exists? store)
  :post-to-missing?      (constantly false)
  :post!                 (partial index-post! store)
  :handle-ok             (partial index-handle-ok store)
  :handle-created        index-handle-created)

(defn resource-exists? [store ctx]
  (db/with-session [session [:hecuba-session store]]
    (let [request      (:request ctx)
          route-params (:route-params request)
          {:keys [entity_id name]} route-params]
      (log/infof "resource-exists? :%s:%s" entity_id name)

      (when-let [item (datasets/get-by-id session entity_id name)]
        {::item item}
        #_(throw (ex-info (format "Cannot find item of id %s")))))))

(defn resource-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          {:keys [members name operation]} (decode-body request)
          converted-type (misc/output-type-for operation)]

      (datasets/insert session {:entity_id (entity_id-from ctx)
                                :name      (name-from ctx)
                                :members   members
                                :operation operation}))))

(defn resource-handle-ok [ctx]
  (let [item (::item ctx)]
    (util/render-item ctx item)))

(defresource resource [store]
  :allowed-methods       #{:get :post}
  :available-media-types #{"application/edn" "application/json"}
  :known-content-type?   #{"application/edn" "application/json"}
  :authorized?           (authorized? store :datasets)
  :exists?               (partial resource-exists? store)
  :post!                 (partial resource-post! store)
  :handle-ok             resource-handle-ok)
