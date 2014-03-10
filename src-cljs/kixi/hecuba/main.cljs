(ns kixi.hecuba.main
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
   [ajax.core :refer (GET POST)]
   [clojure.string :as str]
   [kixi.hecuba.navigation :as nav]
   [kixi.hecuba.chart :as chart]
   [kixi.hecuba.common :refer (index-of map-replace find-first)]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.model :refer (app-model)]))

(enable-console-print!)

(def history (history/new-history))

;; channel onto which history events are put
(def history-channel (history/set-chan! history (chan)))

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

(defn row-for [{:keys [selected data]}]
  (find-first #(= (:id %) selected) data))

(defn uri-for-selection-change
  "Returns the uri to load because of change of selection. Returns nil
   if no change to selection"
  [selected selection-key template {{ids :ids} :args}]
  (println "ids: " ids)
  (let [new-selected (get ids selection-key)]
    (when (or (nil? selected)
              (not= selected new-selected))
      (vector new-selected
              (map-replace template ids)))))

(defn ajax [in data {:keys [template
                            selection-key
                            content-type]}]
  (go-loop []
    (when-let [[new-selected uri] (uri-for-selection-change (:selected @data)
                                                selection-key
                                                template
                                                (<! in))]
      (println "GET " uri)
      (GET uri
           (-> {:handler  (fn [x]
                            (when  (= selection-key :sensor)
                              (println (pr-str x)))
                            (om/update! data :data x)
                            (om/update! data :selected new-selected))
                :headers {"Accept" content-type}
                :response-format :text}
               (cond-> (= content-type "application/json")
                       (merge {:response-format :json :keywords? true})))))
    (recur)))

(defn selected-range-change
  [selected selection-key template {{ids :ids search :search} :args}]
  (println "ids: " ids)
  (let [new-selected (get ids selection-key)]
    (when (or (nil? selected)
              (not= selected new-selected))
      (vector new-selected ids search))))

(defn chart-ajax [in data {:keys [template
                                  selection-key
                                  content-type]}]
  (go-loop []
    (when-let [[new-range ids search] (selected-range-change (:range @data)
                                                      selection-key
                                                      template
                                                      (<! in))]
      (let [[start-date end-date] search
            entity-id             (get ids :property)
            [type device-id]      (str/split (get ids :sensor) #"-")
            url                   (str "/3/entities/" entity-id "/devices/" device-id "/measurements/" type "?startDate=" start-date "&endDate=" end-date)]
        (when (and (not= "" start-date)
                   (not= "" end-date)
                   (not (nil? start-date))
                   (not (nil? device-id))
                   (not (nil? entity-id))
                   (not (nil? type)))
          (prn "measurements url: " url)
          (GET url {:handler #(om/transact! data [:measurements] (constantly %))
                    :headers {"Accept" "application/json"}
                    :response-format :json
                    :keywords? true})))
      )
    (recur)))


;; TODO histkey is really id key. resolve name confusion.
(defn table [cursor owner {:keys [histkey path]}]
  (reify
    om/IRender
    (render [_]
      ;; Select the first row
      ;;(put! out {:type :row-selected :row (first (om/get-state owner :data))})
      (let [cols (get-in cursor [:header :cols])
            table-id (str (name histkey) "-table")]
        (dom/table #js {:id table-id
                        :className "table table-bordered hecuba-table "} ;; table-hover table-stripedso,
                   (dom/thead nil
                              (dom/tr nil
                                      (into-array
                                       (for [[_ {:keys [label]}] cols]
                                         (dom/th nil label)))))
                   (dom/tbody nil
                              (into-array
                               (for [{:keys [id href] :as row} (-> cursor :data
                                                                   (cond-> path path))]
                                 (dom/tr #js {:onClick (fn [_ _ ]
                                                         (println "click:" id)
                                                         (om/update! cursor :selected id)
                                                         (history/update-token-ids! history histkey id))
                                              :className (when (= id (:selected cursor)) "row-selected")
                                              ;; TODO use this to scroll the row into view.
                                              ;; Possible solution here: http://stackoverflow.com/questions/1805808/how-do-i-scroll-a-row-of-a-table-into-view-element-scrollintoview-using-jquery
                                              :id (str table-id "-selected")
                                              }
                                         (into-array
                                          (for [[k {:keys [href]}] cols]
                                            (let [k (if (vector? k) k (vector k))]
                                              (dom/td nil (if href
                                                            (dom/a #js {:href (get row href)} (get-in row k))
                                                            (get-in row k)))))))))))))))

(defn sensor-table [cursor owner {:keys [histkey path]}]
  (reify
    om/IRender
    (render [_]
      ;; Select the first row
      ;;(put! out {:type :row-selected :row (first (om/get-state owner :data))})
      (let [cols (get-in cursor [:tables :sensors :header :cols])
            table-id (str (name histkey) "-table")]
        (dom/table #js {:id table-id
                        :className "table table-bordered hecuba-table "} ;; table-hover table-stripedso,
                   (dom/thead nil
                              (dom/tr nil
                                      (into-array
                                       (for [[_ {:keys [label]}] cols]
                                         (dom/th nil label)))))
                   (dom/tbody nil
                              (into-array
                               (for [{:keys [type deviceId] :as row} (-> cursor :data
                                                                   (cond-> path path))]

                                 (let [id (str type "-" deviceId)]
                                   ;; TODO clojurefy ids
                                   (dom/tr #js
                                           {:onClick
                                            (fn [_ _ ]
                                              (prn "Clicked sensor: " id)
                                              (om/update! cursor [:tables :sensor :selected] id)
                                              (om/update! cursor [:chart :sensor] id)
                                              (history/update-token-ids! history histkey id))
                                            :className (when (= id (:selected (:sensor (:tables cursor)))) "row-selected")
                                            :id (str table-id "-selected") 
                                            }
                                           (into-array
                                            (for [[k {:keys [href]}] cols]
                                              (let [k (if (vector? k) k (vector k))]
                                                (dom/td nil (if href
                                                              (dom/a #js {:href (get row href)} (get-in row k))
                                                              (get-in row k))))))))))))))))

(defn date-picker
  [cursor owner {:keys [histkey]}]
  (reify
    om/IRender
    (render [_]
      (dom/table #js {:id "date-picker"}
                 (dom/tr nil
                         (dom/td nil 
                                 (dom/h4 nil "Start: ")
                                 (dom/input #js
                                            {:type "text"
                                             :id "dateFrom"
                                             :ref "dateFrom"}))
                         (dom/td nil
                                 (dom/h4 nil "End: ")
                                 (dom/input #js
                                            {:type "text"
                                             :id "dateTo"
                                             :ref "dateTo"}))
                         (dom/td nil
                                 (dom/h4 nil)
                                 (dom/button #js {:type "button"
                                                  :onClick (fn [e]
                                                             (let [start (-> (om/get-node owner "dateFrom")
                                                                             .-value)
                                                                   end   (-> (om/get-node owner "dateTo")
                                                                             .-value)
                                                                   range (str start ";" end)]
                                                               (history/set-token-search! history [start end])
                                                               (om/update! cursor [:chart :range] {:start-date start
                                                                                                   :end-date end})
                                                               ))}
                                             "Select dates")))))))

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

