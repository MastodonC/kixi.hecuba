(ns kixi.hecuba.web-paths)

(def paths
  {;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; WEB UI
   :login "login"
   :logout "logout"
   :app "app"
   :admin "admin"
   :profile "profile" ;; user profile settings

   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; API

   ;; programmes
   :programmes-index "programmes"
   :programme-resource "programmes/%s"

   ;; projects
   :programme-projects-index "programmes/%s/projects"
   :programme-projects-resource "programmes/%s/projects/%s"
   :projects-index "projects"
   :project-resource "projects/%s"
   :project-properties-index "projects/%s/properties"

   ;; enitites
   :entities-index "entities"    
   :entity-resource "entities/%s"

   ;; datasets
   :entity-datasets-index "entities/%s/datasets"
   :entity-dataset-resource "entities/%s/datasets/%s"

   ;; devices
   :entity-devices-index "entities/%s/devices"
   :entity-device-resource "entities/%s/devices/%s"

   ;; profiles
   :entity-profiles-index "entities/%s/profiles"
   :entity-profiles-resource "entities/%s/profiles/%s"

   ;; measurements
   :entity-device-measurement-index "enitites/%s/devices/%s/measurements"
   :entity-device-measurement-readingtype-index "enitites/%s/devices/%s/measurements/%s"})

(defn compojure-route
  ([route keys]
     (str "/" (apply (partial format (route paths)) keys)))
  ([route]
     (compojure-route route [])))

(def amon-api-version "4")

(defn amon-index-route
  ([route keys]
     (str "/" amon-api-version (compojure-route route keys) "/"))
  ([route]
     (amon-index-route route [])))

(defn amon-resource-route
  ([route keys]
     (str "/" amon-api-version (compojure-route route keys)))
  ([route]
     (amon-resource-route route [])))

(defn index-path-string
  [route]
  (str "/" amon-api-version "/" (route paths) "/"))

(defn resource-path-string
  [route]
  (str "/" amon-api-version "/" (route paths)))
