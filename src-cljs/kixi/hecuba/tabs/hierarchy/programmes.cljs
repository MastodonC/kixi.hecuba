(ns kixi.hecuba.tabs.hierarchy.programmes
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put!]]
   [clojure.string :as str]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.bootstrap :refer (text-input-control text-area-control static-text static-text checkbox alert) :as bs]
   [kixi.hecuba.common :refer (log) :as common]
   [kixi.hecuba.tabs.hierarchy.data :refer (fetch-programmes)]
   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; programmes
(defn error-handler [programmes]
  (fn [{:keys [status status-text]}]
    (om/update! programmes :alert {:status true
                                   :class "alert alert-danger"
                                   :text status-text})))

(defn post-new-programme [programmes refresh-chan owner programme]
  (common/post-resource (str "/4/programmes/")
                        (-> programme
                            (assoc :created_at (common/now->str))
                            (cond-> (:public_access programme) (update-in [:public_access] str)))
                        (fn [_]
                          (put! refresh-chan {:event :programmes})
                          (om/update! programmes :adding-programme false))
                        (error-handler programmes)))

(defn put-edited-programme [programmes refresh-chan owner url programme]
  (common/put-resource url
                       (assoc programme :updated_at (common/now->str))
                       (fn [_]
                         (put! refresh-chan {:event :programmes})
                         (om/update! programmes :editing false))
                       (error-handler programmes)))

