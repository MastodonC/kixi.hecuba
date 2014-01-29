(ns kixi.hecuba.main
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map<]]
   [ajax.core :refer (GET POST)]
   [kixi.hecuba.navigation :as nav]
   [kixi.hecuba.web.chart :as chart]))

(enable-console-print!)

(def app-model
  (atom
   {:messages []
    :nav {:active "dashboard"
          :menuitems [{:name :dashboard :label "Dashboard" :href "/index.html" :icon "dashboard" :active? true}
                      {:name :overview :label "Overview" :href "/charts.html" :icon "bar-chart-o"}
                      {:name :users :label "Users"}
                      {:name :programmes :label "Programmes"}
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
                            :tables {:programmes {:name "Programmes"
                                                  :header {:cols {:hecuba/name {:label "Name" :href :hecuba/href}
                                                                  :leaders {:label "Leaders"}}
                                                           :sort [:hecuba/name :leaders]}}
                                     :projects {:name "Projects"
                                                :header {:cols {:hecuba/name {:label "Name" :href :hecuba/href}}
                                                         :sort [:hecuba/name]}}
                                     :properties {:name "Properties"
                                                  :header {:cols {:hecuba/name {:label "Name" :href :hecuba/href}
                                                                  :rooms {:label "Rooms"}}
                                                           :sort [:hecuba/name :rooms]}}}
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

(defn ajax [{:keys [in out]}]
  (go-loop []
           (when-let [url (<! in)]
             (GET url
                 {:handler (partial put! out)
                  :headers {"Accept" "application/edn"}})
             (recur))))

(defn table [cursor owner {:keys [in out]}]
  (reify

    om/IWillMount
    (will-mount [_]
      (go-loop []
               (when-let [data (<! in)]
                 (om/set-state! owner :data data)
                 (om/transact! cursor :selected (constantly (first data)))
                 (put! out {:type :row-selected :row (first data)})
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
            properties-table-pair (make-channel-pair)]

        (ajax programmes-table-ajax-pair)
        (ajax projects-table-ajax-pair)
        (ajax properties-table-ajax-pair)

        ;; The data coming 'out' of the programmes table ajax controller goes 'in' to the programmes table
        (pipe (:out programmes-table-ajax-pair) (:in programmes-table-pair))

        ;; The row clicked coming 'out' of the programmes table, we pick
        ;; out the uri and feed it 'in' to the projects table ajax
        ;; controller
        (pipe (map< (comp :hecuba/children-href :row) (:out programmes-table-pair))
              (:in projects-table-ajax-pair))

        ;; The data coming 'out' of the projects table ajax controller goes 'in' to the projects table
        (pipe (:out projects-table-ajax-pair) (:in projects-table-pair))

        ;; The row clicked coming 'out' of the projects table, we pick
        ;; out the uri and feed it 'in' to the properties table ajax
        ;; controller
        (pipe (map< (comp :hecuba/children-href :row) (:out projects-table-pair))
              (:in properties-table-ajax-pair))

        ;; The data coming 'out' of the properties table ajax controller goes 'in' to the properties table
        (pipe (:out properties-table-ajax-pair) (:in properties-table-pair))

        (console-sink "properties-table" (:out properties-table-pair))

        ;; Seed programmes table
        (put! (programmes-table-ajax-pair :in) "/programmes")

        (om/set-state! owner :programmes-table-channels programmes-table-pair)
        (om/set-state! owner :projects-table-channels projects-table-pair)
        (om/set-state! owner :properties-table-channels properties-table-pair)

        ))

    om/IRender
    (render [_]
      (dom/div nil
           (dom/h1 nil (:title data))
           (om/build table (get-in data [:tables :programmes]) {:opts (om/get-state owner :programmes-table-channels)})
           (om/build table (get-in data [:tables :projects]) {:opts (om/get-state owner :projects-table-channels)})
           (om/build table (get-in data [:tables :properties]) {:opts (om/get-state owner :properties-table-channels)})
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

(om/root app-model (nav/nav handle-left-nav) (.getElementById js/document "hecuba-nav"))

(om/root app-model (tab-container {:about about-tab
                                   :programmes programmes-tab
                                   :charts charts-tab
                                   :documentation documentation-tab
                                   :users users-tab})
    (.getElementById js/document "hecuba-tabs"))
