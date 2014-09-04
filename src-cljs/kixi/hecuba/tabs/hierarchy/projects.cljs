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

(defn error-handler [projects]
  (fn [{:keys [status status-text]}]
    (om/update! projects :alert {:status true
                                 :class "alert alert-danger"
                                 :text status-text})))
(defn valid-project? [project]
  (seq(:name project)))

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
                                        (if (valid-project? project)
                                          (post-new-project projects refresh-chan owner project programme_id)
                                          (om/update! projects :alert {:status true
                                                                            :class "alert alert-danger"
                                                                            :text "Please enter name of the project."}))))}
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
     (let [{:keys [project_id programme_id]} cursor]
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
                                    (put-edited-project projects-data refresh-chan owner project
                                                        programme_id project_id)))}
              "Save"]
             [:button {:type "button"
                       :class "btn btn-danger"
                       :onClick (fn [_] (om/update! projects-data :editing false))} "Cancel"]]]
           (om/build bs/alert (-> projects-data :alert))
           (bs/static-text cursor :project_id "Project ID")
           (bs/static-text cursor :programme_id "Programme ID")
           (bs/static-text cursor :created_at "Created At")
           (bs/text-input-control cursor owner :project :description "Description")
           (bs/text-input-control cursor owner :project :organisation "Organisation")
           (bs/text-input-control cursor owner :project :project_code "Project Code")
           (bs/text-input-control cursor owner :project :project_type "Project Type")
           (bs/text-input-control cursor owner :project :type_of "Type Of")]]])))))

(defn project-row [project owner {:keys [table-id editing-chan]}]
  (reify
    om/IRender
    (render [_]
      (html
       (let [{:keys [project_id name type_of description
                     created_at organisation project_code editable selected]} project
                     history   (om/get-shared owner :history)]
         [:tr {:onClick (fn [e]
                          (let [div-id (.-id (.-target e))]
                            (when-not (= div-id (str project_id "-edit"))
                              (history/update-token-ids! history :projects project_id)
                              (common/fixed-scroll-to-element "properties-div"))))
               :class (when selected "success")
               :id (str table-id "-selected")}
          [:td [:div (when editable {:class "fa fa-pencil-square-o"
                                     :id (str project_id "-edit")
                                     :onClick (fn [_]
                                                (when selected
                                                  (put! editing-chan project)))})]]
          [:td name]
          [:td type_of]
          [:td description]
          [:td created_at]
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
        [:tr [:th ""] [:th "Name"] [:th "Type"] [:th "Description"] [:th "Created At"] [:th "Organisation"] [:th "Project Code"]]]
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
          (common/fixed-scroll-to-element "projects-edit-div"))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [editing-chan]}]
      (let [editing          (-> projects :editing)
            adding-project   (-> projects :adding-project)
            programme_id     (-> projects :programme_id)
            can-add-projects (-> projects :can-add-projects)
            refresh-chan     (om/get-shared owner :refresh)]
        (html
         [:div.row#projects-div
          [:div {:class (str "col-md-12 " (if programme_id "" "hidden"))}
           [:h2 "Projects"]
           (when (and
                  (not editing)
                  (not adding-project)
                  can-add-projects) ;; programme is editable so allow to add new projects
             [:div.form-group
              [:div.btn-toolbar
               [:button {:type "button"
                         :class "btn btn-primary"
                         :onClick (fn [_]
                                    (om/update! projects :adding-project true))}
                "Add new"]]])
           (when adding-project
             [:div#projects-add-div
              (om/build (project-add-form projects programme_id refresh-chan) nil)])
           (when editing
             [:div#projects-edit-div
              (om/build (project-edit-form projects refresh-chan) (-> projects :edited-row))])
           (when-not (or editing adding-project)
             [:div#projects-div
              (om/build (projects-table editing-chan) projects)])]])))))
