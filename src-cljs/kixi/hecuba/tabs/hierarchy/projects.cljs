(ns kixi.hecuba.tabs.hierarchy.projects
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put!]]
   [clojure.string :as str]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.bootstrap :as bs]
   [kixi.hecuba.common :refer (log) :as common]
   [kixi.hecuba.tabs.hierarchy.data :refer (fetch-projects)]
   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; projects

(defn error-handler [owner]
  (fn [{:keys [status status-text]}]
    (om/set-state! owner :error true)
    (om/set-state! owner :http-error-response {:status status
                                               :status-text status-text})))
(defn valid-project? [project]
  (not (nil? (:name project))))

(defn post-new-project [data owner project programme_id]
  (let [url  (str "/4/programmes/" programme_id "/projects/")]
    (common/post-resource data url
                          (assoc project :created_at (common/now->str)
                                 :programme_id programme_id)
                          (fn [_] 
                            (fetch-projects programme_id data)
                            (om/update! data [:projects :adding-project] false))
                          (error-handler owner))))

(defn put-edited-project [data owner project programme_id project_id]
  (let [url (str "/4/programmes/" programme_id  "/projects/" project_id)]
    (common/put-resource data url 
                          (assoc project :updated_at (common/now->str))
                          (fn [_] 
                            (fetch-projects programme_id data)
                            (om/update! data [:projects :editing] false))
                          (error-handler owner))))

(defn project-add-form [data programme_id]
  (fn [cursor owner]
    (om/component
     (let [{:keys [status-text]} (om/get-state owner :http-error-response)
            error      (om/get-state owner :error)
            alert-body (if status-text
                         (str " Server returned status: " status-text)
                         " Please enter name of the project.")]
       (html
        [:div
         [:h3 "Add new project"]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-6
           [:div.form-group
            [:div.btn-toolbar
             [:button {:class "btn btn-success"
                       :type "button"
                       :onClick (fn [_] (let [project (om/get-state owner [:project])]
                                          (if (valid-project? project)
                                            (post-new-project data owner project programme_id)
                                            (om/set-state! owner [:error] true))))}
              "Save"]
             [:button {:type "button"
                       :class "btn btn-danger"
                       :onClick (fn [_]
                                  (om/update! data [:projects :adding-project] false))}
              "Cancel"]]]
           (bs/alert "alert alert-danger "
                  [:div [:div {:class "fa fa-exclamation-triangle"} alert-body]]
                  error
                  (str "add-project-form-failure"))
           (bs/text-input-control cursor owner :project :name "Project Name" true)
           (bs/text-input-control cursor owner :project :description "Description")
           (bs/text-input-control cursor owner :project :organisation "Organisation")
           (bs/text-input-control cursor owner :project :project_code "Project Code")
           (bs/text-input-control cursor owner :project :project_type "Project Type")
           (bs/text-input-control cursor owner :project :type_of "Type Of")]]])))))

(defn project-edit-form [data]
  (fn [cursor owner]
    (om/component
     (let [{:keys [id programme_id]} cursor
           {:keys [status-text]} (om/get-state owner :http-error-response)
            error      (om/get-state owner :error)
            alert-body (str " Server returned status: " status-text)]
       (html
        [:div
         [:h3 "Editing Project"]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-6
           [:div.form-group
            [:div.btn-toolbar
             [:button {:type "button"
                       :class "btn btn-success"
                       :onClick (fn [_]
                                  (let [project (om/get-state owner [:project])]
                                    (put-edited-project data owner project 
                                                        programme_id id)))}
              "Save"]
             [:button {:type "button"
                       :class "btn btn-danger"
                       :onClick (fn [_] (om/update! data [:projects :editing] false))} "Cancel"]]]
           (bs/alert "alert alert-danger "
                  [:div [:div {:class "fa fa-exclamation-triangle"} alert-body]]
                  error
                  (str "edit-project-form-failure"))
           (bs/static-text cursor :project_id "Project ID")
           (bs/static-text cursor :programme_id "Programme ID")
           (bs/static-text cursor :created_at "Created At")
           (bs/text-input-control cursor owner :project :description "Description")
           (bs/text-input-control cursor owner :project :organisation "Organisation")
           (bs/text-input-control cursor owner :project :project_code "Project Code")
           (bs/text-input-control cursor owner :project :project_type "Project Type")
           (bs/text-input-control cursor owner :project :type_of "Type Of")]]])))))

