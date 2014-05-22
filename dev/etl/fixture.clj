(ns etl.fixture
  (:require [kixi.hecuba.storage.dbnew :as dbnew]
            [qbits.hayt :as hayt]
            [generators]))

(def re-kw #"(\w+)(?:#(?:(\w+)(?:\.(\w+))?))?")

(defn table-arg-name [table]
  (case table
    :sensors :type
    :measurements :period
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

(defn- insert* [session values]
  (dbnew/execute session
     (hayt/insert (::table values)
                  (hayt/values (dissoc values ::table)))))

(defmulti insert (fn [x _ _] (::table x)) :default :error-no-table)

(defmethod insert :error-no-table [x _ _ ]
  nil)

(defmethod insert :programmes [x session ctx]
  (let [id (str (generators/uuid))]
    (insert* session (merge
                      {:id id}
                      x))
    (assoc ctx :programme_id id)))

(defmethod insert :projects [x session ctx]
  (let [id (str (generators/uuid))]
    (insert* session (merge  {:id id}
                            (select-keys ctx [:programme_id])
                            x))
   (assoc ctx :project_id id)))

(defmethod insert :entities [x session ctx]
  (let [id (str (generators/uuid))]
    (insert* session (merge  {:id id}
                             (select-keys ctx [:project_id])
                             x))
    (assoc ctx :entity_id id)))

(defmethod insert :properties [x session ctx]
  (let [m (select-keys ctx [:programme_id])
        id (str (generators/uuid))]
   (insert* session (merge  {:id id}
                            (select-keys ctx [:programme_id])
                            x))
   (assoc ctx :entity_id id)))

(defmethod insert :devices [x session ctx]
  (let [m {:id (str (generators/uuid))
           :entity_id (:entity_id ctx)}
        device (-> (generators/generate-device-sample (:entity_id x))
                   (dissoc :device_id)
                   (update-in [:meteringPointId] str)
                   (update-in [:parent_id] str)
                   (update-in [:location] pr-str))]

    (insert* session (merge x device m))
    (assoc ctx :device_id (:id m))))

(defmethod insert :sensors [x session ctx]
  (let [m {:device_id (:device_id ctx)
           :type (:type x)}
        start (::start x) ;; TODO clojure 1.6-ify
        end   (::end x)]
    (insert* session
             (->> (dissoc x ::start ::end)
                  (merge
                   (generators/generate-sensor-sample (:id x))
                   m)))
    (insert* session
             (->> m
                  (merge
                   {::table :sensor_metadata
                    :lower_ts start
                    :upper_ts end}
                   m)))
    (merge ctx m)))

(defmethod insert :measurements [x session ctx]

  (when (contains? x :value)
    (let [m (select-keys ctx [:device_id :type])]
      (insert* session (merge
                        x
                        m))))
  ctx)

(defn insert-vector
  ([xs session]
     (insert-vector xs session {}))
  ([xs session ctx]
     (loop [[x & more] xs ctx ctx]
       (when x
         (recur more (cond
                      (vector? x) (insert-vector x session ctx )
                      (seq? x) (doseq [y x]
                                 (insert (merge (::prior ctx) y)
                                         session
                                         ctx))
                      (map? x) (insert x session (assoc ctx ::prior x))
                      :else (throw (ex-info (str "Invalid value " x) {}))))))))

(defn load-fixture [session fixture]
  (let [data (compile-element fixture)]
    (insert-vector data session)))
