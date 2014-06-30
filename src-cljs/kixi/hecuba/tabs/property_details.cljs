(ns kixi.hecuba.tabs.property-details
  (:require [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.userAgent :as agent]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.tabs.slugs :as slugs]
            [kixi.hecuba.tabs.sensors :as sensors]
            [kixi.hecuba.tabs.profiles :as profiles]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Property Details Helpers

;; FIXME: This is a dupe in sensors.cljs
(defn get-property-details [selected-property-id data]
  (->>  data
        :properties
        :data
        (filter #(= (:id %) selected-property-id))
        first))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; property-details
(defn detail-section [title text]
  (if (and text (re-seq #"[A-Za-z0-9]" text))
    [:div [:h3 title]
     [:p text]]
    [:div {:class "hidden"}]))

(defn property-details-div [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:active-tab :overview})
    om/IRenderState
    (render-state [_ state]
      (let [active-tab           (:active-tab state)
            selected-property-id (-> data :active-components :properties)
            properties           (-> data :properties :data)
            property-details     (get-property-details selected-property-id data)
            property_data        (:property_data property-details)]
        (html [:div {:class (str "col-md-12" (if selected-property-id "" " hidden"))}
               [:h2 "Property Details"]
               (bs/panel
                (:slug property-details)
                [:div ;; Tab Container
                 [:ul.nav.nav-tabs {:role "tablist"}
                  [:li {:class (if (= active-tab :overview) "active" nil)}
                   [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :overview))}
                    "Overview"]]
                  [:li {:class (if (= active-tab :profiles) "active" nil)}
                   [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :profiles))}
                    "Profiles"]]
                  [:li {:class (if (= active-tab :sensors) "active" nil)}
                   [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :sensors))}
                    "Sensor Data"]]]
                 ;; Overview
                 [:div {:class (if (not= active-tab :overview) "hidden" "col-md-12")}
                  [:h3 "Overview"]
                  [:div.col-md-4
                   [:dl.dl-horizontal
                    [:dt "Property Code"] [:dd (:property_code property-details)]
                    [:dt "Address"] [:dd (slugs/postal-address-html property_data)]
                    [:dt "Property Type"] [:dd (:property_type property_data)]
                    [:dt "Built Form"] [:dd (:built_form property_data)]
                    [:dt "Age"] [:dd (:age property_data)]
                    [:dt "Ownership"] [:dd (:ownership property_data)]
                    [:dt "Project Phase"] [:dd (:project_phase property_data)]
                    [:dt "Monitoring Hierarchy"] [:dd (:monitoring_hierarchy property_data)]
                    [:dt "Practical Completion Date"] [:dd (:practical_completion_date property_data)]
                    [:dt "Construction Date"] [:dd (:construction_date property_data)]
                    [:dt "Conservation Area"] [:dd (:conservation_area property_data)]
                    [:dt "Listed Building"] [:dd (:listed property_data)]
                    [:dt "Terrain"] [:dd (:terrain property_data)]
                    [:dt "Degree Day Region"] [:dd (:degree_day_region property_data)]
                    ]]
                  [:div.col-md-2
                   (when-let [pic (:path (first (:photos property-details)))]
                     [:img.img-thumbnail.tmg-responsive
                      {:src (str "https://s3-us-west-2.amazonaws.com/get-embed-data/" pic)}])]
                  [:div.col-md-6
                   (for [ti (:technology_icons property_data)]
                     [:img.tmg-responsive {:src ti :width 80 :height 80}])]
                  [:div.col-md-12
                   (detail-section "Description" (:description property_data))
                   (detail-section "Project Summary" (:project_summary property_data))
                   (detail-section "Project Team" (:project_team property_data))
                   (detail-section "Design Strategy" (:design_strategy property_data))
                   (detail-section "Energy Strategy" (:energy_strategy property_data))
                   (detail-section "Monitoring Policy" (:monitoring_policy property_data))
                   (detail-section "Other Notes" (:other_notes property_data))]]
                 ;; Sensors
                 [:div {:class (if (not= active-tab :sensors) "hidden" nil)}
                  (om/build sensors/sensors-div data)]
                 ;; Profiles
                 [:div {:class (if (not= active-tab :profiles) "hidden" "col-md-12")}
                  (om/build profiles/profiles-div data)]])])))))
