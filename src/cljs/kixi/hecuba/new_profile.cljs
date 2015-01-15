(ns kixi.hecuba.new-profile
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [clojure.string :as string]
            [kixi.hecuba.common :as common :refer (log)]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.profiles.form :as form]
            [kixi.hecuba.profiles.app-model :as model]))

(defn post-new-profile [cursor profile]
  (let [[_ _ entity_id] (string/split js/window.location.pathname #"/")
        url             (str "/4/entities/" entity_id "/profiles/")]
    (common/post-resource url (-> profile
                                  (assoc :entity_id entity_id)
                                  form/fix-timestamp
                                  (dissoc :alert))
                          (fn [response]
                            (om/update! cursor :alert {:status true
                                                       :class "alert alert-success"
                                                       :text "Profile added successfully."})
                            (.back js/history))
                          (fn [{:keys [status status-text]}]
                            (om/update! cursor :alert {:status true
                                                       :class "alert alert-danger"
                                                       :text status-text})))))

(defn post-if-valid [cursor profile]
  (if (form/valid? profile)
    (post-new-profile cursor profile)
    (om/update! cursor :alert {:status true
                               :class "alert alert-danger"
                               :text "Please enter event type and make sure energy values are valid numbers."})))

(defn new-profile-form [cursor owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div
        [:div.col-md-12
         [:h3 "Adding new profile"]
         [:div.btn-toolbar {:style {:padding-bottom "15px"}}
          [:button {:type "button"
                    :class "btn btn-success"
                    :onClick (fn [_] (let [profile (form/parse @cursor)]
                                       (post-if-valid cursor profile)))}
           "Save"]
          [:button {:type "button"
                    :class "btn btn-danger"
                    :onClick (fn [_] (.back js/history))}
           "Cancel"]]]
        (om/build form/profile-forms cursor)]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entire view of new profile page

(defn new-profile-view [data owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div {:style {:padding-top "10px"}}
        (om/build new-profile-form (:new-profile data))]))))

(when-let [new-profile (.getElementById js/document "new-profile")]
  (om/root new-profile-view
           model/app-model
           {:target new-profile}))
