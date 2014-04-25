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

(defn select [opts]
  (apply dom/select #js {:className "form-control"}
         (map #(dom/option nil %) opts)))

(defn text-field [on-blur]
  (dom/input
   #js {:className "form-control"
        :type "text"
        :onBlur on-blur}))

(defn text-field-with-button [{:keys [onBlur opts]}]
  (dom/div #js {:className "input-group"}
           (dom/div #js {:className "input-group-btn"}
                    (dom/button #js {:type "button"
                                     :className "btn btn-default dropdown-toggle"
                                     :data-toggle "dropdown"}
                                "Type"
                                (dom/span #js {:className "caret"}))
                    (apply dom/ul #js {:className "dropdown-menu"}
                           (map #(dom/li nil (dom/a #js {:href "#"} %)) opts)))
           (dom/input #js {:type "text"
                           :className "form-control"
                           :onBlur onBlur})))

(defn with-control-label  [label el]
  (dom/div #js {:className "col-lg-12 form-group"}
           (dom/label #js {:className "col-lg-2 control-label"} label)
           (dom/div #js {:className "col-lg-10"} el)))

(defn form-horizontal-with-validation [& xs]
  (apply dom/form
         #js {:className "form-horizontal has-validation"}
         xs))

(defn form-group-with-feedback [ & xs]
  (apply dom/div #js {:className "form-group has-feedback"}
         xs))

(defn dropdown [id opts]
  (dom/div #js {:className "dropdown"}
           (dom/button #js {:id id :className "btn dropdown-toggle sr-only"
                            :type "button" :data-toggle "dropdown"}
                       "Dropdown"
                       (dom/span #js {:className "caret"}))
           (apply dom/ul #js {:className "dropdown-menu"
                              :role "menu"
                              :aria-labelledby "dropdownMenu1"}
                  (map #(dom/li nil (dom/a #js {:role "menuitem" :tabIndex "-1" :href "#"} %)) opts)
  )))

(defn accordion-panel [href id title component]
  (dom/div #js {:className "panel panel-default"}
           (dom/div #js {:className "panel-heading"}
                    (dom/h3 #js {:className "panel-title"}
                            (dom/a #js {:data-toggle "collapse" :data-parent "#accordion" :href href} title)))
           (dom/div #js {:id id :className "panel-collapse collapse in"}
                    (dom/div #js {:className "panel-body"}
                             component))))