(defn valid-programme? [programme programmes]
  (let [programme-name (:name programme)]
    (and (seq programme-name) (empty? (filter #(= (:name %) programme-name) programmes)))))

(defn programme-add-form [programmes]
  (fn [cursor owner]
    (om/component
     (let [refresh-chan (om/get-shared owner :refresh)]
       (html
        [:div
         [:h3 "Add new programme"]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-12
           [:div.form-group
            [:div.btn-toolbar
             [:button.btn.btn-success {:type "button"
                                       :onClick (fn [_] (let [programme (om/get-state owner :programme)]
                                                          (om/update! cursor :programme programme)
                                                          (if (valid-programme? programme (:data @programmes))
                                                            (post-new-programme programmes refresh-chan owner programme)
                                                            (om/update! programmes :alert {:status true
                                                                                           :class "alert alert-danger"
                                                                                           :text "Please enter name of the programme"}))))}
              "Save"]
             [:button.btn.btn-danger {:type "button"
                                      :onClick (fn [_] (om/update! programmes :adding-programme false))}
              "Cancel"]]]
           (om/build alert (-> programmes :alert))
           [:div.row {:role "form"}
            [:div.col-md-4
             (text-input-control cursor owner [:programme :name] "Programme Name" true)
             (text-input-control cursor owner [:programme :leaders] "Leaders")
             (text-input-control cursor owner [:programme :lead_organisations] "Lead Organisations")
             (checkbox cursor owner [:programme :public_access] "Public Access")]
            [:div.col-md-8
             (text-area-control cursor owner [:programme :description] "Description")
             (text-area-control cursor owner [:programme :lead_page_text] "Lead Page Text")]]]]])))))

(defn programme-edit-form [programmes refresh-chan]
  (fn [cursor owner]
    (om/component
     (let [programme-id (-> programmes :selected)]
       (html
        [:div.col-md-12
         [:h1 (:name cursor)]
         [:div.col-md-12 [:form.form-horizontal {:role "form"}
                          [:div.form-group
                           [:div.btn-toolbar
                            [:button.btn.btn-success {:type "button"
                                                      :onClick (fn [_]
                                                                 (let [programme (om/get-state owner [:programme])
                                                                       url       (str "/4/programmes/" programme-id)]
                                                                   (put-edited-programme programmes
                                                                                         refresh-chan owner
                                                                                         url programme)))}
                             "Save"]
                            [:button.btn.btn-danger {:type "button"
                                                     :onClick (fn [_] (om/update! programmes :editing false))}
                             "Cancel"]]]
                          (om/build alert (-> programmes :alert))]]
         [:div.row.col-md-12 (static-text cursor [:programme :programme_id] "Programme ID")]
         [:div.row {:role "form"}
          [:div.col-md-4
           (text-input-control cursor owner [:programme :name] "Programme Name" true)
           (text-input-control cursor owner [:programme :created_at] "Created At")
           (text-input-control cursor owner [:programme :leaders] "Leaders")
           (text-input-control cursor owner [:programme :lead_organisations] "Lead Organisations")
           (checkbox cursor owner [:programme :public_access] "Public Access")]
          [:div.col-md-8
           (text-area-control cursor owner [:programme :description] "Description")
           (text-area-control cursor owner [:programme :lead_page_text] "Lead Page Text")]]])))))

(defn programme-detail [programmes editing-chan]
  (fn [cursor owner]
    (om/component
     (let [programme-id (-> programmes :selected)]
       (html
        [:div.col-md-12
         [:h1 (:name cursor)
          (when (:editable cursor)
            [:button {:type "button"
                      :title "Edit"
                      :class "btn btn-primary pull-right fa fa-pencil-square-o"
                      :onClick (fn [_] (put! editing-chan cursor))}])]
         [:div.row
          [:div.col-md-4
           (static-text cursor [:lead_organisations] "Lead Organisations")
           (static-text cursor [:leaders] "Leaders")
           (static-text cursor [:created_at] "Created At")]
          [:div.col-md-8
           (static-text cursor [:description] "Description")
           (static-text cursor [:lead_page_text] "Lead Page Text")
           (static-text cursor [:programme_id] "API Programme ID")]]])))))

(defn programmes-row [cursor owner {:keys [table-id editing-chan]}]
  (reify
    om/IRender
    (render [_]
      (html
       (let [{:keys [programme_id lead_organisations name description created_at editable selected]} cursor
             history (om/get-shared owner :history)]
         [:tr {:onClick (fn [e]
                          (let [div-id (.-id (.-target e))]
                            (when-not (= div-id (str programme_id "-edit"))
                              (history/update-token-ids! history :programmes programme_id)
                              (common/fixed-scroll-to-element "programmes-detail-div"))))
               :class (when selected "success")
               :id (str table-id "-selected")}
          [:td name]
          [:td lead_organisations]])))))

(defn programmes-table [programmes owner {:keys [editing-chan]}]
  (reify
    om/IInitState
    (init-state [_]
      {:th-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [th-chan]}           (om/get-state owner)
              {:keys [sort-key sort-asc]} (:sort-spec @programmes)
              th-click                    (<! th-chan)]
          (if (= th-click sort-key)
            (om/update! programmes :sort-spec {:sort-key th-click
                                               :sort-asc (not sort-asc)})
            (om/update! programmes :sort-spec {:sort-key th-click
                                               :sort-asc true})))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (let [table-id     "programme-table"
            history      (om/get-shared owner :history)
            th-chan      (om/get-state owner :th-chan)
            sort-spec    (:sort-spec programmes)
            {:keys [sort-key sort-asc]} sort-spec]
        (html
         [:table {:className "table table-hover"}
          [:thead
           [:tr
            (bs/sorting-th sort-spec th-chan "Name" :name)
            (bs/sorting-th sort-spec th-chan "Organisations" :lead_organisations)]]
          [:tbody
           (om/build-all programmes-row (if sort-asc
                                          (sort-by sort-key (:data programmes))
                                          (reverse (sort-by sort-key (:data programmes))))
                         {:opts {:table-id table-id
                                 :editing-chan editing-chan}
                          :key :programme_id})]])))))

(defn programmes-div [programmes owner opts]
  (reify
    om/IInitState
    (init-state [_]
      {:editing-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [editing-chan]} (om/get-state owner)
              edited-row             (<! editing-chan)]
          (om/update! programmes :editing true)
          (om/update! programmes :edited-row {:programme edited-row})
          (common/fixed-scroll-to-element "programmes-div"))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [editing-chan]}]
      (html
       (let [editing          (-> programmes :editing)
             adding-programme (-> programmes :adding-programme)
             refresh-chan     (om/get-shared owner :refresh)
             selected         (-> programmes :selected)
             selected-programme (first (filter #(= (:programme_id %) selected) (:data programmes)))]
         [:div.row#programmes-div
          [:div {:class "col-md-12"}
           [:h1 "Programmes"
            (when (and
                   (not adding-programme)
                   (not editing)
                   (-> programmes :data first :admin))
              [:button.btn.btn-primary.pull-right.fa.fa-plus
               {:type "button"
                :title "Add new"
                :onClick (fn [_] (om/update! programmes :adding-programme true))}])]
           (when adding-programme
             [:div#programmes-add-div
              (om/build (programme-add-form programmes) (:new-programme programmes))])
           (when editing
             [:div#programmes-edit-div
              (om/build (programme-edit-form programmes refresh-chan) (-> programmes :edited-row))])
           (when (not (or adding-programme editing))
             [:div#programmes-div
              (om/build programmes-table programmes {:opts {:editing-chan editing-chan}})])
           [:div#programmes-detail-div
            (when (and selected (not (or adding-programme editing)))
              (om/build (programme-detail programmes editing-chan) selected-programme))]]])))))
