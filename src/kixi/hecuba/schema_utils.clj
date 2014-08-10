(ns kixi.hecuba.schema-utils
  (:require [schema.core :as s]))

(defn schema-keys
  "Get all the optional and required keys from a schema"
  [schema]
  (keep #(cond
          (s/optional-key? %) (:k %)
          (keyword? %) %
          :else nil) (keys schema)))

(defn select-keys-by-schema [m schema]
  (let [skeys (schema-keys schema)]
    (select-keys m skeys)))
