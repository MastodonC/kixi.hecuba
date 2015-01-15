(ns kixi.hecuba.login
  (:require
   [om.core :as om :include-macros true]
   [sablono.core :as html :refer-macros [html]]))

(def app-model
  (atom
   {}))

(defn args-map [location-str]
  (let [query-args-obj (goog.Uri.QueryData. (if (contains? #{\# \?} (get location-str 0))
                                              (subs location-str 1)
                                              location-str))]
    (zipmap (map keyword (.getKeys query-args-obj)) (.getValues query-args-obj))))

(defn query-args-map []
  (args-map js/window.location.search))

(defn reset-alert [cursor owner]
  (om/component
   (html
    (let [params (query-args-map)]
      [:div {:style {:padding-top "10px"}
             :class (if (:reset params) "" "hidden")}
       [:div {:class "alert alert-success"
              :role "alert"}
        "Password has been changed successfully. Please log in."]]))))


(when-let [alert-div (.getElementById js/document "reset-alert")]
  (om/root reset-alert
           app-model
           {:target alert-div}))
