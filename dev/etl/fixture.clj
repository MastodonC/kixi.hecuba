(ns etl.fixture
  (:require [kixi.hecuba.storage.dbnew :as dbnew]
            [qbits.hayt :as hayt]
            [generators]))

(def re-kw #"(\w+)(?:\.(\w+))?")

(defn table-arg-name [table]
  (case table
    :sensors :unit
    :devices :type
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

(defmulti insert ::table :default :default)

(defmethod insert :default [x session ctx]
  (let [id (generators/uuid)]
    (insert* session (::table x)
             (dissoc x ::table))))

(defmethod insert :devices [x session ctx]
  (let [m {:device-id (generators/uuid)}]
    (insert* session :devices
             (->> (dissoc x ::table)
                  (merge
                   (generators/generate-device-sample (:id x) 1)
                   m)))
    (merge ctx m)))

(defmethod insert :sensors [x session ctx]
  (let [m (select-keys ctx [:device-id type])
        start (::start x) ;; TODO clojure 1.6-ify
        end   (::end x)]
    (insert* session :sensors
             (->> (dissoc x ::table)
                  (merge
                   (generators/generate-sensor-sample (:id x) 1)
                   m)))
    (insert* session :sensor_metadata
             (->> (dissoc x ::table)
                  (merge
                   {:lower_ts start
                    :upper_ts end}
                   m)))
    (assoc ctx m)))

(defmethod insert :measurements [x session ctx]
  (let [m     (select-keys ctx [:device-id type])]
    (insert* :measurements
             (->> (dissoc x
                          ::table
                          ::start
                          ::end)
                  (merge
                   (generators/generate-measurements (:id x) 1)
                   m)))))

(defn load-fixture [session fixture]
  (let [data (compile-element fixture)]
    (tree-seq vector? rest data)))
