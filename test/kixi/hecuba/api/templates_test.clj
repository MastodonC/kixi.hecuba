(ns kixi.hecuba.api.templates-test
  (:require [kixi.hecuba.api.templates :refer :all]
            [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [kixi.hecuba.protocols :as hecuba]
            [qbits.hayt :as hayt]
            [clojure.walk :refer (prewalk)]
            [ring.mock.request :refer (request content-type)]
            ))

(defn- multipart-request [method params]
    {:request {:request-method method
               :multipart-params params}})

(defn get-request [params]
  {:request {:request-method :get}})

(defn spit-temp-file-with [contents]
  (let [f (doto (java.io.File/createTempFile "hecuba" "tmp")
            (.deleteOnExit))]
    (spit f contents)
    f))

(defrecord MockStore [state results-index results]
  hecuba/Cassandra
  (-execute [session query opts]
    (swap! state conj [query opts])
    (nth results (swap! results-index inc)))
  (-execute-async [session query opts]
    (swap! state conj [query opts :async])
    (nth results (swap! results-index inc)))
  (-execute-chan [session query opts]
    (swap! state conj [query opts :chan])
    (nth results (swap! results-index inc))))

(defn operations [store & opts]
  (let [s @(.-state (:hecuba-session store))]
    (mapv #(update-in %1 [0] dissoc :id) s)))

(defn new-mock-store [& results]
  {:hecuba-session (->MockStore (atom [])
                                (atom -1)
                                (concat results (repeat nil)))})

(deftest malformed-test-good-post-request
  (is (= false
         (malformed? (multipart-request :post {"template" {:size 1000
                                                           :filename "foo.csv"
                                                           :tempfile (io/file "/tmp" "foo1231")
                                                           :content-type "application/octet-stream"}
                                               "name" "foo template"})))))


(deftest malformed-test-zero-size
  (is (= true
         (malformed? (multipart-request :post {"template" {:size 0
                                                           :filename "foo.csv"
                                                           :tempfile (io/file "/tmp" "foo1231")
                                                           :content-type "application/octet-stream"}
                                               "name" "foo template"})))))

(deftest index-exists-makes-correct-cql-call
  (let [store (new-mock-store (list {:id 1 :name "foo"}))
        result (index-exists? store
                              (get-request {:template_id 1}))]
    (is (= [[(hayt/select :csv_templates) nil]]
           (operations store)))
    (is (= {:kixi.hecuba.api.templates/items [{:id 1 :name "foo"}]}
           result))))

(deftest index-post-makes-correct-cql-call
  (let [contents "foo bar baz"
        tempfile (spit-temp-file-with contents)
        store    (new-mock-store {:id 123 :name "foo template" :filename "foo.csv" :template "foo bar baz"})
        result   (index-post! store
                              (multipart-request :post {"template" {:size (count (.getBytes contents))
                                                                    :filename "foo.csv"
                                                                    :tempfile tempfile
                                                                    :content-type "application/octet-stream"}
                                                        "name" "foo template"}))]
    (is (= [[(hayt/insert :csv_templates {:name "foo template"
                                          :filename "foo.csv"
                                          :template (slurp tempfile)}) nil]]
           (operations store :ignore-ids)))
    (is (= {:kixi.hecuba.api.templates/location "/4/templates/123"}
           result))))

(deftest resource-put-makes-correct-cql-call
  (let [contents "foo bar baz"
        tempfile (spit-temp-file-with contents)
        store    (new-mock-store {:id 123 :name "foo template" :filename "foo.csv" :template "foo bar baz"})
        result   (resource-put! store
                                (merge-with merge
                                 {:request {:route-params {:template_id 123}}}
                                 (multipart-request :put {"template" {:size         (count (.getBytes contents))
                                                                      :filename     "foo.csv"
                                                                      :tempfile     tempfile
                                                                      :content-type "application/octet-stream"}
                                                          "name"     "foo template"})))]
    (is (= [[(hayt/update :csv_templates
                          (hayt/set-columns {:name "foo template"
                                             :filename "foo.csv"
                                             :template (slurp tempfile)})
                          (hayt/where [[= :id 123]])) nil]]
           (operations store :ignore-ids)))
    (is (= {:kixi.hecuba.api.templates/location "/4/templates/123"}
           result))))

(deftest entity-resource-handle-ok-returns-correct-results
  (let [store    (new-mock-store [{:id 3000 :metadata {:customer_ref "cref 1"} :description "description"
                                   :location {:name "Bedroom"} :parent_id 1000}
                                  {:id 3001 :metadata {:customer_ref "cref 2"} :description "description2"
                                   :location {:name "Lounge"} :parent_id 1000}
                                  ]
                                 [{:device_id 3000
                                   :type "CO2"
                                   :accuracy "0"
                                   :resolution "60"
                                   :frequency "frequency3000/1"
                                   :period "INSTANT"
                                   :max "100"
                                   :min "0"}
                                  {:device_id 3000
                                   :type "electricityConsumption"
                                   :accuracy "5"
                                   :resolution "30"
                                   :frequency "frequency3000/2"
                                   :period "CUMULATIVE"
                                   :max "200"
                                   :min "10"}
                                  {:device_id 3000
                                   :type "solarRadiation"
                                   :accuracy "10"
                                   :resolution "40"
                                   :frequency "frequency3000/3"
                                   :period "PULSE"
                                   :max "450"
                                   :min "100"}
                                  {:device_id 3001
                                   :type "CO2"
                                   :accuracy "5"
                                   :resolution "6"
                                   :frequency "frequency3001/1"
                                   :period "INSTANT"
                                   :max "10"
                                   :min "5"}])
        result   (entity-resource-handle-ok store
                                            {:kixi.hecuba.api.entities/item {:id 1000 :name "foo"}
                                             :request {:route-params {:entity_id 123}}
                                             :content-type "text/csv"})]
    (is (=  {"Content-Disposition" "attachment; filename=1000_template.csv"}
            (get-in result [:response :headers])))

    (is (= '(["Device UUID" 3000 3000 3000 3001]
               ["Reading Type"
                "CO2"
                "electricityConsumption"
                "solarRadiation"
                "CO2"]
               ["Customer Ref" "cref 1" "cref 1" "cref 1" "cref 2"]
               ["Description"
                "description"
                "description"
                "description"
                "description2"]
               ["Location" "Bedroom" "Bedroom" "Bedroom" "Lounge"]
               ["Accuracy (percent)" "0" "5" "10" "5"]
               ["Sample Interval (seconds)" "60" "30" "40" "6"]
               ["Frequency"
                "frequency3000/1"
                "frequency3000/2"
                "frequency3000/3"
                "frequency3001/1"]
               ["Period" "INSTANT" "CUMULATIVE" "PULSE" "INSTANT"]
               ["Parent UUID" 1000 1000 1000 1000]
               ["Sensor Range Max" "100" "200" "450" "10"]
               ["Sensor Range Min" "0" "10" "100" "5"])
           (get-in result [:response :body])))
    )
  )
