(ns kixi.hecuba.tabs.slugs
  (:require [clojure.string :as str]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Slugs
(defn slugify-programme [programme]
  (assoc programme :slug (:name programme)))

(defn slugify-project "Create a slug for projects in the UI"
  [project]
  (assoc project :slug (:name project)))

(defn- postal-address-filter [property_data]
  (filter #(when %
             (re-seq #"[A-Za-z0-9]" %))
          [(:address_street property_data)
           (:address_street_two property_data)
           (:address_city property_data)
           (:address_code property_data)
           (:address_region property_data)
           (:address_country property_data)]))

(defn postal-address-html
  [property_data]
  (interpose [:br ] (postal-address-filter property_data)))

(defn postal-address
  ([property_data separator]
     (str/trim (str/join separator (postal-address-filter property_data))))
  ([property_data]
     (postal-address property_data ", ")))

(defn slugify-property
  "Create a slug for a property in the UI"
  [property]
  (let [property_data (:property_data property)]
    (assoc property :slug (let [property_code (get property :property_code "CODELESS")
                                addr (postal-address property_data)]
                            (if (empty? addr)
                              property_code
                              (str property_code  ", " addr))))))

(defn slugify-whoami [m]
  [:div.whoami (:identity m)])
