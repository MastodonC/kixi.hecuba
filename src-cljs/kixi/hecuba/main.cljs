(ns kixi.hecuba.main
  (:require
   [om.core :as om :include-macros true]
   [kixi.hecuba.tabs.hierarchy :as hierarchy]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.model :refer (app-model)]
   [ankha.core :as ankha]
   [cljs.core.async :refer [put! chan <!]]))

(when-let [hecuba-tabs (.getElementById js/document "hecuba-tabs")]
  (om/root hierarchy/main-tab
           app-model
           {:target hecuba-tabs
            :shared {:history (history/new-history [:programmes :projects :properties :sensors :measurements])
                     :refresh (chan)}}))

;; Useful for debugging in dev
;; (om/root ankha/inspector app-model {:target (.getElementById js/document "ankha")})
