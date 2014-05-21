(ns etl.fixture
  (:require [kixi.hecuba.storage.dbnew :as dbnew]
            [qbits.hayt :as hayt]
            [generators]))

(def re-kw #"(\w+)(?:\.(\w+))?")

(defn table-arg-name [table]
  (case table
    :sensors :type
    :name))

(defmulti compile-element type)

(defmethod compile-element clojure.lang.Keyword [kw]
  (let [[_ table-name & [arg]] (re-matches re-kw (name kw))
        table (keyword table-name)]
    (cond-> {::table table}
            arg (assoc (table-arg-name table) arg))))

(defmethod compile-element clojure.lang.IPersistentMap [m] m)

(defmethod compile-element clojure.lang.IPersistentVector [xs]
  (reduce (fn [a x] (if (map? x)
                     (conj (pop a) (merge (last a) x))
                     (conj a (compile-element x))))
          []
          xs))

(defmethod compile-element clojure.lang.ISeq [xs]
  (for [x xs] (compile-element x)))

(defn- insert* [session table values]
  (dbnew/execute session
     (hayt/insert table (hayt/values values))))

(defn- insert-simple [x session]
    (let [id (str (generators/uuid))
        data (-> x
                 (assoc :id id)
                 (dissoc x ::table))]
      (insert* session (::table x) data)
      id))

(defmulti insert (fn [x _ _] (::table x)) :default :default)

(defmethod insert :default [x session ctx]
  (insert-simple x session))

(defmethod insert :programmes [x session ctx]
  (assoc ctx :programme_id
         (insert-simple x session)))

(defmethod insert :projects [x session ctx]
  (assoc ctx :project_id
         (insert-simple x session)))

(defmethod insert :entities [x session ctx]
  (assoc ctx :entity_id
         (insert-simple x session)))

(defmethod insert :properties [x session ctx]
  (assoc ctx :property_id
         (insert-simple x session)))

(defmethod insert :devices [x session ctx]
  (let [m {:id (str (generators/uuid))
           :entity_id (:entity_id ctx)}
        device (-> (generators/generate-device-sample (:entity_id x))
                   (dissoc :device-id)
                   (update-in [:meteringPointId] str)
                   (update-in [:parent-id] str)
                   (update-in [:location] pr-str))
        data (->> (dissoc x ::table)
                  (merge
                   device
                   m))]

    (insert* session :devices data)
    (merge ctx {:device_id (:id m)})))

(defmethod insert :sensors [x session ctx]
  (let [m {:device_id (:device_id ctx)
           :type (:type x)}
        start (::start x) ;; TODO clojure 1.6-ify
        end   (::end x)]
    (println "SS:"  (->> (dissoc x ::table ::start ::end)
                  (merge
                   (generators/generate-sensor-sample (:id x))
                   m)))
    (insert* session :sensors
             (->> (dissoc x ::table ::start ::end)
                  (merge
                   (generators/generate-sensor-sample (:id x))
                   m)))
    (println "SS2")
    (insert* session :sensor_metadata
             (->> m
                  (merge
                   {:lower_ts start
                    :upper_ts end}
                   m)))
    (merge ctx m)))

(defmethod insert :measurements [x session ctx]
  (let [m     (select-keys ctx [:device-id type])]
    (insert* :measurements
             (->> (dissoc x
                          ::table
                          ::start
                          ::end)
                  (merge
                   (generators/generate-measurements (:id x))
                   m)))))

(defn insert-seq
  ([xs session]
     (insert-seq xs session {}))
  ([xs session ctx]
     (loop [[x & more] xs ctx ctx]
       (when x
         (recur more (if (vector? x)
                       (insert-seq x session ctx)
                       (insert x session ctx)))))))

(defn load-fixture [session fixture]
  (let [data (compile-element fixture)]
    ;; (clojure.pprint/pprint data)
    (insert-seq data session)))
