(ns kixi.hecuba.data_schema_test
  (:require [clojure.test :refer :all]
            [kixi.hecuba.application.system :as system]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.storage.search :as search]
            [modular.core :as mod]
            [com.stuartsierra.component :as component]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [kixi.amon-schema :as amon]
            [schema_gen.core :as sg]
            [schema.core :as s]
            [clojure.tools.logging  :as log]))


(defn- new-system []
  (let [cfg (system/config)]
    (-> (component/system-map
        :cluster (db/new-cluster (:cassandra-cluster cfg))
        :hecuba-session (db/new-session (:hecuba-session cfg))
        :search-session (search/new-search-session (:search-session cfg)))
        ;:store (db/new-store)
        (mod/system-using
         {;:store [:hecuba-session :s3 :search-session]
          :hecuba-session [:cluster]}))))

(def system (atom nil))

;; -----------------------------------
;; post data to check that everything required is running.
;; -----------------------------------

(defn- connected? []
  (let 
      [sample (last (sg/generate-examples amon/BaseProgramme))]
    (client/post "http://127.0.0.1:8010/4/programmes/" {:accept :json :content-type :json :basic-auth ["test@mastodonc.com" "password"] :body (json/write-str sample)}))
  (log/info "Posted => connected"))

;;-----------------------------------
;; main testing fixture
;;-----------------------------------

(defn data-test-fixture [tests]
  (try
    (connected?)
    (reset! system (component/start (new-system)))
    (tests)
    (component/stop @system)
    (log/info "Completed k.h.d.* tests inside 'use-fixture'.")
    (catch Exception e (log/info "Not connected."))))

(use-fixtures :once data-test-fixture)

;;-----------------------------------
;; programme
;;-----------------------------------
(def ^:private programme  {:programme_id "111"
                           :description "desc"
                           :lead_page_text "lead-text"
                           :public_access "access"
                           :home_page_text nil
                           :lead_organisations nil 
                           :name "TEST-prog-insert"
                           :leaders nil 
                           :updated_at nil 
                           :user_id "support@mastodonc.com" 
                           :created_at nil})

(defn- insert-programme [session prog]
  (println "\nInserting programme:\n" prog)
  (kixi.hecuba.data.programmes/insert session prog))

(defn- get-programme []
  (println "\n Returning programme with id: " (:programme_id programme) "\n"
           (kixi.hecuba.data.programmes/get-by-id 
            (:hecuba-session system) (:programme_id programme))))

;;-----------------------------------
;; project
;;-----------------------------------
(def ^:private project {:project_id "222"
                        :description nil
                        :organisation nil
                        :name "TEST-proj-insert"
                        :programme_id "111"
                        :project_type nil
                        :updated_at nil
                        :type_of nil
                        :user_id "support@mastodonc.com"
                        :project_code nil
                        :created_at nil})

(defn- insert-project [session proj]
  (println "\nInserting project:\n" proj)
  (kixi.hecuba.data.projects/insert session proj))

(defn- get-project []
  (println "\n Returning project with id:" (:project_id project) "\n"
           (kixi.hecuba.data.projects/get-by-id
            (:hecuba-session system) (:project_id project))))

;;-----------------------------------
;; entity
;;-----------------------------------
(def ^:private entity {:entity_id "333"
                       :address_county nil
                       :address_street_two nil
                       :property_data nil
                       :retrofit_completion_date nil
                       :name "TEST-entity-insert"
                       :project_id "222"
                       :property_code "3e49de0-12af-012e-4f3a-12313b0348f8"
                       :csv_uploads nil
                       :calculated_fields_last_calc {}
                       :calculated_fields_labels {}
                       :calculated_fields_values {}
                       :address_country nil
                       :documents nil
                       :photos nil
                       :address_region nil
                       :notes nil
                       :user_id "support@mastodonc.com"
                       :metering_point_ids nil})

(defn- insert-entity [session ent]
  (println "\nInserting entity:\n" ent)
  (kixi.hecuba.data.entities/insert session ent))

(defn- get-entity []
  (println "\n Returning entity  with id:" (:entity_id entity) "\n"
           (kixi.hecuba.data.entities/get-by-id
            (:hecuba-session system) (:entity_id entity))))

