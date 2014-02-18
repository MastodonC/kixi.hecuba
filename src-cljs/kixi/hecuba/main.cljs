(ns kixi.hecuba.main
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter<]]
   [ajax.core :refer (GET POST)]
   [kixi.hecuba.navigation :as nav]
   [kixi.hecuba.web.chart :as chart]))

(enable-console-print!)

(def app-model
  (atom
   {:messages []
    :nav {:active "dashboard"
          :menuitems [{:name :dashboard :label "Dashboard" :href "/index.html" :icon "dashboard"}
                      {:name :overview :label "Overview" :href "/charts.html" :icon "bar-chart-o"}
                      {:name :users :label "Users"}
                      {:name :programmes :label "Programmes" :active? true}
                      {:name :projects :label "Project"}
                      {:name :properties :label "Properties"}
                      {:name :charts :label "Charts"}
                      {:name :about :label "About"}
                      {:name :documentation :label "Documentation"}
                      {:name :api_users :label "API users"}
                      ]}

    :tab-container {:selected :programmes
                    :tabs [{:name :about :title "About"}
                           {:name :documentation :title "Documentation"}
                           {:name :users :title "Users"}
                           {:name :programmes
                            :title "Programmes"
                            :location {:name "(location name goes here)"
                                       :longitude "0"
                                       :latitude "0"}
                            :tables {:programmes {:name "Programmes"
                                                  :header {:cols {:name {:label "Name" :href :href}
                                                                  :description {:label "Description"}
                                                                  :created_at {:label "Created at"}
                                                                  }
                                                           :sort [:name :leaders]}}
                                     :projects {:name "Projects"
                                                :header {:cols {:name {:label "Name" :href :href}
                                                                :type_of {:label "Type"}
                                                                :description {:label "Description"}
                                                                ;; TODO Why are these underscores?
                                                                :created_at {:label "Created at"}
                                                                :organisation {:label "Organisation"}
                                                                :project_code {:label "Project code"}}
                                                         :sort [:name]}}
                                     :properties {:name "Properties"
                                                  :header {:cols {:addressStreetTwo {:label "Address" :href "href"}
                                                                  :addressCounty {:label "County"}
                                                                  :addressCountry {:label "Country"}
                                                                  :addressRegion {:label "Region"}}
                                                           :sort [:addressStreetTwo]}}
                                     :devices {:name "Devices"
                                               :header {
                                                        ;; TODO Why do keys work here? Probably a bug in the liberator resource
                                                        :cols {:entity-id {:label "Entity"}
                                                               :device-id {:label "Device"}
                                                               }
                                                        :sort [:entity-id :device-id]}}
                                     :sensors {:name "Sensors"
                                               :header {:cols {:type {:label "Type"}
                                                               :unit {:label "Unit"}
                                                               :period {:label "Period"}
                                                               :deviceId {:label "Device"}}
                                                        :sort [:type]}}
                                     :measurements {:name "Measurements"
                                               :header {:cols {:timestamp {:label "Timestamp"}
                                                               :type {:label "Type"}
                                                               :value {:label "Value"}
                                                               :error {:label "Error"}}
                                                        :sort [:timestamp]}}}
                            }
                           {:name :charts
                            :title "Charts"
                            :chart {:property "rad003"
                                    :devices [{:hecuba/name "01"
                                               :name "External temperature"}
                                              {:hecuba/name "02"
                                               :name "External humidity"}]}
                            }
                           ]}

    }))

(defn blank-tab [data owner]
  (om/component
      (dom/p nil "This page is unintentionally left blank")))

(defn charts-tab [data owner]
  (om/component
      (dom/div nil
           (dom/h1 nil (:title data))
           (om/build chart/chart-figure (:chart data)))))

(defn about-tab [data owner]
  (om/component
      (dom/div nil
           (dom/h1 nil (:title data))
           (dom/p nil "I'm the About tab"))))

(defn documentation-tab [data owner]
  (om/component
      (dom/div nil
           (dom/h1 nil (:title data))
           (dom/p nil "Some documentation"))))

(defn users-tab [data owner]
  (om/component
      (dom/div nil
           (dom/h1 nil (:title data))
           (dom/p nil "List of users"))))

(defn update-when [x pred f & args]
  (if pred (apply f x args) x))

