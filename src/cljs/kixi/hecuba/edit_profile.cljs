(ns kixi.hecuba.edit-profile
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.coerce :as tc]
            [cljs-time.format :as tf]
            [clojure.string :as string]
            [kixi.hecuba.common :as common :refer (log)]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.profiles.app-model :as model]
            [kixi.hecuba.profiles.panels :as panels]
            [kixi.hecuba.widgets.datetimepicker :as dtpicker]
            [kixi.hecuba.profiles.form :as form]
            [kixi.hecuba.tabs.hierarchy.data :as data]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entire view of edit profile page

(defn put-edited-profile [cursor owner profile]
  (let [[_ _ entity_id profile_id] (string/split js/window.location.pathname #"/")]
    (common/put-resource (str "/4/entities/" entity_id "/profiles/" profile_id)
                         (-> profile
                             form/fix-timestamp
                             (assoc :entity_id entity_id))
                         (fn [_] (om/set-state! owner :alert {:status true
                                                              :class "alert alert-success"
                                                              :text "Profile edited successfully."})
                           (.back js/history))
                         (fn [{:keys [status status-text]}]
                            (om/set-state! owner :alert {:status true
                                                         :class "alert alert-danger"
                                                         :text status-text})))))

(defn put-if-valid [cursor owner profile]
  (if (form/valid? profile)
    (put-edited-profile cursor owner profile)
    (om/set-state! owner :alert {:status true
                                 :class "alert alert-danger"
                                 :text "Please enter event type and make sure energy values are valid numbers."})))

(defn edit-profile-form [cursor owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div
        [:div.col-md-12
         [:h3 "Editing profile"]
         [:div.btn-toolbar {:style {:padding-bottom "15px"}}
          [:button {:type "button"
                    :class "btn btn-success"
                    :onClick (fn [_] (let [profile (form/parse @cursor)]
                                       (put-if-valid cursor owner profile)))}
           "Save"]
          [:button {:type "button"
                    :class "btn btn-danger"
                    :onClick (fn [_] (.back js/history))}
           "Cancel"]]]
        (om/build form/profile-forms cursor)]))))

(defn parse [timestamp]
  (let [raw       (tf/parse (tf/formatter "yyyy-MM-dd'T'HH:mm:ssZ") timestamp)
        formatted (tf/unparse (tf/formatter "yyyy-MM-dd") raw)]
    {:_value formatted}))

(defn parse-timestamp [profile]
  (if (-> profile :timestamp seq)
    (update-in profile [:timestamp] parse)
    profile))

(defn edit-profile-view [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [[_ _ entity_id profile_id] (string/split js/window.location.pathname #"/")]
        (data/fetch-profile data :edited-profile entity_id profile_id (fn [profile]
                                                                        (-> (common/deep-merge model/profile-schema
                                                                                               (form/unparse profile))
                                                                            parse-timestamp)))))
    om/IRender
    (render [_]
      (html
       [:div {:style {:padding-top "10px"}}
        (om/build edit-profile-form (:edited-profile data))]))))

(when-let [edit-profile (.getElementById js/document "edit-profile")]
  (om/root edit-profile-view
           model/app-model
           {:target edit-profile}))
