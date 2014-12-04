(ns kixi.hecuba.tabs.hierarchy
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
   [cljs.reader :as reader]
   [ajax.core :refer (GET)]
   [clojure.string :as str]
   [kixi.hecuba.bootstrap :as bs]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.model :refer (app-model)]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.tabs.hierarchy.programmes :as programmes]
   [kixi.hecuba.tabs.hierarchy.projects :as projects]
   [kixi.hecuba.tabs.hierarchy.properties :as properties]
   [kixi.hecuba.tabs.hierarchy.property-details :as property-details]
   [kixi.hecuba.tabs.hierarchy.data :as data]
   [kixi.hecuba.common :refer (log) :as common]
   [kixi.hecuba.tabs.hierarchy.tech-icons :as icons]
   [sablono.core :as html :refer-macros [html]]))

(defn back-to-programmes [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (history/update-token-ids! history :properties nil)
    (history/update-token-ids! history :projects nil)
    (history/update-token-ids! history :programmes nil)
    (common/fixed-scroll-to-element "programmes-div")))

(defn back-to-properties [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (history/update-token-ids! history :properties nil)
    (common/fixed-scroll-to-element "properties-div")))

(defn back-to-devices [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (common/fixed-scroll-to-element "devices-div")))

(defn uri-for-selection-change
  "Returns the uri to load because of change of selection. Returns nil
   if no change to selection"
  [current-selected selection-key template nav-event]
  (let [ids          (-> nav-event :args :ids)
        new-selected (get ids selection-key)]
    (when (or (nil? current-selected)
              (nil? new-selected)
              (not= current-selected
                    new-selected))
      (vector new-selected
              (common/map-replace template ids)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; History loop - this drives the fetches and clear downs
(defn history-loop [history-channel data]
  (go-loop []
    (let [nav-event                                        (<! history-channel)
          history-status                                   (-> nav-event :args)
          [start-date end-date]                            (:search history-status)
          {:keys [programmes projects properties sensors]} (:ids history-status)
          old-nav                                          (:active-components @data)
          old-programmes                                   (-> old-nav :ids :programmes)
          old-projects                                     (-> old-nav :ids :projects)
          old-properties                                   (-> old-nav :ids :properties)
          [old-start-date old-end-date]                    (-> old-nav :search)]

      ;; Clear down
      (when (or (nil? programmes)
                (empty? (-> @data :programmes :data)))
        (log "Clearing projects.")
        (om/update! data [:programmes :selected] nil)
        (om/update! data [:projects :data] [])
        (om/update! data [:projects :programme_id] nil)
        (data/fetch-programmes data))

      (when-not projects
        (log "Clearing properties.")
        (om/update! data [:projects :selected] nil)
        (om/update! data [:properties :data] [])
        (om/update! data [:properties :project_id] nil))

      (when-not properties
        (log "Clearing devices, sensors and measurements.")
        (om/update! data [:properties :selected] nil)
        (om/update! data [:property-details :data] {})
        (om/update! data [:property-details :property_id] nil)
        (om/update! data [:properties :devices :data] [])
        (om/update! data [:properties :sensors :data] [])
        (om/update! data [:properties :sensors :selected] #{})
        (om/update! data [:properties :sensors :alert] {})
        (om/update! data [:properties :chart :sensors] #{})
        (om/update! data [:properties :chart :measurements] [])
        (om/update! data [:properties :chart :all-groups] [])
        (om/update! data [:properties :chart :units] {}))

      (when-not sensors
        (log "Clearing measurements")
        (om/update! data [:properties :chart :measurements] [])
        (om/update! data [:properties :chart :all-groups] [])
        (om/update! data [:properties :chart :sensors] #{})
        (om/update! data [:properties :chart :units] {})
        (om/update! data [:properties :sensors :selected] #{}))

      (when (and (not= programmes old-programmes)
                 programmes)
        (log "Setting selected programme to: " programmes)
        (om/update! data [:programmes :selected] programmes)
        (om/update! data [:projects :programme_id] programmes)
        (om/update! data [:properties :programme_id] programmes)
        (om/update! data [:properties :sensors :selected] #{})
        (om/update! data [:properties :sensors :alert] {})
        (om/update! data [:properties :chart :sensors] #{})
        (om/update! data [:properties :chart :measurements] [])
        (om/update! data [:properties :chart :all-groups] [])
        (om/update! data [:properties :chart :units] {})
        (om/transact! data [:programmes :data] (fn [d] (mapv #(cond
                                                               (= old-programmes (:programme_id %)) (assoc % :selected false)
                                                               (= programmes (:programme_id %)) (assoc % :selected true)
                                                               :else %)
                                                             d)))
        (data/fetch-projects programmes data))

      (when (and (not= projects old-projects)
                 projects)
        (log "Setting selected project to: " projects)
        (om/update! data [:projects :selected] projects)
        (om/update! data [:properties :project_id] projects)
        (om/update! data [:properties :sensors :selected] #{})
        (om/update! data [:properties :sensors :alert] {})
        (om/update! data [:properties :chart :sensors] #{})
        (om/update! data [:properties :chart :measurements] [])
        (om/update! data [:properties :chart :all-groups] [])
        (om/update! data [:properties :chart :units] {})
        (om/update! data [:properties :editable] (-> @data :projects :editable))
        (om/transact! data [:projects :data] (fn [d] (mapv #(cond
                                                             (= old-projects (:project_id %)) (assoc % :selected false)
                                                             (= projects (:project_id %)) (assoc % :selected true)
                                                             :else %)
                                                           d)))
        (om/update! data [:properties :can-add-properties] (-> (filter #(= (:project_id %) projects) (-> @data :projects :data))
                                                               first
                                                               :editable))
        (data/fetch-properties projects data))

      (when (and (not= properties old-properties)
                 properties)
        (log "Setting property details to: " properties)
        (om/update! data [:properties :selected] properties)
        ;; Update datasets tab
        (om/update! data [:properties :datasets :sensors] (data/fetch-sensors properties (:properties @data)))
        (om/update! data [:properties :datasets :editable] (-> (filter #(= (:entity_id %) properties) (:data (:properties @data)))
                                                               first
                                                               :editable))
        (data/fetch-datasets properties [:properties :datasets :datasets] data)
        (om/update! data [:properties :datasets :property-id] properties)
        ;; Update upload and download statatus tab
        (om/update! data [:properties :uploads :files] [])
        (om/update! data [:properties :downloads :files] [])
        ;; Update sensors tab
        (om/update! data [:properties :sensors :selected] #{})
        (om/update! data [:properties :sensors :alert] {})
        (om/update! data [:properties :chart :sensors] #{})
        (om/update! data [:properties :chart :units] {})
        (om/update! data [:properties :chart :measurements] [])
        (om/update! data [:properties :chart :all-groups] [])
        (om/transact! data [:properties :data] (fn [d] (mapv #(cond
                                                             (= old-properties (:entity_id %)) (assoc % :selected false)
                                                             (= properties (:entity_id %)) (assoc % :selected true)
                                                             :else %)
                                                           d))))

      (when sensors
        (log "Setting selected sensors to: " sensors)
        (let [sensors-hashmap (into #{} (str/split sensors #";"))]
          (om/update! data [:properties :chart :sensors] sensors-hashmap)
          (om/update! data [:properties :sensors :selected] sensors-hashmap)))

      (when (or (not= start-date old-start-date)
                (not= end-date old-end-date))
        (log "Setting date range to: " start-date end-date)
        (om/update! data [:properties :chart :range] {:start-date start-date :end-date end-date}))

      (when (and programmes (seq (-> @data :programmes :data)))
        (let [selected-programme (first (filter #(= (:programme_id %) programmes) (-> @data :programmes :data)))]
          (om/update! data [:projects :can-add-projects] (:editable selected-programme))))

      (when (and projects (seq (-> @data :projects :data)))
        (let [selected-project (first (filter #(= (:project_id %) projects) (-> @data :projects :data)))]
          (om/update! data [:properties :can-add-properties] (:editable selected-project))))

      ;; Update the new active components
      (om/update! data :active-components history-status))
    (recur)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Search input field

(defn active-selections []
  (om/ref-cursor (-> (om/root-cursor app-model) :active-components)))

(defn search []
  (om/ref-cursor (-> (om/root-cursor app-model) :search)))

(defn order-key [sort-asc]
  (if sort-asc "asc" "desc"))

(defn get-sort-str [th-click sort-asc]
  (str "&sort_key=" (str (name th-click) ".lower_case_sort")
       "&sort_order=" (order-key sort-asc)))

(defn update-sort [cursor th-click sort-asc term results-size]
  (om/update! cursor :sort-spec {:sort-key th-click :sort-asc sort-asc})
  (data/search-properties cursor 0 results-size term (get-sort-str th-click sort-asc)))

(defn search-input [cursor owner]
  (om/component
   (let [history (om/get-shared owner :history)]
     (html
      [:div {:style {:padding-top "10px"}}
       [:div.input-group.input-group-md
        [:input {:type "text"
                 :default-value (:term cursor)
                 :class "form-control input-md"
                 :on-change (fn [e]
                              (om/set-state! owner :value (.-value (.-target e)))
                              (when (empty? (.-value (.-target e)))
                                (om/update! cursor :selected nil)
                                (om/update! cursor :term nil)
                                (om/update! cursor :data [])))
                 :on-key-press (fn [e] (when (= (.-keyCode e) 13)
                                         ;; Get page 0, 10 items
                                         (let [term (om/get-state owner :value)
                                               {:keys [sort-key sort-asc]} (:sort-spec @cursor)]
                                           (om/update! cursor :term term)
                                           (data/search-properties cursor 0 10 term
                                                                   (get-sort-str sort-key sort-asc)))))}]
        [:span.input-group-btn
         [:button {:type "button"
                   :class "btn btn-primary"
                   :onClick (fn [_]
                              (om/update! cursor :term (om/get-state owner :value))
                              (data/search-properties cursor 0 10 (om/get-state owner :value)))}
          [:span.glyphicon.glyphicon-search]]]]]))))

(defn found-property-row [cursor owner]
  (om/component
   (html
    (let [property_data (:property_data cursor)
          {:keys [programme_id project_id entity_id]} cursor
          history            (om/get-shared owner :history)
          selections         (om/observe owner (active-selections))
          selected-entity-id (-> selections :ids :properties)
          search-cursor      (search)]
      [:tr {:onClick (fn [_]
                       (history/update-token-ids! history :programmes programme_id)
                       (history/update-token-ids! history :projects project_id)
                       (history/update-token-ids! history :properties entity_id)
                       (om/update! search-cursor :selected entity_id))
            :class (if (= selected-entity-id entity_id) "success" "")}
       [:td (when-let [uri (:uri (first (:photos cursor)))]
              [:img.img-thumbnail.table-image
               {:src uri}])]
       [:td (:programme_name cursor)]
       [:td (:project_name cursor)]
       [:td (:property_code cursor)]
       [:td (slugs/postal-address property_data)]
       [:td.tech-icon-container-sm (for [ti (-> cursor :property_data :technology_icons)]
                                     (icons/tech-icon ti))]]))))

(defn search-results [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:results-size 10
       :th-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [th-chan results-size]}  (om/get-state owner)
              {:keys [sort-key sort-asc]}     (:sort-spec @cursor)
              th-click                        (<! th-chan)]
          (if (= th-click sort-key)
            (update-sort cursor th-click (not sort-asc) (:term @cursor) results-size)
            (update-sort cursor th-click true (:term @cursor) results-size)))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [results-size th-chan]}      (om/get-state owner)
            {:keys [data term stats sort-spec]} cursor
            {:keys [sort-key sort-asc]}         sort-spec
            {:keys [total_hits page]}           stats]
        (html
         [:div.row#results-div
          [:div.col-md-12 {:style {:padding-top "10px"} :id "found-properties-table"}
           [:h2 (str "We've found " total_hits (if (= total_hits 1) " matching property." " matching properties."))]
           (if (:fetching cursor)
             [:div [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "Fetching data."]]]
             (let [results-left  (- total_hits (* (inc page) results-size))
                   more-to-load? (pos? results-left)]
               (if (-> data seq)
                 [:div
                  [:table.table.table-hover.table-condensed
                   [:thead
                    [:tr [:th "Photo"]
                     (bs/sorting-th sort-spec th-chan "Programme Name" :programme_name)
                     (bs/sorting-th sort-spec th-chan "Project Name" :project_name)
                     (bs/sorting-th sort-spec th-chan "Property ID" :property_code)
                     (bs/sorting-th sort-spec th-chan "Address" :address)
                     [:th "Technologies"]]]
                   [:tbody
                    (om/build-all found-property-row data {:key :entity_id})]]
                  [:nav
                   [:ul.pager
                    [:li {:style {:padding-right "5px"}}
                     [:a {:class (if (= page 0) "previous disabled" "previous hover")
                          :on-click (fn [_]
                                      (when-not (= page 0)
                                        (data/search-properties cursor (dec page) results-size term
                                                                (get-sort-str sort-key sort-asc))))}
                      "Previous"]]
                    [:li {:style {:padding-right "5px"}}
                     [:a {:class (if-not more-to-load? "next disabled" "next hover")
                          :on-click (fn [_]
                                      (when more-to-load?
                                        (data/search-properties cursor (inc page) results-size term
                                                                (get-sort-str sort-key sort-asc))))}
                      "Next"]]]]]

                 [:div [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "No results."]]])))]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main View

(defn main-tab [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [history       (om/get-shared owner :history)
            refresh       (om/get-shared owner :refresh)
            property-chan (om/get-shared owner :property-chan)
            m             (mult (history/set-chan! history (chan)))
            tap-history   #(tap m (chan))]

        ;; handle navigation changes
        (history-loop (tap-history) data)

        ;; go loop listening for requests to refresh tables
        (go-loop []
          (let [{:keys [event id]}  (<! refresh)
                {:keys [programmes projects properties]} (-> @data :active-components :ids)]
            (log "Event received! " event)
            (cond
             (= event :programmes)     (data/fetch-programmes data)
             (= event :projects)       (data/fetch-projects programmes data)
             (= event :properties)     (data/fetch-properties projects data)
             (= event :property)       (data/fetch-property properties data)
             (= event :new-property)   (data/fetch-new-property id data)
             (= event :datasets)       (data/fetch-datasets properties [:properties :datasets :datasets] data)
             (= event :upload-status)  (data/fetch-upload-status programmes projects properties data)))
          (recur))))
    om/IRender
    (render [_]
      (html
       (let [{:keys [search programmes projects properties]} data]
         [:div
          (om/build search-input search)
          (when (-> search :term) (om/build search-results search))
          (om/build programmes/programmes-div programmes)
          (om/build projects/projects-div projects)
          (om/build properties/properties-div properties)
          [:div.col-md-12.last
           (om/build property-details/property-details-div properties)]])))))