(defn ajax [{:keys [in out]} content-type]
  (go-loop []
    (when-let [url (<! in)]
      (GET url
          (-> {:handler #(put! out %)
               :headers {"Accept" content-type
                         "Authorization" "Basic Ym9iOnNlY3JldA=="}}
              (update-when (= content-type "application/json") merge {:response-format :json :keywords? true})))

      (recur))))

(defn table [cursor owner {:keys [in out]}]
  (reify

    om/IWillMount
    (will-mount [_]
      (go-loop []
               (when-let [data (<! in)]
                 (om/set-state! owner :data data)
                 (om/transact! cursor :selected (constantly (first data)))
                 (when-let [row (first data)]
                   (put! out {:type :row-selected :row row})
                   ;; TODO We need to 'clear' tables below us if there's no data on this table
                   ;;(put! out {:type :clear-table})
                   )
                 (recur))))

    om/IRender
    (render [_]
      ;; Select the first row
      ;;(put! out {:type :row-selected :row (first (om/get-state owner :data))})
      (let [cols (get-in cursor [:header :cols])]
        (dom/table #js {:className "table table-bordered hecuba-table"} ;; table-hover table-striped
             (dom/thead nil
                  (dom/tr nil
                       (into-array
                        (for [[_ {:keys [label]}] cols]
                          (dom/th nil label)))))
             (dom/tbody nil
                  (into-array
                   (for [row (om/get-state owner :data)]
                     (dom/tr #js {:onClick (om/pure-bind
                                               (fn [_ _]
                                                 (om/transact! cursor :selected (constantly row))
                                                 (put! out {:type :row-selected :row row})
                                                 )

                                               cursor)
                                  :className (when (= row (:selected cursor)) "row-selected")
                                  }
                          (into-array
                           (for [[k {:keys [href]}] cols]
                             (dom/td nil (if href
                                           (dom/a #js {:href (get row href)} (get row k))
                                           (get row k)))))))))))
      )))

(defmulti render-content-directive (fn [itemtype _ _] itemtype))

(defmethod render-content-directive :text
  [_ item _]
  (dom/p nil item))

(defmethod render-content-directive :table
  [_ item owner]
  (om/build table item {:opts (om/get-state owner :event-chan)}))

(defn console-sink [label ch]
  (go-loop []
   (when-let [v (<! ch)]
     (println "[console-sink]" label ":" v)
     (recur))))

(defn make-channel-pair []
  {:in (chan (sliding-buffer 1))
   :out (chan (sliding-buffer 1))})

