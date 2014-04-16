(ns kixi.hecuba.amon
  (:require
   [bidi.bidi :as bidi]
   [camel-snake-kebab :as csk :refer (->kebab-case-keyword)]
   [cheshire.core :as json]
   [clj-time.coerce :as tc]
   [clj-time.core :as t]
   [clj-time.format :as tf]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [clojure.walk :refer (postwalk)]
   [com.stuartsierra.component :as component]
   [hiccup.core :refer (html)]
   [kixi.hecuba.api.datasets :as datasets]
   [kixi.hecuba.api.device :as device]
   [kixi.hecuba.api.entity :as entity]
   [kixi.hecuba.api.measurement :as measurement]
   [kixi.hecuba.api.programme :as programme]
   [kixi.hecuba.api.project :as project]
   [kixi.hecuba.api.property :as property]
   [kixi.hecuba.api.rollup :as rollup]
   [kixi.hecuba.api.sensor :as sensor]
   [kixi.hecuba.data.misc :as misc]
   [kixi.hecuba.data.validate :as v]
   [kixi.hecuba.protocols :refer (upsert! delete! update! item items)]
   [kixi.hecuba.queue :as q]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [ring.middleware.cookies :refer (wrap-cookies)]))


;; TODO - this is a hack - malcolm to advise.
(extend-protocol bidi/Matched
    nil
    (resolve-handler [this m] nil)
    (unresolve-handler [this m] nil))

(defn make-handlers [opts queue]
  (let [p (promise)]
    @(deliver p {:programmes (programme/programmes opts p)
                 :programme (programme/programme opts p)

                 :projects (project/projects opts p)
                 :allprojects (project/projects opts p)
                 :project (project/project opts p)

                 :properties (property/properties opts p)
                 :entities (entity/entities opts p)
                 :entity (entity/entity opts p)

                 :devices (device/devices opts p)
                 :device (device/device opts p)
                 :sensor-metadata (sensor/sensor-metadata opts p)
                 :measurements (measurement/measurements opts queue p)
                 :measurement (measurement/measurements-by-reading opts p)
                 :measurement-slice (measurement/measurements-slice opts p)
                 :hourly-rollups (rollup/hourly-rollups opts p)
                 :sensors-by-property (sensor/sensors-by-property opts p)
                 :datasets (datasets/items opts p)
                 :dataset (datasets/item opts p)
                 })))


(defn make-routes [handlers]
  ;; AMON API here
  ["/" (bidi/->WrapMiddleware
        [
         ["programmes/" (:programmes handlers)]
         ["programmes" (bidi/->Redirect 301 (:programmes handlers))]
         [["programmes/" [sha1-regex :programme-id]] (:programme handlers)]
         [["programmes/" [sha1-regex :programme-id] "/projects/"] (:projects handlers)]
         [["programmes/" [sha1-regex :programme-id] "/projects"] (bidi/->Redirect 301 (:projects handlers))]

         ["projects/" (:allprojects handlers)]
         ["projects" (bidi/->Redirect 301 (:allprojects handlers))]
         [["projects/" [sha1-regex :project-id]] (:project handlers)]
         [["projects/" [sha1-regex :project-id] "/properties/"] (:properties handlers)]
         [["projects/" [sha1-regex :project-id] "/properties"] (bidi/->Redirect 301 (:properties handlers))]


         ["entities/" (:entities handlers)]
         ["entities" (bidi/->Redirect 301 (:entities handlers))]
         [["entities/" [sha1-regex :entity-id]] (:entity handlers)]
         [["entities/" [sha1-regex :entity-id] "/datasets/"] (:datasets handlers)]
         [["entities/" [sha1-regex :entity-id] "/datasets"] (bidi/->Redirect 301 (:datasets handlers))]

         [["entities/" [sha1-regex :entity-id] "/datasets/" :name] (:dataset handlers)]
         [["entities/" [sha1-regex :entity-id] "/sensors/"] (:sensors-by-property handlers)]
         [["entities/" [sha1-regex :entity-id] "/sensors"] (bidi/->Redirect 301 (:sensors-by-property handlers))]

         [["entities/" [sha1-regex :entity-id] "/devices/"] (:devices handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices"] (bidi/->Redirect 301 (:devices handlers))]


         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id]] (:device handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/metadata/"] (:sensor-metadata handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/metadata/"] (bidi/->Redirect 301 (:sensor-metadata handlers))]

         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/measurements/"] (:measurements handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/measurements"] (bidi/->Redirect 301 (:measurements handlers))]

         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/measurements/" :reading-type] (:measurement-slice handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/measurements/" :reading-type "/" :timestamp] (:measurement handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/hourly_rollups/" :reading-type] (:hourly-rollups handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/daily_rollups/" :reading-type] (:daily-rollups handlers)]
         ]
        wrap-cookies)])

(defrecord AmonApi [context]
  component/Lifecycle
  (start [this]
    (log/info "AmonApi starting")
    (if-let [store (get-in this [:store])]
      (let [handlers  (make-handlers store (get-in this [:queue :queue]))]
        (assoc this
          :handlers handlers
          :routes (make-routes handlers)))
      (throw (ex-info "No store!" {:this this}))))
  (stop [this] this)

  modular.bidi/BidiRoutesContributor
  (routes [this] (:routes this))
 (context [this] context))

(defn new-amon-api [context]
  (->AmonApi context))
