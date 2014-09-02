(ns kixi.hecuba.property-search
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require
   [om.core :as om :include-macros true]
   [sablono.core :as html :refer-macros [html]]
   [cljs.core.async :refer [chan mult tap]]
   [ajax.core :refer (GET)]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.common :refer (log interval) :as common]
   [kixi.hecuba.tabs.hierarchy.data :as data]
   [kixi.hecuba.tabs.hierarchy.property-details :as pd]
   [kixi.hecuba.tabs.slugs :as slugs]
   [clojure.string :as str]))

(def app-model
  (atom
   {:properties {:data []
                 :selected nil
                 :alert {}
                 :devices {:name "Devices"
                           :header   {:cols {[:location :name] {:label "Name"}
                                             :type     {:label "Type"}
                                             :unit     {:label "Unit"}
                                             :select   {:label "Select" :checkbox true}}
                                      :sort [:name]}
                           :alert {}
                           :selected nil
                           :adding false
                           :editing false
                           :edited-device nil}
                 :sensors {:name     "Sensors"
                           :header   {:cols {[:location :name] {:label "Name"}
                                             :type     {:label "Type"}
                                             :unit     {:label "Unit"}
                                             :select   {:label "Select" :checkbox true}}
                                      :sort [:name]}
                           :selected nil
                           :editing false
                           :row nil
                           :alert {}}
                 :chart    {:property ""
                            :sensors #{}
                            :unit ""
                            :range {}
                            :measurements []
                            :message ""}
                 :raw-data {:name "Raw Data"
                            :data []
                            :selected nil
                            :message ""
                            :range {:start-date nil :end-end nil}}
                 :profiles {:alert {}}
                 :uploads []
                 :downloads {:files []}}
    :search {:term nil}
    :fetching {:properties false}
    :active-components {}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data fetchers

(defn fetch-properties [data query] ;; TOFIX rename to search-properties
  (GET (str "/4/entities/?q=" query)
       {:handler (fn [response]
                   (om/update! data [:properties :data] (take 20 (:entities response)))
                   (om/update! data [:stats] {:total_hits (:total_hits response)
                                              :page (:page response)})
                   (om/update! data [:fetching :entities] false))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; History

(defn history-loop [history-channel data]
  (go-loop []
    (let [nav-event                     (<! history-channel)
          history-status                (-> nav-event :args)
          range                         (:search history-status)
          [start-date end-date]         range
          {:keys [properties sensors]}  (:ids history-status)
          old-nav                       (:active-components @data)
          old-properties                (:properties (:ids old-nav))
          old-sensors                   (:sensors (:ids old-nav))
          old-range                     (:search old-nav)
          [old-start-date old-end-date] old-range]

      ;; Clear down
      (when-not properties
        (log "Clearing devices, sensors and measurements.")
        (om/update! data [:properties :selected] nil)
        (om/update! data [:property-details :data] {})
        (om/update! data [:property-details :property_id] nil)
        (om/update! data [:devices :data] [])
        (om/update! data [:sensors :data] [])
        (om/update! data [:measurements :data] []))

      ;; Fetch data
      (when (and properties
                 (empty? (-> @data :properties :data)))
        (fetch-properties data properties))

      (when (and (not= properties old-properties)
                 properties)
        (log "Setting property details to: " properties)
        (om/update! data [:properties :selected] properties)
        (om/update! data [:properties :sensors :selected] #{})
        (om/update! data [:properties :chart :measurements] [])
        (om/transact! data [:properties :data] (fn [d] (mapv #(cond
                                                               (= old-properties (:entity_id %)) (assoc % :selected false)
                                                               (= properties (:entity_id %)) (assoc % :selected true)
                                                               :else %)
                                                             d))))

      (when sensors
        (log "Setting selected sensors to: " sensors)
        (om/update! data [:properties :sensors :selected] (into #{} (str/split sensors #";"))))

      (when (and (not= range old-range)
                 (seq range))
        (log "Setting date range to: " start-date end-date)
        (om/update! data [:properties :chart :range] {:start-date start-date :end-date end-date}))

      (when (and properties sensors start-date end-date)
        (data/fetch-measurements data properties sensors start-date end-date))

      ;; Update the new active components
      (om/update! data :active-components history-status))
    (recur)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Table of found properties

(defn table-row [data]
  (fn [cursor owner]
    (om/component
     (html
      (let [property_data (:property_data cursor)
            entity_id     (:entity_id cursor)
            history       (om/get-shared owner :history)]
        [:tr {:onClick (fn [_] (history/update-token-ids! history :properties entity_id))
              :class (if (= (-> data :properties :selected) entity_id) "success" "")}
         [:td (when-let [uri (:uri (first (:photos cursor)))]
                [:img.img-thumbnail.table-image
                 {:src uri}])]
         [:td (:property_code cursor)]
         [:td (:property_type property_data)]
         [:td (slugs/postal-address property_data)]
         [:td (:address_region property_data)]
         [:td (:ownership property_data)]
         [:td (for [ti (:technology_icons property_data)]
                [:img.img-responsive {:src ti :width 40 :height 40}])]
         [:td (:monitoring_hierarchy property_data)]])))))

(defn table [data]
  (fn [cursor owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12 {:style {:padding-top "10px"} :id "found-properties-table"}
          [:h2 "Properties"]
          [:div
           [:table.table.table-hover.table-condensed
            [:thead
             [:tr [:th "Photo"] [:th "Property Code"] [:th "Type"] [:th "Address"]
              [:th "Region"] [:th "Ownership"] [:th "Technologies"] [:th "Monitoring Hierarchy"]]]
            [:tbody
             (om/build-all (table-row data) cursor {:key :entity_id})]]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Search input field

(defn search-input [data]
  (fn [cursor owner]
    (om/component
     (let [history (om/get-shared owner :history)]
       (html
        [:div
         [:div.input-group.input-group-md
          [:input {:type "text"
                   :default-value (:term cursor)
                   :class "form-control input-md"
                   :on-change (fn [e] (om/set-state! owner :value (.-value (.-target e))))
                   :on-key-press (fn [e] (when (= (.-keyCode e) 13)
                                           (om/update! cursor :term (om/get-state owner :value))
                                           (history/update-token-ids! history :properties nil)
                                           (fetch-properties data (om/get-state owner :value))))}]
          [:span.input-group-btn
           [:button {:type "button"
                     :class "btn btn-primary"
                     :onClick (fn [_]
                                (om/update! cursor :term (om/get-state owner :value))
                                (history/update-token-ids! history :properties nil)
                                (fetch-properties data (om/get-state owner :value)))}
            "Search"]]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entire view of property search page

(defn property-search-view [data owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [clicked     (om/get-state owner [:chans :selection])
            history     (om/get-shared owner :history)
            refresh     (om/get-shared owner :refresh)
            m           (mult (history/set-chan! history (chan)))
            tap-history #(tap m (chan))]

        (history-loop (tap-history) data)

        ;;go loop listening for requests to refresh tables
        (go-loop []
          (let [{:keys [event id]}  (<! refresh)]

            (cond
             (= event :properties)     (data/fetch-properties (-> @data :active-components :ids :projects) data)
             (= event :property)       (data/fetch-property (-> @data :active-components :ids :properties) data)
             (= event :new-property)   (data/fetch-new-property id data)))
          (recur))))
    om/IRender
    (render [_]
      (html
       [:div {:style {:padding-top "10px"}}
        (om/build (search-input data) (:search data))
        (om/build (table data) (-> data :properties :data))
        (om/build pd/property-details-div (:properties data))]))))

(when-let [search (.getElementById js/document "search")]
  (om/root property-search-view
           app-model
           {:target search
            :shared {:history (history/new-history [:properties :sensors :range])
                     :refresh (chan)}}))
