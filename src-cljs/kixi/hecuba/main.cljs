(ns kixi.hecuba.main
  (:require
   [om.core :as om :include-macros true]
   [kixi.hecuba.tabs.hierarchy :as hierarchy]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.model :refer (app-model)]
   [ankha.core :as ankha]))

(om/root hierarchy/main-tab
         app-model
         {:target (.getElementById js/document "hecuba-tabs")
          :shared {:history (history/new-history [:programmes :projects :properties :sensors :measurements])}})

;; Useful for debugging in dev
;; (om/root ankha/inspector app-model {:target (.getElementById js/document "ankha")})

