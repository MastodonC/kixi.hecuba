;; Some utilities that help speed up development
(ns dev
  (:require
   clojure.edn
   clojure.set
   [bidi.bidi :refer (path-for match-route)]
   [clojure.pprint :refer (pprint)]

   [clojure.tools.logging :refer :all]
   [clojure.java.io :as io]
   [clojure.walk :refer (postwalk)]

   [org.httpkit.client :refer (request) :rename {request http-request}]
  ))

(defn post-resource [post-uri data]
  (http-request
   {:method :post
    :url post-uri
    :basic-auth ["bob" "secret"]
    :body [data]}
   identity))

;; This is going over HTTP, kixi.hecuba.amon-test goes over ring-mock, so they're a bit different.

(def dummy-data
  {:programmes
   [{:name "America"
     :leaders "Bush"
     :projects
     [{:name "Green Manhattan"
       :properties [{:name "The Empire State Building"
                     :address "New York"
                     :rooms 100
                     :date-of-construction 1930}]}


      {:name "The Historical Buildings Project"
       :properties [{:name "Falling Water"
                     :address "1491 Mill Run Rd, Mill Run, PA"
                     :rooms 4
                     :date-of-construction 1937}]}

      {:name "Area 51 Conservation Project"}]}

    {:name "London"
     :leaders "Blair"
     :projects
     [{:name "Monarchy Energy Savings"
       :properties [{:name "Buckingham Palace"
                     :address "London SW1A 1AA, United Kingdom"
                     :rooms 775}

                    {:name "Windsor Castle"
                     :rooms 175
                     :devices [{:name "Corgi Boiler"
                                :room "Throne Room"}
                               {:name "Crown Jewels Security Camera"
                                :room "The Vault"}
                               {:name "Lord Lucan Escape Monitor"
                                :room "Dungeon"}]
                     }]}

      {:name "Carbon Neutral Tech City"
       :properties [{:name "The ODI"
                     :address "3rd Floor, 65 Clifton Street, London EC2A 4JE"
                     :rooms 13
                     }]}]}]})

(defn spider
  "A spider takes some data, and constructs a new map. The mapping given
  as the second argumen is a mapping between keys and functions that,
  when given the data as an argument, will provide a entry's value."
  [data mapping]
  (letfn [(as-coll [c] (if (coll? c) c (list c)))
          (tr [f]
            (cond
             (vector? f) (fn [x] (filter (apply comp (reverse (map tr f))) x))
             ;; Collections treated like node-sets are in xpath
             (list? f) (partial map (first f))
             :otherwise f)
            )]
    (reduce-kv
     (fn [s k path]
       (if-let
           [val (reduce
                 (fn [e f] (when e ((tr f) e)))
                 data (as-coll path))]
         (assoc s k val)
         s))
     {} mapping)))





