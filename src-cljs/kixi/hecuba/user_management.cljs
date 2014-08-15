(ns kixi.hecuba.user-management
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put!]]
   [clojure.string :as str]
   [kixi.hecuba.tabs.hierarchy.data :as data]
   [kixi.hecuba.common :refer (log) :as common]
   [kixi.hecuba.bootstrap :as bs]
   [sablono.core :as html :refer-macros [html]]
   [kixi.hecuba.tabs.slugs :as slugs]))

(def app-model
  (atom
   {:programmes {:data []
                 :selected #{}}
    :projects {:data []
               :selected #{}}
    :users {:data []
            :selected nil
            :typed nil
            :class ""}
    :user-roles {:data [{:display "Super Admin" :role :kixi.hecuba.security/super-admin}
                        {:display "Admin" :role :kixi.hecuba.security/admin}
                        {:display "Programme Manager" :role :kixi.hecuba.security/programme-manager}
                        {:display "Project Manager" :role :kixi.hecuba.security/project-manager}
                        {:display "User" :role :kixi.hecuba.security/user}]
                 :selected nil}
    :alert {}
    :editing true}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Save form

(defn post-form [data username role programmes projects]
  (common/put-resource data (str "/4/usernames/" username)
                       {:username username
                        :data {:roles #{role}
                               :programmes programmes
                               :projects projects}}
                       (fn [response] (om/update! data :editing false))
                       (fn [{:keys [status status-text]}]
                         (om/update! data :alert {:status true
                                                  :class "alert alert-danger"
                                                  :text status-text}))))

(defmulti validate-selection (fn [data username role programmes projects] role))
(defmethod validate-selection :kixi.hecuba.security/super-admin [data username role programmes projects]
  (if (and username role)
    (post-form data username role programmes projects)
    (om/update! data :alert {:status true
                             :class "alert alert-danger"
                             :text "Please select username and role."})))

(defmethod validate-selection :kixi.hecuba.security/admin [data username role programmes projects]
  (if (and username role)
    (post-form data username role programmes projects)
    (om/update! data :alert {:status true
                             :class "alert alert-danger"
                             :text "Please select username and role."})))

(defmethod validate-selection :kixi.hecuba.security/programme-manager [data username role programmes projects]
  (if (and username role (seq programmes))
    (post-form data username role programmes projects)
    (om/update! data :alert {:status true
                             :class "alert alert-danger"
                             :text "Please select username and role and programme(s)."})))

(defmethod validate-selection :kixi.hecuba.security/project-manager [data username role programmes projects]
  (if (and username role (seq programmes) (seq projects))
    (post-form data username role programmes projects)
    (om/update! data :alert {:status true
                             :class "alert alert-danger"
                             :text "Please select username and role, programme(s) and project(s)."})))

(defmethod validate-selection :kixi.hecuba.security/user [data username role programmes projects]
  (if (and username role (seq programmes) (seq projects))
    (post-form data username role programmes projects)
    (om/update! data :alert {:status true
                             :class "alert alert-danger"
                             :text "Please select username and role, programme(s) and project(s)."})))

(defmethod validate-selection :default [data username role programmes projects] false)

(defn save-form
  "Validates selection and posts updated user data if valid. Otherwise displays error alert."
  [data username role programme project]
  (validate-selection data username role programme project))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Username

(defn projects-for-programmes
  "Fetches all projects for programme_ids that are assigned to that user."
  [data programmes]
  (doseq [programme_id programmes]
    (data/fetch-projects programme_id data
                         (fn [response]
                           (log "Fetching projects for programme: " programme_id)
                           (om/update! data [:projects :data]
                                       (concat (-> @data :projects :data)
                                               (mapv slugs/slugify-project response)))
                           (om/update! data [:projects :fetching] (if (empty? (-> @data :projects :data))
                                                                    :no-data :has-data)))
                         (fn [{:keys [status status-text]}]
                           (om/update! data :alert {:status true
                                                    :class "alert alert-danger"
                                                    :text status-text})
                           (om/update! data [:projects :fetching] :error)))))

(defn load-user-data
  "Initialises drop down, programmes and projects with user data."
  [data user]
  (let [{:keys [programmes projects roles]} (:data user)
        displayed-role (-> (filter #(= (:role %) (first roles)) (-> @data :user-roles :data))
                           first)]
    (om/update! data [:users :class] "has-success")
    (om/update! data [:users :selected] (:username user))
    (om/update! data [:user-roles :selected] displayed-role)
    (om/update! data [:programmes :selected] programmes)
    (om/update! data [:projects :selected] projects)
    (data/fetch-programmes data)
    (projects-for-programmes data programmes)))

(defn find-username
  "Check if typed username exists and load user data if it does. Display error alert otherwise."
  [data username]
  (if username
    (let [user (first (filter #(= (:username %) username) (-> @data :users :data)))]
      (if user
        (load-user-data data user)
        (do
          (om/update! data [:users :class] "has-error")
          (om/update! data :alert {:status true
                                   :class "alert alert-danger"
                                   :text "Username not found."}))))))

(defn clear-form
  "Reset app-state when user deletes their selection."
  [data]
  (om/update! data [:projects :data] [])
  (om/update! data [:projects :selected] #{})
  (om/update! data [:user-roles :selected] nil)
  (om/update! data [:users :selected] nil)
  (om/update! data [:programmes :selected] #{}))

(defn username-selection-form [data]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [selected-username (-> cursor :selected)
               class             (-> cursor :class)]
           [:div.form-inline {:role "form"}
            [:div.col-md-6
             [:div {:class (str "form-group " class) :style {:width "100%"}}
              [:input {:type "text"
                       :style {:width "100%"}
                       :class "form-control"
                       :default-value selected-username
                       :on-change (fn [e] (om/set-state! owner :value  (.-value (.-target e)))
                                    (when (empty? (om/get-state owner :value))
                                      (clear-form data)))
                       :on-key-press (fn [e] (when (= (.-keyCode e) 13)
                                               (find-username data (om/get-state owner :value))))}]]]
            [:button {:type "button" :class "btn btn-primary"
                      :on-click (fn [e] (find-username data (om/get-state owner :value)))} "Find user"]]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Roles

(defn role-selection-form [cursor owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div.dropdown
        [:button {:class "btn btn-default dropdown-toggle"
                  :type "button"
                  :style {:width "100%"}
                  :id "dropdownMenu1"
                  :data-toggle "dropdown"}
         (get-in cursor [:selected :display] "Select role")
         [:span.caret.pull-right]]
        [:ul.dropdown-menu {:role "menu"
                            :aria-labelledby "dropdownMenu1"}
         (for [item (:data cursor)]
           [:li {:role "presentation"}
            [:a {:role "menuitem" :tab-index "-1" :href "#"
                 :onClick #(om/update! cursor :selected item)} (:display item)]])]]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Projects

(defn project-row [projects]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [project_id name type_of description
                       created_at organisation project_code editable]} cursor
               selected? (contains? (-> projects :selected) project_id)]
           [:tr {:onClick #(om/transact! projects :selected (fn [projects]
                                                              ((if selected? disj conj) projects project_id)))
                 :class (when selected? "success")}
            [:td name]
            [:td type_of]
            [:td description]
            [:td created_at]
            [:td organisation]
            [:td project_code]]))))))

(defmulti projects-table-html
  (fn [projects]
    (:fetching projects)))

(defmethod projects-table-html :fetching [projects]
  (bs/fetching-row projects))

(defmethod projects-table-html :no-data [projects]
  (bs/no-data-row projects))

(defmethod projects-table-html :error [projects]
  (bs/error-row projects))

(defmethod projects-table-html :has-data [projects]
  [:div.row
   [:div.col-md-12
    [:table {:className "table table-hover"}
     [:thead
      [:tr [:th "Name"] [:th "Type"] [:th "Description"] [:th "Created At"] [:th "Organisation"] [:th "Project Code"]]]
     [:tbody
      (om/build-all (project-row projects) (sort-by :project_id (:data projects)) {:key :project_id})]]]])

(defmethod projects-table-html :default [_]
  [:div.row [:div.col-md-12]])

(defn projects-table [cursor owner]
  (reify
    om/IRender
    (render [_]
      (html (projects-table-html cursor)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Programmes

(defn programmes-row [programmes selected-chan]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [programme_id lead_organisations name description created_at editable]} cursor
               selected? (contains? (-> programmes :selected) programme_id)]
           [:tr {:onClick (fn [_]
                            (put! selected-chan {:programme_id programme_id
                                                 :deselect? selected?}))
                 :class (when selected? "success")}
            [:td name]
            [:td lead_organisations]
            [:td programme_id]
            [:td created_at]]))))))

(defn programmes-table [selected-chan]
  (fn [data owner]
    (reify
      om/IRender
      (render [_]
        (let [programmes   (-> data :programmes)]
          (html
           [:div.row-xs-height
            [:table {:className "table table-hover"}
             [:thead
              [:tr
               [:th "Name"]
               [:th "Organisations"]
               [:th "ID"]
               [:th "Created At"]]]
             [:tbody
              (om/build-all (programmes-row programmes selected-chan)
                            (sort-by :name (:data programmes)) {:key :programme_id})]]]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entire view of the page

(defn select-programme [data programme_id]
  (om/transact! data [:programmes :selected] (fn [selected] (conj selected programme_id)))
  (data/fetch-projects programme_id data
                       (fn [response]
                         (log "Fetching projects for programme: " programme_id)
                         (om/update! data [:projects :data]
                                     (concat (-> @data :projects :data)
                                             (mapv slugs/slugify-project response)))
                         (om/update! data [:projects :fetching] (if (empty? (-> @data :projects :data))
                                                                  :no-data :has-data)))
                       (fn [{:keys [status status-text]}]
                         (om/update! data :alert {:status true
                                                  :class "alert alert-danger"
                                                  :text status-text})
                         (om/update! data [:projects :fetching] :error))))

(defn deselect-programme [data programme_id]
  (om/transact! data [:programmes :selected] (fn [selected] (disj selected programme_id)))
  (om/transact! data [:projects :data] (fn [data] (remove #(= (:programme_id %) programme_id) data))))

(defn static-text [data label]
  [:div.form-group
   [:label.control-label.col-md-2 {:for label} label]
   [:p {:class "form-control-static col-md-10"} data]])

(defn user-management-view [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:selected-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (data/fetch-usernames data)
      (go-loop []
        (let [{:keys [selected-chan]} (om/get-state owner)
              selection               (<! selected-chan)
              {:keys [programme_id deselect?]} selection]
          (when programme_id
            (if deselect?
              (deselect-programme data programme_id)
              (select-programme data programme_id))))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [role display]} (-> data :user-roles :selected)
            user       (-> data :users :selected)
            programmes (-> data :programmes :selected)
            projects   (-> data :projects :selected)
            editing    (-> data :editing)]
        (html
         [:div.col-md-10 {:style {:padding-top "10px"}}
          (if editing

            [:div
             ;; Username
             [:div
              (om/build (username-selection-form data) (:users data))]
             ;; Alert
             [:div {:style {:padding-top "10px"}} (om/build bs/alert (:alert data))]
             ;; Role
             [:div.col-md-6
              [:div {:style {:width "100%"}}
               (om/build role-selection-form (:user-roles data))]]
             [:button.btn.btn-primary {:onClick #(save-form data user role programmes projects)} "Save"]
             ;; Programmes
             [:div.col-md-12 {:style {:padding-top "15px"}}
              [:h4 "Programmes"]
              (om/build (programmes-table (:selected-chan state)) data)]
             ;; Projects
             [:div.col-md-12
              [:h4 "Projects"]
              (om/build projects-table (:projects data))]]

            [:div.col-md-10
             [:div.col-md-10 [:div.alert.alert-success {:role "alert"} "User updated successfully."]
              [:a {:class "btn btn-primary" :href "/user"} "Find another user"]]
             ;; Static display of data
             [:div.col-md-10 {:style {:padding-top "15px"}}
              [:h4 "Saved user:"]
              [:form.form-horizontal {:role "form"}
               (static-text user "User Name")
               (static-text display "Role")
               [:h5 "Programmes"]
               (for [programme programmes]
                 (let [{:keys [name programme_id]} (-> (filter #(= (:programme_id %) programme) (-> data :programmes :data))
                                                       first)]
                   (static-text programme_id name)))
               [:h5 "Projects"]
               (for [project projects]
                 (let [{:keys [name project_id]} (-> (filter #(= (:project_id %) project) (-> data :projects :data))
                                                     first)]
                   (static-text project_id name)))]]])])))))


(when-let [user-mgmt (.getElementById js/document "user-management")]
  (om/root user-management-view
           app-model
           {:target user-mgmt}))
