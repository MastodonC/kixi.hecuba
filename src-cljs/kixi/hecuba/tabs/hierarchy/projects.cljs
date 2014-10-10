(ns kixi.hecuba.tabs.hierarchy.projects
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put!]]
   [clojure.string :as str]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.bootstrap :refer (text-area-control static-text static-text-vertical) :as bs]
   [kixi.hecuba.common :refer (log) :as common]
   [kixi.hecuba.tabs.hierarchy.data :refer (fetch-projects)]
   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; projects

(defn error-handler [projects]
  (fn [{:keys [status status-text]}]
    (om/update! projects :alert {:status true
                                 :class "alert alert-danger"
                                 :text status-text})))
(defn valid-project? [project projects]
  (let [project-name (:name project)]
    (and (seq project-name) (empty? (filter #(= (:name %) project-name) projects)))))

(defn post-new-project [projects-data refresh-chan owner project programme_id]
  (let [url  (str "/4/programmes/" programme_id "/projects/")]
    (common/post-resource url
                          (assoc project :created_at (common/now->str)
                                 :programme_id programme_id)
                          (fn [_]
                            (put! refresh-chan {:event :projects})
                            (om/update! projects-data :adding-project false))
                          (error-handler projects-data))))

(defn put-edited-project [projects-data refresh-chan owner project programme_id project_id]
  (let [url (str "/4/programmes/" programme_id  "/projects/" project_id)]
    (common/put-resource url
                         (assoc project :updated_at (common/now->str))
                         (fn [_]
                           (put! refresh-chan {:event :projects})
                           (om/update! projects-data :editing false))
                         (error-handler projects-data))))

(defn delete-project [projects-data programme_id project_id history refresh-chan]
  (common/delete-resource (str "/4/programmes/" programme_id  "/projects/" project_id)
                          (fn []
                            (put! refresh-chan {:event :projects})
                            (history/update-token-ids! history :projects nil)
                            (om/update! projects-data :editing false))
                          (error-handler projects-data)))

(defn project-add-form [projects programme_id refresh-chan]
  (fn [cursor owner]
    (om/component
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
                                        (if (valid-project? project (:data @projects))
                                          (post-new-project projects refresh-chan owner project programme_id)
                                          (om/update! projects :alert {:status true
                                                                            :class "alert alert-danger"
                                                                            :text "Please enter unique name of the project."}))))}
            "Save"]
           [:button {:type "button"
                     :class "btn btn-danger"
                     :onClick (fn [_]
                                (om/update! projects :adding-project false))}
            "Cancel"]]]
         (om/build bs/alert (-> projects :alert))
         (bs/text-input-control cursor owner :project :name "Project Name" true)
         (bs/text-input-control cursor owner :project :description "Description")
         (bs/text-input-control cursor owner :project :organisation "Organisation")
         (bs/text-input-control cursor owner :project :project_code "Project Code")
         (bs/text-input-control cursor owner :project :project_type "Project Type")
         (bs/text-input-control cursor owner :project :type_of "Type Of")]]]))))

(defn project-edit-form [projects-data refresh-chan]
  (fn [cursor owner]
    (om/component
     (let [{:keys [project_id programme_id]} cursor
           history (om/get-shared owner :history)]
       (html
        [:div
         [:h3 (:name cursor)]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-12
           [:div.form-group
            [:div.btn-toolbar
             [:button {:type "button"
                       :class "btn btn-success"
                       :onClick (fn [_]
                                  (let [project (om/get-state owner [:project])]
                                    (put-edited-project projects-data refresh-chan owner project
                                                        programme_id project_id)))} "Save"]
             [:button {:type "button"
                       :class "btn btn-danger"
                       :onClick (fn [_] (om/update! projects-data :editing false))} "Cancel"]
             [:button {:type "button"
                          :class "btn btn-danger pull-right"
                          :onClick (fn [_]
                                     (delete-project projects-data programme_id project_id  history refresh-chan))}
                 "Delete Project"]]]
           (om/build bs/alert (-> projects-data :alert))
           [:div.col-md-4
            (bs/text-input-control cursor owner :project :name "Project Name")
            (bs/text-input-control cursor owner :project :organisation "Organisation")
            (bs/text-input-control cursor owner :project :project_code "Project Code")
            (bs/text-input-control cursor owner :project :project_type "Project Type")
            (bs/text-input-control cursor owner :project :type_of "Type Of")
            (bs/static-text cursor :created_at "Created At")]
           [:div.col-md-8
            (bs/text-area-control cursor owner :project :description "Description")
            (bs/static-text cursor :project_id "API Project ID")]]]])))))