(defn project-row [data history projects table-id editing-chan]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [project_id name type_of description
                       created_at organisation project_code editable]} cursor
               selected? (= (:selected projects) project_id)]
           [:tr {:onClick (fn [e]
                            (let [div-id (.-id (.-target e))]
                              (when-not (= div-id (str project_id "-edit"))
                                (om/update! projects :selected project_id)
                                (history/update-token-ids! history :projects project_id)
                                (common/fixed-scroll-to-element "properties-div"))))
                 :class (when selected? "success")
                 :id (str table-id "-selected")}
            [:td [:div (when editable {:class "fa fa-pencil-square-o"
                                       :id (str project_id "-edit")
                                       :onClick (fn [_]
                                                  (when selected?
                                                    (put! editing-chan cursor)))})]]
            [:td name]
            [:td type_of]
            [:td description]
            [:td created_at]
            [:td organisation]
            [:td project_code]]))))))


(defmulti projects-table-html
  (fn [data projects owner editing-chan]
    (:fetching projects)))

(defmethod projects-table-html :fetching [projects _ _]
  (bs/fetching-row projects))

(defmethod projects-table-html :no-data [projects _ _]
  (bs/no-data-row projects))

(defmethod projects-table-html :error [projects _ _]
  (bs/error-row projects))

(defmethod projects-table-html :has-data [data projects owner editing-chan]
  (let [table-id   "projects-table"
        history    (om/get-shared owner :history)]
    [:div.row
     [:div.col-md-12
      [:table {:className "table table-hover"}
       [:thead
        [:tr [:th ""] [:th "Name"] [:th "Type"] [:th "Description"] [:th "Created At"] [:th "Organisation"] [:th "Project Code"]]]
       [:tbody
        (for [row (sort-by :project_id (:data projects))]
          (om/build (project-row data history projects table-id editing-chan) row))]]]]))

(defmethod projects-table-html :default [_ _ _]
  [:div.row [:div.col-md-12]])

(defn projects-table [data editing-chan]
  (fn [projects owner]
    (reify
      om/IRender
      (render [_]
        (html (projects-table-html data projects owner editing-chan))))))

(defn projects-div [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [editing-chan]} (om/get-state owner)
              edited-row             (<! editing-chan)]
          (om/update! data [:projects :editing] true)
          (om/update! data [:projects :edited-row] edited-row)
          (common/fixed-scroll-to-element "projects-edit-div"))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [editing-chan]}]
      (let [{:keys [programmes projects]} data
            editing        (-> data :projects :editing)
            adding-project (-> data :projects :adding-project)
            programme_id   (-> data :active-components :programmes)
            programme      (-> (filter #(= (:programme_id %) programme_id) (-> programmes :data)) first)]
        (html
         [:div.row#projects-div
          [:div {:class (str "col-md-12 " (if programme_id "" "hidden"))}
           [:h2 "Projects"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:href "/app"}
                  (common/title-for programmes)]]]
           (when (and
                  (not editing)
                  (not adding-project)
                  (:editable programme)) ;; programme is editable so allow to add new projects
             [:div.form-group
              [:div.btn-toolbar
               [:button {:type "button"
                         :class "btn btn-primary"
                         :onClick (fn [_]
                                    (om/update! data [:projects :adding-project] true))}
                "Add new"]]])
           [:div {:id "projects-add-div" :class (if adding-project "" "hidden")}
            (om/build (project-add-form data programme_id) nil)]
           [:div {:id "projects-edit-div" :class (if editing "" "hidden")}
            (om/build (project-edit-form data) (-> projects :edited-row))]
           [:div {:id "projects-div" :class (if (or editing adding-project) "hidden" "")}
            (om/build (projects-table data editing-chan) projects)]]])))))
