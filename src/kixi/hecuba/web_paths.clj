(ns kixi.hecuba.web-paths)

(def paths
  {;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;; WEB UI
   :login "login"
   :logout "logout"
   :app "app"
   :admin "admin"
   :profile "profile" ;; user profile settings
   :multiple-properties-comparison "properties_comparison"
   :property_map "property_map"
   :property-search "search"
   :user-management "user"

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
   :project-entities-index "projects/%s/entities"

   ;; entities
   :entities-index "entities"
   :entity-resource "entities/%s"

   ;; all properties
   :properties "properties"

   ;; datasets
   :entity-datasets-index "entities/%s/datasets"
   :entity-dataset-resource "entities/%s/datasets/%s"

   ;; devices
   :entity-devices-index "entities/%s/devices"
   :entity-device-resource "entities/%s/devices/%s"

   ;; profiles
   :entity-profiles-index "entities/%s/profiles"
   :entity-profiles-resource "entities/%s/profiles/%s"

   ;; images
   :entity-images-index "entities/%s/images"
   :entity-images-resource "entities/%s/images/%s"

   ;; documents
   :entity-documents-index "entities/%s/documents"
   :entity-documents-resource "entities/%s/documents/%s"


   ;; measurements
   :entity-device-measurement-index "entities/%s/devices/%s/measurements"
   :entity-device-measurement-readingtype-index "entities/%s/devices/%s/measurements/%s"
   :entity-device-measurement-readingtype-resource "entities/%s/devices/%s/measurements/%s/%s"

   ;; properties having locations
   :entity-property-having-locations "entities/having-locations"

   ;; hourly readings
   ;; FIXME: This should be a query parameter and not a separate resource (or rather the raw should force not choosing the hourly/daily)
   :entity-device-measurement-readingtype-hourly-rollups "entities/%s/devices/%s/hourly_rollups/%s"

   ;; daily readings
   ;; FIXME: This should be a query parameter and not a separate resource
   :entity-device-measurement-readingtype-daily-rollups "entities/%s/devices/%s/daily_rollups/%s"
   :templates-index "templates"
   :templates-resource "templates/%s"
   :entity-templates-resource "templates/for-entity/%s" ;; TODO better url
   :measurements "measurements/for-entity/%s"

   :uploads-status-resource "uploads/%s/%s/status"
   :uploads-data-resource "uploads/%s/%s/data"

   :downloads-status-resource "downloads/%s/%s/status"
   :download-data-resource "download/%s/data"

   :upload-status-for-username "uploads/for-username/programme/%s/project/%s/entity/%s/status"
   :download-status "downloads/programme/%s/project/%s/entity/%s/status"

   ;; Usernames
   :username-index "usernames"
   :username-resource "usernames/%s"})

(defn compojure-route
  ([route keys]
     (str "/" (apply (partial format (get paths route)) keys)))
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
  (str "/" amon-api-version "/" (get paths route) "/"))

(defn resource-path-string
  [route]
  (str "/" amon-api-version "/" (get paths route)))