(defn device-detail [{:keys [selected data] :as cursor} owner]
  (om/component
   (let [row (first (filter #(= (:id %) selected) data))]
     (let [{:keys [description name
                   latitude longitude]} (:location row)]
       (dom/div nil
                (dom/h3 nil (apply str  "Device Detail "  (interpose \/ (remove nil? [description name])))) ;; TODO add a '-'
                (dom/p nil (str "Latitude: " latitude))
                (dom/p nil (str "Longitude: " longitude)))))))

(defn title-for [cursor & {:keys [title-key] :or {title-key :name}}]
  (some->> (get (row-for cursor) title-key)
           (str " - ")))

(defn programmes-tab [data owner]
  (let [{:keys [programmes projects properties devices measurements]} (:tables data)]
    (reify
      om/IWillMount
      (will-mount [_]
        (let [m           (mult history-channel)
              tap-history #(tap m (chan))]

          ;; attach a go-loop that fires ajax requests on history changes to each table

          ;;TODO still some cruft to tidy here:  /3 and singular/plural bunk.
          (ajax (tap-history) programmes {:template      "/3/programmes/"
                                           :content-type  "application/edn"
                                           :selection-key :programme})
          (ajax (tap-history) projects {:template      "/3/programmes/:programme/projects"
                                        :content-type  "application/edn"
                                        :selection-key :project})
          (ajax (tap-history) properties {:template      "/3/projects/:project/properties"
                                          :content-type  "application/json"
                                          :selection-key :property})
          (ajax (tap-history) devices {:template      "/3/entities/:property/devices"
                                       :content-type  "application/json"
                                       :selection-key :device})
          (ajax (tap-history) data {:template "/3/entities/:property/devices/:device"
                                    :content-type "application/json"
                                    :selection-key :foo})
          (chart-ajax (tap-history) (:chart data) {:template "/3/entities/:property/devices/:device/measurements?startDate=:start-date&endDate=:end-date"
                                         :content-type  "application/json"
                                         :selection-key :range})
          (ajax (tap-history) measurements {:template      "/3/entities/:property/devices/:device/measurements"
                                            :content-type  "application/json"
                                            :selection-key :measurement})
          ))
      om/IRender
      (render [_]

        ;; Note dynamic titles for each of the sections.

        ;; TODO sort out duplication here, wrap (on/build table ...) calls probably.
        ;;      we need to decide on singular/plural for entities. I vote singular.
        ;;
        (dom/div nil
                 (dom/h1 {:id "programmes"} (:title data))
                 (om/build table programmes {:opts {:histkey :programme}})
                 (dom/h2 {:id "projects"} (str  "Projects " (title-for programmes)))
                 (om/build table projects {:opts {:histkey :project}})
                 (dom/h2 {:id "properties"} (str "Properties" (title-for projects)))
                 (om/build table properties {:opts {:histkey :property}})
                 (dom/h2 {:id "devices"} "Devices" (title-for properties :title-key :addressStreetTwo))
                 (om/build table devices {:opts {:histkey :device}})
                 (om/build device-detail devices)
                 (dom/h2 {:id "sensors"} "Sensors")
                 (om/build sensor-table data {:opts {:histkey :sensor :path :readings}})
                  (dom/h2 nil "Chart")
                 (dom/div #js {:id "date-picker"})
                 (dom/p nil "Note: When you select something to plot on a given axis, you will only be able to plot other items of the same unit on that axis.")
                 (om/build date-picker data {:opts {:histkey :range}})
                 (dom/div #js {:id "chart"})
                 (om/build chart/chart-figure (:chart data)))))))

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

(defn FOO []
  (let [path [:tab-container :tabs 3 :tables :sensors :data]]
    (println "AM:" (type(-> @app-model (get-in path))))
    (println "AM:" (pr-str (-> @app-model (get-in path))))))

(om/root
    (let [{:keys [in out] :as pair} (make-channel-pair)]
      (go-loop []
               (when-let [n (<! out)]
                 (handle-left-nav n)
                 (recur)))
      (nav/nav pair))
    app-model
    {:target (.getElementById js/document "hecuba-nav")})

(om/root (tab-container {:about about-tab
                         :programmes programmes-tab
                         :charts charts-tab
                         :documentation documentation-tab
                         :users users-tab})
         app-model
         {:target (.getElementById js/document "hecuba-tabs")})
