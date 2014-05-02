(ns kixi.hecuba.main
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
   [ajax.core :refer (GET POST)]
   [clojure.string :as str]
   [kixi.hecuba.navigation :as nav]
   [kixi.hecuba.widgets.datetimepicker :as dtpicker]
   [kixi.hecuba.tabs.programmes :as programmes]
   [kixi.hecuba.widgets.chart :as chart]
   [kixi.hecuba.common :refer (index-of map-replace find-first interval)]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.model :refer (app-model)]
   [kixi.hecuba.sensor :as sensor]))

(enable-console-print!)

;; TODO
(om/root programmes/programmes-tab
         app-model
         {:target (.getElementById js/document "hecuba-tabs")
          :shared {:history (history/new-history [:programme :project :property :device :sensor :measurement])
                   :clear-tables (chan)}})
