(ns kixi.hecuba.menu
  (:require [ajax.core :refer (GET)]
            [kixi.hecuba.common :refer (log) :as common]
            [kixi.hecuba.tabs.slugs :as slugs]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(def menu-model (atom {:menu [ {:pathname "/app" :text "Main"}
                               {:pathname "/search" :text "Search"}
                               {:pathname "/properties_comparison" :text "Compare"}
                               {:pathname "/property_map" :text "Property Map"}
                               ;; {:pathname "/user" :text "User Management" :only-roles [:kixi.hecuba.security/super-admin]}
                               {:pathname "/logout" :text "Logout"}
                               {:pathname "https://github.com/MastodonC/kixi.hecuba/blob/master/doc/documentation.md" :text "Documentation"}]
                       :whoami nil}))

(defn menu [{:keys [menu whoami]} owner]
  (reify
    om/IRenderState
    (render-state [_ {page-pathname :pathname}]
      (html
       [:div
        [:ul {:class "nav navbar-nav"}
         (for [{:keys [pathname text roles]} menu
               :let [href (if (= page-pathname pathname) "#" pathname)]]
           [:li [:a {:href href} text]])]
        [:div.pull-right {:id :whoami} (slugs/slugify-whoami whoami)]]))))


(when-let [hecuba-menu (.getElementById js/document "hecuba-menu")]
  (om/root menu
           menu-model
           {:target hecuba-menu
            :init-state {:pathname (-> js/window .-location .-pathname)}})
  (GET "/4/whoami"
       {:handler (fn [x] (swap! menu-model assoc :whoami x))
        :error-handler (fn [x] (log "Error retrieving whoami data."))
        :response-format :json
        :keywords? true}))
