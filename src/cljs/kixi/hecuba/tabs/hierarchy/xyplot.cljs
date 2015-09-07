(ns kixi.hecuba.tabs.hierarchy.xyplot
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [clojure.string :as str]
            [cljs.core.async :refer [<! >! chan put!]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [ajax.core :refer [GET]]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.core   :as t]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.common :refer (log) :as common]
            [kixi.hecuba.tabs.hierarchy.data :as data]
            [kixi.hecuba.model :refer (app-model)]))

(defn xyplot-div [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:event-chan (chan)})
    om/IWillMount
    (will-mount [_])
    om/IRenderState
    (render-state [_ {:keys [event-chan]}]
      (html
       [:div [:h3 "XY Plot"]]))))
