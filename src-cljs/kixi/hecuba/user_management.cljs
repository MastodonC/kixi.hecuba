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
   [kixi.hecuba.tabs.slugs :as slugs]
   [cljs.reader :as reader]
   [ajax.core :refer (GET)]))

(def app-model
  (atom
   {:programmes {:data []}
    :projects {:data []}
    :user {:data []
           :fetching false
           :typed nil
           :role nil
           :programmes {}
           :projects {}
           :class ""}
    :selected {}
    :alert {}
    :editing true}))

(def user-roles [{:display "Super Admin" :role :kixi.hecuba.security/super-admin :value "super-admin"}
                 {:display "Admin" :role :kixi.hecuba.security/admin :value "admin"}
                 {:display "User" :role :kixi.hecuba.security/user :value "user"}
                 {:display "None" :role "none" :value "none"}])

(def programmes-table-roles [{:display "Programme Manager" :role :kixi.hecuba.security/programme-manager :value "programme-manager"}
                             {:display "User" :role :kixi.hecuba.security/user :value "user"}
                             {:display "None" :role "none" :value "none"}])

(def projects-table-roles [{:display "Project Manager" :role :kixi.hecuba.security/project-manager :value "project-manager"}
                           {:display "User" :role :kixi.hecuba.security/user :value "user"}
                           {:display "None" :role "none" :value "none"}])

