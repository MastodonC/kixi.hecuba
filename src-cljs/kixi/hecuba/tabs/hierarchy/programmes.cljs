(ns kixi.hecuba.tabs.hierarchy.programmes
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put!]]
   [ajax.core :refer (PUT)]
   [clojure.string :as str]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.common :refer (text-input-control static-text log) :as common]
   [kixi.hecuba.tabs.hierarchy.data :refer (fetch-programmes)]
   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; programmes

(defn post-resource [data programme_id programme-data]
  (when programme-data
    (PUT  (str "/4/programmes/" programme_id)
          {:content-type "application/json"
           :handler #(fetch-programmes data)
           :params programme-data})))

(defn save-form [data owner programme_id]
  (let [programme-data (om/get-state owner [:programme])]
    (post-resource data programme_id programme-data)
    (om/update! data [:programmes :editing] false)))

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
             [:button.btn.btn-default {:type "button"
                                       :class (str "btn btn-success")
                                       :onClick (fn [_] (save-form data owner programme-id))} "Save"]
             [:button.btn.btn-default {:type "button"
                                       :class (str "btn btn-danger")
                                       :onClick (fn [_] (om/update! data [:programmes :editing] false))} "Cancel"]]]
           (static-text cursor :id "Programme ID")
           (text-input-control owner cursor :programme :created_at "Created At")
           (text-input-control owner cursor :programme :description "Description")
           (text-input-control owner cursor :programme :home_page_text "Home Page Text")
           (text-input-control owner cursor :programme :lead_organisations "Lead Organisations")
           (text-input-control owner cursor :programme :lead_page_text "Lead Page Text")
           (text-input-control owner cursor :programme :leaders "Leaders")
           (text-input-control owner cursor :programme :public_access "Public Access") ;; TODO should be boolean not text?
           (static-text cursor :updated_at "Updated At")]]])))))

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
       (let [editing (-> data :programmes :editing)]
         [:div.row#programmes-div
          [:div {:class "col-md-12"}
           [:h1 "Programmes"]
           [:div {:id "programmes-edit-div" :class (if editing "" "hidden")}
            (om/build (programme-edit-form data) (-> data :programmes :edited-row))]
           [:div {:id "programmes-div" :class (if editing "hidden" "")}
            (om/build (programmes-table editing-chan) data)]]])))))
