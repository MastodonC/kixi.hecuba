(ns kixi.hecuba.password-reset
  (:require
   [om.core :as om :include-macros true]
   [clojure.string :as str]
   [kixi.hecuba.tabs.hierarchy.data :as data]
   [kixi.hecuba.common :refer (log) :as common]
   [kixi.hecuba.bootstrap :as bs]
   [sablono.core :as html :refer-macros [html]]))

(def app-model
  (atom
   {:alert {}
    :input-class ""}))

(defn post-form [data url password]
  (om/update! data :input-class "has-success")
  (common/post-resource url
                        {:password password}
                        (fn [response] (set! (.-location js/window) "/login?reset=true"))
                        (fn [{:keys [status status-text]}]
                          (om/update! data :alert {:status true
                                                  :class "alert alert-danger"
                                                  :text status-text}))))

(defn save-new-password [data state]
  (let [password              (-> state :password)
        password-confirmation (-> state :password-confirmation)
        url                   (.-URL js/document)]
    (if (= password password-confirmation)
      (post-form data url password)
      (do
        (om/update! data :input-class "has-error")
        (om/update! data :alert {:status true
                                 :class "alert alert-danger"
                                 :text "Passwords must match"})))))


(defn new-password-view [cursor owner]
  (om/component
   (html
    (let [class   (:input-class cursor)]
      [:div
       [:h1 "Reset Password"]
       ;; Alert
       [:div {:style {:padding-top "10px"}} (bs/alert owner)]
       ;; Password input form
       [:form {:role "form"}
        [:div.form-group {:class class}
         [:label {:for "password-input"} "New Password"]
         [:input {:type "password"
                  :class "form-control"
                  :id "password-input"
                  :placeholder "Password"
                  :on-change (fn [e] (om/set-state! owner :password  (.-value (.-target e))))}]]
        [:div.form-group {:class class}
         [:label {:for "password-confirmation"} "Confirm password"]
         [:input {:type "password"
                  :class "form-control"
                  :id "password-confirmation"
                  :placeholder "Confirm password"
                  :on-change (fn [e] (om/set-state! owner :password-confirmation  (.-value (.-target e))))}]]
        [:button {:type "button"
                  :class "btn btn-primary"
                  :on-click #(save-new-password cursor (om/get-state owner))} "Submit"]]]))))

(when-let [new-password (.getElementById js/document "new-password")]
  (om/root new-password-view
           app-model
           {:target new-password}))