;;-----------------------------------
;; device
;;-----------------------------------
(def ^:private device {:device_id "444"
                       :description "Test data insert device"
                       :readings ({:min nil
                                   :unit "kWh"
                                   :user_metadata {}
                                   :accuracy nil
                                   :frequency nil
                                   :corrected_unit nil
                                   :type "electricityConsumption"
                                   :correction_factor nil
                                   :upper_ts #inst "2014-01-02T17:35:00.000-00:00"
                                   :correction nil
                                   :resolution "60"
                                   :alias nil
                                   :median 0.0
                                   :status nil
                                   :max nil
                                   :lower_ts #inst "2014-01-01T00:00:00.000-00:00"
                                   :correction_factor_breakdown nil
                                   :period "PULSE", :synthetic false
                                   :device_id "444"
                                   :actual_annual false}
                                  {:min nil
                                   :unit "co2"
                                   :user_metadata {}
                                   :accuracy nil
                                   :frequency nil
                                   :corrected_unit nil
                                   :type "electricityConsumption_co2"
                                   :correction_factor nil
                                   :upper_ts nil
                                   :correction nil
                                   :resolution "60"
                                   :alias nil
                                   :median 0.0
                                   :status nil, :max nil
                                   :lower_ts nil, :correction_factor_breakdown nil
                                   :period "PULSE"
                                   :synthetic true
                                   :device_id "444"
                                   :actual_annual false})
                       :name "TEST-insert-device"
                       :privacy nil
                       :metering_point_id nil
                       :parent_id nil
                       :entity_id "333"
                       :synthetic false
                       :location nil
                       :metadata nil})

(defn- insert-device [session dev]
  (println "\nInserting device:\n" dev)
  (kixi.hecuba.data.devices/insert session (:entity_id dev) dev))

(defn- get-device []
  (println "\n Returning device  with id:" (:device_id device) "\n"
           (kixi.hecuba.data.devices/get-by-id
            (:hecuba-session system) (:device_id device))))

;;-----------------------------------
;; sensor
;;-----------------------------------
(def ^:private sensor {:device_id "444"
                       :type "test-type"})

(defn- insert-sensor [session sen]
  (println "\nInserting sensor:\n" sen)
  (kixi.hecuba.data.sensors/insert session sen))

(defn- get-sensor[]
  (println "\n Returning sensors with device id:" (:device_id device) "\n"
           (kixi.hecuba.data.sensors/get-sensors
            (:device_id device) (:hecuba-session system))))

;;-----------------------------------
;; overall tests
;;-----------------------------------
(defn- cleanup [session]
  (println "\nDeleting created objects...")
  (kixi.hecuba.data.devices/delete (:device_id device) true session)
  (println "\nDeleted device and sensors")
  (kixi.hecuba.data.entities/delete (:entity_id entity) session)
  (println "\nDeleted entity")
  (kixi.hecuba.data.projects/delete (:project_id project) session)
  (println "\nDeleted project")
  (kixi.hecuba.data.programmes/delete (:programme_id programme) session)
  (println "\nDeleted programme"))

(deftest ^:data-tests test-all
  (is (nil?
       (let
           [insert-prog (assoc programme :name (last (sg/generate-examples s/Str)))
            insert-proj (assoc project :name (last (sg/generate-examples s/Str)))
            insert-ent (assoc entity :name (last (sg/generate-examples s/Str)))
            insert-dev (assoc device :name (last (sg/generate-examples s/Str)))
            insert-sen (assoc sensor :type (last (sg/generate-examples s/Str)))]
         (db/with-session [session (:hecuba-session @system)]
           (insert-programme session insert-prog)
           (insert-project session insert-proj)
           (insert-entity session insert-ent)
           (insert-device session insert-dev)
           (insert-sensor session insert-sen)
           (get-programme)
           (get-project)
           (get-entity)
           (get-device)
           (get-sensor)
           (cleanup session))))))


;;--------------------------------
;; Helper methods, these are not needed anymore.
;;-------------------------------
(comment

  (defn- get-all-programmes []
    (println "\n Returning all programmes:\n" (kixi.hecuba.data.programmes/get-all
                                               (:hecuba-session (component/start (new-system))))))

  (defn- test-programme []
    (db/with-session [session (:hecuba-session (component/start (new-system)))]
      (insert-programme session programme))
    (get-programme))

(defn- get-all-projects []
  (println "\n Returning all projects:\n" (kixi.hecuba.data.projects/get-all
            (:hecuba-session (component/start (new-system))))))

(defn- test-project []
  (db/with-session [session (:hecuba-session (component/start (new-system)))]
    (insert-project session project))
  (get-project))

(defn- get-all-entities []
  (println "\n Returning all entities:\n" (kixi.hecuba.data.entities/get-all
            (:hecuba-session (component/start (new-system))))))

(defn- test-entity []
  (db/with-session [session (:hecuba-session (component/start (new-system)))]
    (insert-entity session entity))
  (get-entity))

(defn- get-some-devices []
  (println "\n Returning all devices for given entity:\n"
           (kixi.hecuba.data.devices/get-devices
            (:hecuba-session (component/start (new-system))) "333")))

(defn- test-device []
  (db/with-session [session (:hecuba-session (component/start (new-system)))]
    (insert-device session device))
  (get-device))

(defn- get-some-sensors []
  (println "\n Returning all sensors for given device:\n"
           (kixi.hecuba.data.sensors/get-sensors
            "444"
            (:hecuba-session (component/start (new-system))))))

(defn- test-sensor []
  (db/with-session [session (:hecuba-session (component/start (new-system)))]
    (insert-sensor session sensor))
  (get-sensor))

)
