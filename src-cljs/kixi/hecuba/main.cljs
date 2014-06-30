(ns kixi.hecuba.main
  (:require
   [om.core :as om :include-macros true]
   [kixi.hecuba.tabs.programmes :as programmes]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.model :refer (app-model)]
   [ankha.core :as ankha]))

;; (enable-console-print!)

(om/root programmes/programmes-tab
         app-model
         {:target (.getElementById js/document "hecuba-tabs")
          :shared {:history (history/new-history [:programmes :projects :properties :devices :sensors :measurements])}})

;; Useful for debugging in dev
;; (om/root ankha/inspector app-model {:target (.getElementById js/document "ankha")})

