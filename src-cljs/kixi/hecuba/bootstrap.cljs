(ns kixi.hecuba.bootstrap
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [put!]]
            [kixi.hecuba.tabs.slugs :as slugs]))


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

(defn panel
  "A bootstrap panel"
  ([background class title data padding]
     [:div {:class (str  "panel " class)}
      [:div {:class "panel-heading" :style {:background-color background :color "black"}}
       [:div {:class "panel-title"}
        title]]
      [:div {:class "panel-body" :style {:padding (str padding "px")}} data]])
  ([background class title data]
     [:div {:class (str  "panel " class)}
      [:div {:class "panel-heading" :style {:background-color background :color "black"}}
       [:div {:class "panel-title"}
        title]]
      [:div {:class "panel-body"} data]])
  ([class title data]
     [:div {:class (str  "panel " class)}
      [:div {:class "panel-heading"}
       [:div {:class "panel-title"}
        title]]
      [:div {:class "panel-body"} data]])
  ([title data]
     (panel "panel-info" title data)))

(defn error-row [data]
  [:div.row
   [:div.col-md-12.text-center
    [:p.lead {:style {:padding-top 30}}
     "There has been an error. Please contact " [:a {:href "mailto:support@mastodonc.com"} "support@mastodonc.com"]]
    [:p "Error Code: " (:error-status data) " Message: " (:error-text data)]]])

(defn no-data-row [data]
  [:div.row [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "No data available for this selection."]]])

(defn fetching-row [data]
  [:div.row [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "Fetching data for selection." ]]])

(defn handle-change [owner keys e]
  (let [value (.-value (.-target e))]
    (om/set-state! owner keys value)))

(defn alert [cursor owner]
  (om/component
   (let [{:keys [status text class]} cursor]
     (html
      [:div {:style {:display (if status "block" "none")}}
       [:div {:class class}
        [:button.close {:type "button"
                        :onClick (fn [_] (om/update! cursor :status false))}
         [:span {:class "fa fa-times"}]]
        text]]))))

(defn text-input-control [data owner keys label & required]
  (let [id (str key)]
    [:div.form-group
     [:label {:for id} label]
     [:div {:class (str (if required "required " ""))}
      [:input {:defaultValue (get-in data keys "")
               :on-change #(handle-change owner keys %1)
               :class "form-control"
               :type "text"}]]]))

(defn text-area-control [data owner keys label]
  [:div.form-group
   [:label {:for label} label]
   [:textarea.form-control {:id label
                            :name label
                            :defaultValue (get-in data keys "")
                            :on-change #(handle-change owner keys %1)
                            :rows 2}]])

(defn static-text [data keys label]
  [:div.form-group
   [:label {:for label} label]
   [:p {:class "form-control-static"} (get-in data keys "")]])

(defn checkbox [data owner keys label]
  [:div.checkbox
   [:label
    [:input {:type "checkbox"
             :defaultChecked (let [checked? (get-in data keys)]
                               (or (= checked? "true") (= checked? true)))
             :on-change #(om/set-state! owner keys (.-checked (.-target %)))}
     label]]])

(defn address-control [data owner keys]
  [:div
   [:div.form-group
    [:label.control-label.col-md-2 {:for "address_street"} "Street Address"]
    [:div.col-md-10
     [:input {:defaultValue (get-in data (conj keys :address_street) "")
              :on-change #(handle-change owner (conj keys :address_street) %1)
              :class "form-control"
              :type "text"
              :id "address_street"}]]]
   [:div.form-group
    [:label.control-label.col-md-2 {:for "address_street_two"} "Street Address 2"]
    [:div.col-md-10
     [:input {:defaultValue (get-in data (conj keys :address_street_two) "")
              :on-change #(handle-change owner (conj keys :address_street_two) %1)
              :class "form-control"
              :type "text"
              :id "address_street_two"}]]]
   [:div.form-group
    [:label.control-label.col-md-2 {:for "address_city"} "City"]
    [:div.col-md-10
     [:input {:defaultValue (get-in data (conj keys :address_city) "")
              :on-change #(handle-change owner (conj keys :address_city)  %1)
              :class "form-control"
              :type "text"
              :id "address_city"}]]]
   [:div.form-group
    [:label.control-label.col-md-2 {:for "address_code"} "Postal Code"]
    [:div.col-md-10
     [:input {:defaultValue (get-in data (conj keys :address_code) "")
              :on-change #(handle-change owner (conj keys :address_code) %1)
              :class "form-control"
              :type "text"
              :id "address_code"}]]]
   [:div.form-group
    [:label.control-label.col-md-2 {:for "address_country"} "Country"]
    [:div.col-md-10
     [:input {:defaultValue (get-in data (conj keys :address_country) "")
              :on-change #(handle-change owner (conj keys :address_country) %1)
              :class "form-control"
              :type "text"
              :id "address_country"}]]]])

(defn address-static-text [property_data]
  [:div.form-group
   [:label {:for "address"} "Address"]
   [:p {:class "form-control-static"} (slugs/postal-address-html property_data)]])

(defn sorting-th [sort-spec th-chan label header-key]
  (let [ {:keys [sort-key sort-asc]} sort-spec]
    [:th {:onClick (fn [_ _] (put! th-chan header-key))}
     (str label " ")
     (if (= sort-key header-key)
       (if sort-asc
         [:i.fa.fa-sort-asc]
         [:i.fa.fa-sort-desc]))]))
