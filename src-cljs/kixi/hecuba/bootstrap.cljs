(ns kixi.hecuba.bootstrap
      (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]))


(defn checkbox [v cursor on-click]
  (dom/div 
   #js {:className "checkbox"
        ;; TODO hack alert!!
        :style {:margin 0}}
   (dom/label #js {:className "checkbox-inline"}
              (dom/input #js {:type "checkbox"
                              :value v
                              :onClick on-click}))))
(defn button
  ([text kind dismiss]
     (button text kind dismiss nil))
  ([text kind dismiss on-click]
     (dom/button #js {:type "button"
                      :className (str "btn btn-" kind)
                      :data-dismiss "modal"
                      :onClick on-click}
                 text)))

(defn primary-button
  ([text dismiss]
     (primary-button text dismiss nil))
  ([text dismiss on-click]
     (button text "primary" dismiss on-click)))

(defn default-button
  ([text dismiss]
     (default-button text dismiss nil))
  ([text dismiss on-click]
     (button text "default" dismiss on-click)))