(defn project-detail [projects-data editing-chan]
  (fn [cursor owner]
    (om/component
     (let [{:keys [project_id programme_id]} cursor]
       (html
        [:div.col-md-12
         [:h1 (:name cursor)
          (when (:editable cursor)
            [:button {:type "button"
                      :title "Edit"
                      :class "btn btn-primary pull-right fa fa-pencil-square-o"
                      :onClick (fn [_] (put! editing-chan cursor))}])]
         (om/build bs/alert (-> projects-data :alert))
         [:div.row
          [:div.col-md-4
           (static-text-vertical cursor :organisation "Organisation")
           (static-text-vertical cursor :project_code "Project Code")
           (static-text-vertical cursor :project_type "Project Type")
           (static-text-vertical cursor :type_of "Type Of")
           (static-text-vertical cursor :created_at "Created At")]
          [:div.col-md-8
           (static-text-vertical cursor :description "Description")
           (static-text-vertical cursor :project_id "API Project ID")]]])))))

(defn project-row [project owner {:keys [table-id editing-chan]}]
  (reify
    om/IRender
    (render [_]
      (html
       (let [{:keys [project_id name type_of description
                     created_at organisation project_code editable selected]} project
                     history   (om/get-shared owner :history)]
         [:tr {:onClick (fn [e]
                          (history/update-token-ids! history :projects project_id)
                          (common/fixed-scroll-to-element "project-detail-div"))
               :class (when selected "success")
               :id (str table-id "-selected")}
          [:td name]
          [:td type_of]
          [:td organisation]
          [:td project_code]])))))


(defmulti projects-table-html
  (fn [projects owner editing-chan]
    (:fetching projects)))

(defmethod projects-table-html :fetching [projects _ _]
  (bs/fetching-row projects))

(defmethod projects-table-html :no-data [projects _ _]
  (bs/no-data-row projects))

(defmethod projects-table-html :error [projects _ _]
  (bs/error-row projects))

(defmethod projects-table-html :has-data [projects owner editing-chan]
  (let [table-id   "projects-table"
        history    (om/get-shared owner :history)]
    [:div.row
     [:div.col-md-12
      [:table {:className "table table-hover"}
       [:thead
        [:tr [:th "Name"] [:th "Type"] [:th "Organisation"] [:th "Project Code"]]]
       [:tbody
        (om/build-all project-row (sort-by :project_id (:data projects))
                      {:opts {:table-id table-id
                              :editing-chan editing-chan}
                       :key :project_id})]]]]))

(defmethod projects-table-html :default [_ _ _]
  [:div.row [:div.col-md-12]])

(defn projects-table [editing-chan]
  (fn [projects owner]
    (reify
      om/IRender
      (render [_]
        (html (projects-table-html projects owner editing-chan))))))

(defn projects-div [projects owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [editing-chan]} (om/get-state owner)
              edited-row             (<! editing-chan)]
          (om/update! projects :editing true)
          (om/update! projects :edited-row edited-row)
          (common/fixed-scroll-to-element "projects-div"))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [editing-chan]}]
      (let [editing          (-> projects :editing)
            adding-project   (-> projects :adding-project)
            programme_id     (-> projects :programme_id)
            can-add-projects (-> projects :can-add-projects)
            refresh-chan     (om/get-shared owner :refresh)
            selected         (:selected projects)
            selected-project (first (filter #(= (:project_id %) selected) (:data projects)))]
        (html
         [:div.row#projects-div
          [:div {:class (str "col-md-12 " (if programme_id "" "hidden"))}
           [:h1 "Projects"
            (when (and
                  (not editing)
                  (not adding-project)
                  can-add-projects) ;; programme is editable so allow to add new projects
             [:button.btn.pull-right.fa.fa-plus
              {:type "button"
               :title "Add new"
               :class (str "btn btn-primary " (if editing "hidden" ""))
               :onClick (fn [_]
                          (om/update! projects :adding-project true))}])]
           [:div#projects-add-div
            (when adding-project
              (om/build (project-add-form projects programme_id refresh-chan) nil))]
           [:div#projects-edit-div
            (when editing
              (om/build (project-edit-form projects refresh-chan) (-> projects :edited-row)))]
           [:div#projects-div
            (when-not (or editing adding-project)
              (om/build (projects-table editing-chan) projects))]
           [:div#project-detail-div
            (when (and selected (not (or adding-project editing)))
              (om/build (project-detail projects editing-chan) selected-project))]]])))))
