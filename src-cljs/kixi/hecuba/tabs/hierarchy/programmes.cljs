(ns kixi.hecuba.tabs.hierarchy.programmes
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put!]]
   [ajax.core :refer (POST PUT)]
   [clojure.string :as str]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.common :refer (text-input-control static-text log checkbox) :as common]
   [kixi.hecuba.tabs.hierarchy.data :refer (fetch-programmes)]
   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; programmes

(defn post-resource [data method url programme-data]
  (when programme-data
    (method url {:content-type "application/json"
                 :handler #(fetch-programmes data)
                 :params programme-data})))

(defn programme-add-form [data]
  (fn [cursor owner]
    (om/component
     (html
      [:div
       [:h3 "Add new programme"]
       [:form.form-horizontal {:role "form"}
        [:div.col-md-6
         [:div.form-group
          [:div.btn-toolbar
           [:button.btn.btn-success {:type "button"
                                     :onClick (fn [_] (let [programme-data (om/get-state owner [:programme])
                                                            url            (str "/4/programmes/")]
                                                        (post-resource data POST url (-> programme-data
                                                                                         (assoc :created_at (common/now->str))))
                                                        (om/update! data [:programmes :adding-programme] false)))}
            "Save"]
           [:button.btn.btn-danger {:type "button"
                                    :onClick (fn [_] (om/update! data [:programmes :adding-programme] false))}
            "Cancel"]]]
         (text-input-control owner cursor :programme :name "Programme Name")
         (text-input-control owner cursor :programme :description "Description")
         (text-input-control owner cursor :programme :home_page_text "Home Page Text")
         (text-input-control owner cursor :programme :lead_organisations "Lead Organisations")
         (text-input-control owner cursor :programme :lead_page_text "Lead Page Text")
         (text-input-control owner cursor :programme :leaders "Leaders")
         (text-input-control owner cursor :programme :public_access "Public Access")]]]))))

(defn programme-edit-form [data]
  (fn [cursor owner]
    (om/component
     (let [programme-id (-> data :active-components :programmes)]
       (html
        [:div
         [:h3 "Editing Programme"]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-6
           [:div.form-group
            [:div.btn-toolbar
             [:button.btn.btn-success {:type "button"
                                       :onClick (fn [_] (let [programme-data (om/get-state owner [:programme])
                                                              url            (str "/4/programmes/" programme-id)]
                                                          (post-resource data PUT url (-> programme-data
                                                                                          (assoc :updated_at (common/now->str))))
                                                          (om/update! data [:programmes :editing] false)))} "Save"]
             [:button.btn.btn-danger {:type "button"
                                       :onClick (fn [_] (om/update! data [:programmes :editing] false))} "Cancel"]]]
           (static-text cursor :id "Programme ID")
           (text-input-control owner cursor :programme :created_at "Created At")
           (text-input-control owner cursor :programme :description "Description")
           (text-input-control owner cursor :programme :home_page_text "Home Page Text")
           (text-input-control owner cursor :programme :lead_organisations "Lead Organisations")
           (text-input-control owner cursor :programme :lead_page_text "Lead Page Text")
           (text-input-control owner cursor :programme :leaders "Leaders")
           (checkbox owner cursor :programme :public_access "Public Access")]]])))))

(defn programmes-row [data history programmes table-id editing-chan]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [id lead_organisations name description created_at editable]} cursor
               selected? (= (:selected programmes) id)]
           [:tr {:onClick (fn [e]
                            (let [div-id (.-id (.-target e))]
                              (when-not (= div-id (str id "-edit"))
                                (om/update! programmes :selected id)
                                (history/update-token-ids! history :programmes id)
                                (common/fixed-scroll-to-element "projects-div"))))
                 :class (when selected? "success")
                 :id (str table-id "-selected")}
            [:td [:div (when editable {:class "fa fa-pencil-square-o" :id (str id "-edit")
                                       :onClick (fn [_] (when selected? (put! editing-chan cursor)))})]]
            [:td name]
            [:td lead_organisations]
            [:td id]
            [:td created_at]]))))))

(defn programmes-table [editing-chan]
  (fn [data owner]
    (reify
      om/IRender
      (render [_]
        (let [programmes   (-> data :programmes)
              table-id     "programme-table"
              history      (om/get-shared owner :history)]
          (html
           [:table {:className "table table-hover"}
            [:thead
             [:tr
              [:th ""]
              [:th "Name"]
              [:th "Organisations"]
              [:th "ID"]
              [:th "Created At"]]]
            [:tbody
             (for [row (sort-by :name (:data programmes))]
               (om/build (programmes-row data history programmes table-id editing-chan) row))]]))))))

(defn programmes-div [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [editing-chan]} (om/get-state owner)
              edited-row             (<! editing-chan)]
          (om/update! data [:programmes :editing] true)
          (om/update! data [:programmes :edited-row] edited-row)
          (common/fixed-scroll-to-element "programmes-edit-div"))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [editing-chan]}]
      (html
       (let [editing          (-> data :programmes :editing)
             adding-programme (-> data :programmes :adding-programme)]
         [:div.row#programmes-div
          [:div {:class "col-md-12"}
           [:h1 "Programmes"]
           (when (-> data :programmes :data first :admin)
             [:div.form-group
              [:div.btn-toolbar
               [:button.btn.btn-default {:type "button"
                                         :class (str "btn btn-primary")
                                         :onClick (fn [_] (om/update! data [:programmes :adding-programme] true))}
                "Add new"]]])
           [:div {:id "programmes-add-div" :class (if adding-programme "" "hidden")}
            (om/build (programme-add-form data) nil)]
           [:div {:id "programmes-edit-div" :class (if editing "" "hidden")}
            (om/build (programme-edit-form data) (-> data :programmes :edited-row))]
           [:div {:id "programmes-div" :class (if editing "hidden" "")}
            (om/build (programmes-table editing-chan) data)]]])))))
