(ns kixi.hecuba.api.datasets
  (:require
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [qbits.hayt :as hayt]
   [kixi.hecuba.queue :as q]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.data.misc :as misc]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid)]
   [kixi.hecuba.storage.db :as db]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.api.devices :as d]
   [kixi.hecuba.web-paths :as p]))

(def ^:private entity-dataset-resource (p/resource-path-string :entity-dataset-resource))

(defn all-datasets [store]
  (db/with-session [session (:hecuba-session store)]
    (db/execute session
                (hayt/select :datasets))))

(defn synthetic-device [entity_id description]
  (hash-map :description     description
            :parent_id       (str (uuid))
            :entity_id       entity_id
            :location        "{\"name\": \"Synthetic\", \"latitude\": \"0\", \"longitude\": \"0\"}"
            :privacy         "private"
            :metering_point_id (str (uuid))
            :synthetic       true))

(defn synthetic-sensor [type device_id unit]
  {:device_id                 device_id
   :type                      type
   :unit                      unit
   :period                    "PULSE"
   :status                    "N/A"
   :synthetic                 true})

(defn synthetic-sensor-metadata [type device_id]
  {:type      type
   :device_id device_id})

(defn- entity_id-from [ctx]
  (get-in ctx [:request :route-params :entity_id]))

(defn- name-from [ctx]
  (get-in ctx [:request :route-params :name]))

;; TODO This shoudl be more generic.
;; Creation of default sensors is done in devices.clj when new device is added - maybe should add  common function for both cases
;; (when new device is added and when synthetic sensor is created).
(defmulti create-output-sensors (fn [store device_id unit members operation] (keyword (.toLowerCase operation))))

(defmethod create-output-sensors :total-kwh [store device_id unit _ operation]
  (db/with-session [session (:hecuba-session store)]
    (let [type      (misc/output-type-for nil operation)
          synthetic (synthetic-sensor type device_id (misc/output-unit-for operation))
          default   (d/calculated-sensor synthetic)]
      (db/execute session (hayt/insert :sensors (hayt/values synthetic)))
      (db/execute session (hayt/insert :sensors (hayt/values default)))
      (db/execute session (hayt/insert :sensor_metadata (hayt/values (synthetic-sensor-metadata type device_id))))
      (db/execute session (hayt/insert :sensor_metadata (hayt/values (synthetic-sensor-metadata (:type default) device_id)))))))

(defmethod create-output-sensors :system-efficiency-overall [store device_id _ _ operation]
  (db/with-session [session (:hecuba-session store)]
    (let [type      (misc/output-type-for nil operation)
          synthetic (synthetic-sensor type device_id nil)]
      (db/execute session (hayt/insert :sensors (hayt/values synthetic)))
      (db/execute session (hayt/insert :sensor_metadata (hayt/values (synthetic-sensor-metadata type device_id)))))))

(defn index-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request      (:request ctx)
          method       (:request-method request)
          route-params (:route-params request)
          {:keys [entity_id name]} route-params
          entity       (-> (db/execute session (hayt/select :entities (hayt/where [[= :id entity_id]]))) first)]
      (case method
        :post (not (nil? entity))
        :get (let [items (db/execute session (hayt/select :datasets (hayt/where [[= :entity_id entity_id]
                                                                                 [= :name name]])))]
               {::items items})))))

(defn index-post! [store ctx]
   (db/with-session [session (:hecuba-session store)]
     (let [entity_id (entity_id-from ctx)]
       (when-not (empty? (first (db/execute session (hayt/select :entities (hayt/where [[= :id entity_id]])))))
         (let [request   (:request ctx)
               {:keys [members name operation]} (decode-body request)
               members   (into #{} members)
               unit      (misc/output-unit-for operation)
               device    (synthetic-device entity_id "Synthetic")
               device_id (sha1/gen-key :device device)]
           (db/execute session (hayt/insert :devices (hayt/values (assoc device :id device_id))))
           (create-output-sensors store device_id unit members operation)
           (db/execute session (hayt/insert :datasets (hayt/values {:entity_id entity_id
                                                                    :name      name
                                                                    :members   members
                                                                    :operation operation
                                                                    :device_id device_id})))           
           (hash-map ::name name
                     ::entity_id entity_id))))))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [entity_id   (entity_id-from ctx)]
      (util/render-items ctx (db/execute session (hayt/select :dataset (hayt/where [[= :entity_id entity_id]])))))))

(defn index-handle-created [ctx]
  (let [entity_id   (::entity_id ctx)
        name        (::name ctx)]
    (if (and entity_id name)
      (let [location (format entity-dataset-resource entity_id name)]
        (ring-response {:headers {"Location" location}}))
      (ring-response {:status 400 :body "Provide valid entity_id."}))))

(defresource index [store]
  :allowed-methods       #{:get :post}
  :available-media-types #{"application/edn" "text/html"}
  :authorized?           (authorized? store)
  :post!                 (partial index-post! store)
  :handle-ok             (partial index-handle-ok store)
  :handle-created        index-handle-created)

(defn resource-exists? [store ctx]
  (db/with-session [session [:hecuba-session store]]
    (let [request      (:request ctx)
          route-params (:route-params request)
          {:keys [entity_id name]} route-params]
      (log/infof "resource-exists? :%s:%s" entity_id name)

      (when-let [item (first (db/execute session (hayt/select :datasets (hayt/where [[= :entity_id entity_id]
                                                                                     [= :name name]]))))]
        {::item item}
        #_(throw (ex-info (format "Cannot find item of id %s")))))))

(defn resource-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          {:keys [members name operation]} (decode-body request)
          converted-type              (misc/output-type-for operation)]

      (db/execute session (hayt/insert :datasets (hayt/values {:entity_id (entity_id-from ctx)
                                                               :name      (name-from ctx)
                                                               :members   members
                                                               :operation operation}))))))

(defn resource-handle-ok [ctx]
  (let [item (::item ctx)]
    (util/render-item ctx item)))

(defresource resource [store]
  :allowed-methods       #{:get :post}
  :available-media-types #{"application/edn" "text/html"}
  :authorized?           (authorized? store :datasets)
  :exists?               (partial resource-exists? store)
  :post!                 (partial resource-post! store)
  :handle-ok             resource-handle-ok)