(defn programmes-tab [data owner]
  (reify
    om/IWillMount
    (will-mount [_]

      (let [programmes-table-ajax-pair (make-channel-pair)
            programmes-table-pair (make-channel-pair)
            projects-table-ajax-pair (make-channel-pair)
            projects-table-pair (make-channel-pair)
            properties-table-ajax-pair (make-channel-pair)
            properties-table-pair (make-channel-pair)
            devices-table-pair (make-channel-pair)
            devices-detail-ajax-pair (make-channel-pair)
            devices-detail-pair (make-channel-pair)
            sensors-table-pair (make-channel-pair)
            measurements-table-ajax-pair (make-channel-pair)
            measurements-table-pair (make-channel-pair)]

        (ajax programmes-table-ajax-pair "application/edn")
        (ajax projects-table-ajax-pair "application/edn")
        (ajax properties-table-ajax-pair "application/json")
        (ajax devices-detail-ajax-pair "application/json")
        (ajax measurements-table-ajax-pair "application/json")

        #_(go-loop []
          (when-let [url (<! in)]
            (println "GET" url)
            (GET url
                {:handler #(put! out (case content-type
                                       "application/json" (js->clj %)
                                       %))
                 :headers {"Accept" content-type
                           "Authorization" "Basic Ym9iOnNlY3JldA=="}})
            (recur)))

        ;; The data coming 'out' of the programmes table ajax controller goes 'in' to the programmes table
        (pipe (:out programmes-table-ajax-pair) (:in programmes-table-pair))

        ;; The row clicked coming 'out' of the programmes table, we pick
        ;; out the uri and feed it 'in' to the projects table ajax
        ;; controller
        (pipe (map< (comp :projects :row) (:out programmes-table-pair))
              (:in projects-table-ajax-pair))

        ;; The data coming 'out' of the projects table ajax controller goes 'in' to the projects table
        (pipe (:out projects-table-ajax-pair) (:in projects-table-pair))

        ;; The row clicked coming 'out' of the projects table, we pick
        ;; out the uri and feed it 'in' to the properties table ajax
        ;; controller
        (pipe (map< (comp :properties :row) (:out projects-table-pair))
              (:in properties-table-ajax-pair))

        ;; The data coming 'out' of the properties table ajax controller goes 'in' to the properties table
        (pipe (:out properties-table-ajax-pair) (:in properties-table-pair))

        (pipe (map< (comp
                     (fn [{:keys [id deviceIds]}] (for [device-id deviceIds] {:entity-id id :device-id device-id}))
                     :row)              ; stored on the property row
                    (:out properties-table-pair))
              (:in devices-table-pair))

        (pipe (map< (comp (fn [{:keys [device-id entity-id]}]
                            (if (and device-id entity-id)
                              (do
                                (println "Constructing URI to device" (str "/entities/" entity-id "/devices/" device-id))
                                (str "/entities/" entity-id "/devices/" device-id))
                              (str "/Dummy")
                              ))
                          :row)
                    (:out devices-table-pair))
              (:in devices-detail-ajax-pair))

        (pipe (map< (fn [x]
                      ;; TODO This should really be an independent consumer on a multiplexed channel
                      (om/transact! data [:location :name] (constantly (-> x :location :name)))
                      (om/transact! data [:location :longitude] (constantly (-> x :location :longitude)))
                      (om/transact! data [:location :latitude] (constantly (-> x :location :latitude)))
                      (:readings x))
                    (:out devices-detail-ajax-pair))
              (:in sensors-table-pair))

        (console-sink "devices-detail-ajax" (:out sensors-table-pair))

        ;; Seed programmes table
        (put! (programmes-table-ajax-pair :in) "/programmes")

        (om/set-state! owner :programmes-table-channels programmes-table-pair)
        (om/set-state! owner :projects-table-channels projects-table-pair)
        (om/set-state! owner :properties-table-channels properties-table-pair)
        (om/set-state! owner :devices-table-channels devices-table-pair)
        (om/set-state! owner :sensors-table-channels sensors-table-pair)
        (om/set-state! owner :measurements-table-channels measurements-table-pair)

        ))

    om/IRender
    (render [_]
      (dom/div nil
               (dom/h1 nil (:title data))
               (om/build table (get-in data [:tables :programmes]) {:opts (om/get-state owner :programmes-table-channels)})
               (dom/h2 nil "Projects")
               (om/build table (get-in data [:tables :projects]) {:opts (om/get-state owner :projects-table-channels)})
               (dom/h2 nil "Properties")
               (om/build table (get-in data [:tables :properties]) {:opts (om/get-state owner :properties-table-channels)})
               (dom/h2 nil "Devices")
               (om/build table (get-in data [:tables :devices]) {:opts (om/get-state owner :devices-table-channels)})
               (dom/p nil (str "Location: " (get-in data [:location :name])))
               (dom/p nil (str "Longitude: " (get-in data [:location :longitude])))
               (dom/p nil (str "Latitude: " (get-in data [:location :latitude])))
               (dom/h2 nil "Sensors")
               (om/build table (get-in data [:tables :sensors]) {:opts (om/get-state owner :sensors-table-channels)})
               (dom/h2 nil "Measurements")
               (om/build table (get-in data [:tables :measurements]) {:opts (om/get-state owner :measurements-table-channels)})
               ))))


(defn tab-container [tabs]
  (fn [data owner]
    (om/component
        (dom/div nil
             (let [selected (-> data :tab-container :selected)]
               (if-let [tab (get tabs selected)]
                 (om/build tab (->> data :tab-container :tabs (filter #(= (:name %) selected)) first))
                 (om/build blank-tab data)))))))

(defn ^:export handle-left-nav [menu-item]
  ;; Currently we implement a one-to-one correspondence between the
  ;; left-hand-menu and the tab container, but in due course 'Project'
  ;; and 'Properties' will cause a scroll to a location under the
  ;; 'Programmes' tab
  (swap! app-model assoc-in [:tab-container :selected] menu-item))

(om/root app-model
    (let [{:keys [in out] :as pair} (make-channel-pair)]
      (go-loop []
               (when-let [n (<! out)]
                 (handle-left-nav n)
                 (recur)))
      (nav/nav pair))
    (.getElementById js/document "hecuba-nav"))

(om/root app-model (tab-container {:about about-tab
                                   :programmes programmes-tab
                                   :charts charts-tab
                                   :documentation documentation-tab
                                   :users users-tab})
    (.getElementById js/document "hecuba-tabs"))