(defn dropdown [{:keys [default items]} owner {:keys [event-chan id path]}]
  (om/component
   (html
    [:select.form-control
     {:default-value default :id id
      :on-change (fn [e]
                   (let [v    (.-value (aget (.-options (.-target e)) (.-selectedIndex (.-options (.-target e)))))]
                     (put! event-chan {:event :dropdown :value {:path path
                                                                :value (if-not (= v "none") v nil)}})))}
     (for [item items]
       [:option {:value (:role item)} (:display item)])])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Save form

(defn post-form [data username selected]
  (let [{:keys [role programmes projects]} selected]
    (let [existing-data (dissoc (-> @data :user) :class :username :data :fetching :typed)
          selected-role (:role selected)
          new-data      (-> (into {} (filter #(seq (val %)) (select-keys selected [:programmes :projects])))
                            (cond-> (seq role) (assoc :role role)))
          data-to-post  (common/deep-merge existing-data new-data)
          parsed (-> data-to-post
                     (assoc :programmes (into {} (remove #(nil? (val %)) (:programmes data-to-post))))
                     (assoc :projects (into {} (remove #(nil? (val %)) (:projects data-to-post)))))]
      (common/put-resource (str "/4/usernames/" username)
                           {:username username
                            :data parsed}
                           (fn [response]
                             (om/update! data [:user :role] (:role parsed))
                             (om/update! data [:user :programmes] (:programmes parsed))
                             (om/update! data [:user :projects] (:projects parsed))
                             (om/update! data :editing false))
                           (fn [{:keys [status status-text]}]
                             (om/update! data :alert {:status true
                                                      :class "alert alert-danger"
                                                      :text status-text}))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Username

(defn enrich-projects [programmes projects]
  (let [lookup (into {} (map #(hash-map (:programme_id %) (:name %)) programmes))]
    (mapv #(assoc % :programme_name (get lookup (:programme_id %))) projects)))

(defn fetch-projects [data]
  (GET (str "/4/projects/")
       {:handler (fn [response]
                   (om/update! data [:projects :data] (->> response
                                                           (filter :editable)
                                                           (mapv slugs/slugify-project)
                                                           (enrich-projects (-> @data :programmes :data))))
                   (om/update! data [:projects :fetching] (if (empty? (-> @data :projects :data))
                                                            :no-data :has-data)))
        :error-handler (fn [{:keys [status status-text]}]
                         (om/update! data :alert {:status true
                                                  :class "alert alert-danger"
                                                  :text status-text})
                         (om/update! data [:projects :fetching] :error))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

(defn fetch-programmes [data]
  (GET (str "/4/programmes/")
       {:handler (fn [response]
                   (om/update! data [:programmes :data] (->> response
                                                             (filter :editable)
                                                             (mapv slugs/slugify-programme)))
                   (om/update! data [:programmes :fetching] (if (empty? (-> @data :programmes :data))
                                                              :no-data :has-data)))
        :error-handler (fn [{:keys [status status-text]}]
                         (om/update! data :alert {:status true
                                                  :class "alert alert-danger"
                                                  :text status-text})
                         (om/update! data [:programmes :fetching] :error))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

(defn load-user-data
  "Initialises drop down, programmes and projects with user data."
  [user found-user event-chan]
  (let [{:keys [programmes projects role]} (:data found-user)]
    (om/update! user :class "has-success")
    (om/update! user :username (:username found-user))
    (om/update! user :role role)
    (om/update! user :programmes programmes)
    (om/update! user :projects projects)
    (put! event-chan {:event :fetch-data})))

(defn find-username
  "Check if typed username exists and load user data if it does. Display error alert otherwise."
  [user event-chan]
  (let [username (-> @user :typed)]
    (when username
      (let [found-user (first (filter #(= (:username %) username) (-> @user :data)))]
        (if found-user
          (load-user-data user found-user event-chan)
          (do
            (om/update! user :class "has-error")
            (put! event-chan {:event :clear-form})
            (put! event-chan {:event :alert :value {:status true
                                                    :class "alert alert-danger"
                                                    :text "Username not found."}})))))))

(defn username-selection-form [user owner]
 (reify
   om/IRenderState
   (render-state [_ {:keys [event-chan]}]
     (html
      (let [typed-username (-> user :typed)
            class          (-> user :class)]
        [:div.form-inline {:role "form"}
         [:div {:class (str "col-md-10 form-group " class)}
          [:input {:type "text"
                   :style {:width "100%"}
                   :class "form-control"
                   :value typed-username
                   :on-click (fn [e]
                               (put! event-chan {:event :alert :value {:status false}})
                               (om/update! user :class ""))
                   :on-change (fn [e] (om/update! user :typed (.-value (.-target e)))
                                (when (empty? (-> @user :typed))
                                  (put! event-chan {:event :alert :value {:status false}})
                                  (om/update! user :class "")
                                  (put! event-chan {:event :clear-form})))
                   :on-key-press (fn [e] (when (= (.-keyCode e) 13)
                                           (put! event-chan {:event :alert :value {:status false}})
                                           (find-username user event-chan)))}]]
         [:button {:type "button" :class "col-md-2 btn btn-primary"
                   :on-click (fn [e] (find-username user event-chan))} "Find user"]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Projects

(defn project-row [project owner {:keys [event-chan]}]
  (reify
    om/IRender
    (render [_]
      (html
       (let [{:keys [programme_name project_id name role]} project]
         [:tr
          [:td {:style {:width "60%"}} (str programme_name " : " name)]
          [:td {:style {:width "40%"}}
           (om/build dropdown {:default role
                               :items projects-table-roles}
                    {:opts {:event-chan event-chan
                            :id (str project_id "-role")
                            :path [:projects project_id]}})]])))))


(defn projects-table [{:keys [projects existing-selections]} owner {:keys [event-chan]}]
  (reify
    om/IRender
    (render [_]
      (html
       [:div.row-xs-height
        [:div
         [:table {:className "table table-striped borderless"}
          [:thead
           [:tr [:th "Name"] [:th "Role"]]]
          [:tbody
           (om/build-all project-row (sort-by :project_id
                                              (mapv #(assoc % :role (get-in existing-selections [(:project_id %)] "none"))
                                                    projects))
                         {:key :project_id :opts {:event-chan event-chan}})]]]]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Programmes

(defn programmes-row [programme owner {:keys [event-chan]}]
  (reify
    om/IRender
    (render [_]
      (html
       (let [{:keys [programme_id name role]} programme]
         [:tr
          [:td {:style {:width "60%"}} name]
          [:td {:style {:width "40%"}}
          (om/build dropdown {:default role
                              :items programmes-table-roles}
                    {:opts {:event-chan event-chan
                            :id (str programme_id "-role")
                            :path [:programmes programme_id]}})]])))))

(defn programmes-table [{:keys [programmes existing-selections]} owner {:keys [event-chan]}]
  (reify
    om/IRender
    (render [_]
      (html
       [:div.row-xs-height
        [:table {:class "table table-striped borderless"}
         [:thead
          [:tr
           [:th "Name"]
           [:th "Role"]]]
         [:tbody
          (om/build-all programmes-row
                        (sort-by :name (mapv #(assoc % :role (get-in existing-selections [(:programme_id %)] "none"))
                                             programmes)) {:key :programme_id :opts {:event-chan event-chan}})]]]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entire view of the page

(defn role-display-from-value [value roles-lookup]
  (-> (filter #(= (:role %) value) roles-lookup) first :display))

(defn static-text [cursor owner]
  (om/component
   (html
    (let [{:keys [label data]} cursor]
      [:div.form-group
       [:label.control-label.col-md-2 {:for label} label]
       [:p {:class "form-control-static col-md-10"} data]]))))

(defn fetch-data [data]
  (fetch-programmes data)
  (fetch-projects data))

(defn clear-form
  "Reset app-state when user deletes their selection."
  [data]
  (om/update! data [:programmes :data] [])
  (om/update! data [:projects :data] [])
  (om/update! data :selected nil)
  (om/update! data [:user :role] nil)
  (om/update! data [:user :class] "")
  (om/update! data [:user :username] nil)
  (om/update! data [:user :projects] {})
  (om/update! data [:user :programmes] {}))

(defn user-management-view [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:event-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (data/fetch-usernames data)
      (GET "/4/whoami/"
           {:handler (fn [x] (om/update! data :editor x))
            :error-handler (fn [status status-text] (om/update! data :alert {:status true
                                                                             :class "alert alert-danger"
                                                                             :text status-text}))
            :response-format :json
            :keywords? true})
      (go-loop []
        (let [{:keys [event-chan]}  (om/get-state owner)
              {:keys [event value]} (<! event-chan)]
          (cond
           (= event :dropdown) (let [{:keys [path value]} value]
                                 (om/update! data (into [:selected] path) (reader/read-string value)))
           (= event :alert) (om/update! data :alert value)
           (= event :fetch-data) (fetch-data data)
           (= event :clear-form) (clear-form data)))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [event-chan]}]
      (let [role (-> data :user :role)
            {:keys [username projects programmes]} (-> data :user)
            selected   (-> data :selected)
            editing    (-> data :editing)
            editor     (-> data :editor)]
        (html
         [:div.col-md-12 {:style {:padding-top "10px"}}
          (if editing
            [:div
             ;; Alert
             [:div.col-md-12 [:div {:style {:padding-top "10px"}} (om/build bs/alert (:alert data))]]
             ;; Username
             [:div
              [:div.col-md-6
               (om/build username-selection-form (:user data) {:init-state {:event-chan event-chan}})]
              (when (seq username)
                [:button.btn.btn-success {:on-click #(post-form data username @selected)} "Save"])]

             ;; Admin Role - only visible to super admins
             (when (and (= "super-admin" (:role editor)) (not (nil? role)))
               [:div.form-group.col-md-6 {:style {:padding-top "10px"}}
                [:label.control-label.col-md-2 {:for "role-selection"} "Role:"]
                [:div.col-md-6
                 [:div {:style {:width "100%"}}
                  (om/build dropdown {:default role
                                      :items user-roles}
                            {:opts {:event-chan event-chan
                                    :id "role-selection"
                                    :path [:role]}})]]])
             ;; Show which user is being edited
             (when (seq username)
               [:div.col-md-12 [:h4 "Editing: "] username])

             ;; Programmes
             [:div.col-md-12 {:style {:padding-top "15px"}}
              [:h4 "All Programmes"]
              (om/build programmes-table {:programmes (-> data :programmes :data)
                                          :existing-selections (-> data :user :programmes)}
                        {:opts {:event-chan event-chan}})]
             ;; Projects
             [:div.col-md-12
              [:h4 "All Projects"]
              (om/build projects-table {:projects (-> data :projects :data)
                                        :existing-selections (-> data :user :projects)}
                        {:opts {:event-chan event-chan}})]]

            [:div.col-md-12
             [:div.col-md-12 [:div.alert.alert-success {:role "alert"} "User updated successfully."]
              [:a {:class "btn btn-primary" :href "/user"} "Find another user"]]
             ;; Static display of data
             [:div.col-md-12 {:style {:padding-top "15px"}}
              [:h4 "Saved user:"]
              [:form.form-horizontal {:role "form"}
               (om/build static-text {:label "User Name"
                                      :data username})
               (om/build static-text {:label "Role"
                                      :data (role-display-from-value role user-roles)})
               [:h5 "All Programmes"]
               (for [[programme_id role] programmes]
                 (let [{:keys [name]} (-> (filter #(= (:programme_id %) programme_id) (-> data :programmes :data))
                                                       first)]
                   (om/build static-text {:label name
                                          :data (role-display-from-value role programmes-table-roles)})))
               [:h5 "All Projects"]
               (for [[project_id role] projects]
                 (let [{:keys [name]} (-> (filter #(= (:project_id %) project_id) (-> data :projects :data))
                                                     first)]
                   (om/build static-text {:label name
                                          :data (role-display-from-value role projects-table-roles)})))]]])])))))


(when-let [user-mgmt (.getElementById js/document "user-management")]
  (om/root user-management-view
           app-model
           {:target user-mgmt}))
