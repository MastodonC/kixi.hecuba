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
      (GET uri
           (-> {:handler  (fn [x]
                            (om/update! data :data x)
                            (om/update! data :selected new-selected))
                :headers {"Accept" content-type}
                :response-format :text}
               (cond-> (= content-type "application/json")
                       (merge {:response-format :json :keywords? true})))))
    (recur)))

(defn selected-range-change
  [selected selection-key template {{ids :ids search :search} :args}]
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
            sensor-id             (get ids :sensor) 
            [type device-id]      (str/split sensor-id #"-")
            url                   (str "/3/entities/" entity-id "/devices/" device-id "/measurements/" type "?startDate=" start-date "&endDate=" end-date)]
        (om/update! data :sensor sensor-id)
        (when (and (not (empty? start-date))
                   (not (empty? end-date))
                   (not (nil? device-id))
                   (not (nil? entity-id))
                   (not (nil? type)))
          (GET url {:handler #(om/update! data :measurements %)
                    :headers {"Accept" "application/json"}
                    :response-format :json
                    :keywords? true}))))
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

(defn sensor-table [{:keys [tables chart]} owner {:keys [histkey path]}]
  (reify
    om/IRender
    (render [_]
      ;; Select the first row
      ;;(put! out {:type :row-selected :row (first (om/get-state owner :data))})
      (let [{sensors :sensors} tables
            cols               (get-in sensors [:header :cols])
            table-id           (str (name histkey) "-table")]

        (dom/table
         #js {:id table-id
              :className "table table-bordered hecuba-table "} ;; table-hover table-stripedso,
         (dom/thead nil
                    (dom/tr nil
                            (into-array
                             (for [[_ {:keys [label]}] cols]
                               (dom/th nil label)))))
         (dom/tbody nil
                    (into-array
                     (for [{:keys [type deviceId] :as row} (-> sensors :data
                                                               (cond-> path path))]
                       (let [id (str type "-" deviceId)]
                         ;; TODO clojurefy ids
                         (dom/tr #js
                                 {:onClick (fn [_ _ ]
                                             (prn "Clicked sensor: " id)
                                             (om/update! sensors :selected id)
                                             (om/update! chart :sensor id)
                                             (history/update-token-ids! history histkey id))
                                  :className (when (= id (:selected sensors)) "row-selected")
                                  :id (str table-id "-selected")}
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
      (dom/div nil 
               (dom/div #js {:className "container"}
                        (dom/div #js {:className "col-sm-3"}
                                 (dom/div #js {:className "form-group"}
                                          (dom/div #js {:className "input-group date"
                                                        :id "dateFrom" }
                                                   (dom/input #js
                                                              {:type "text"
                                                               :ref "dateFrom"
                                                               :data-format "DD-MM-YYYY HH:mm"
                                                               :className "form-control"
                                                               :placeholder "Start date"})
                                                   (dom/span #js {:className "input-group-addon"}
                                                             (dom/span #js {:className "glyphicon glyphicon-calendar"})))))
                        (dom/div #js {:className "col-sm-3"}
                                 (dom/div #js {:className "form-group"}
                                          (dom/div #js {:className "input-group date" 
                                                        :id "dateTo"}
                                                   (dom/input #js
                                                              {:type "text"
                                                               :data-format "DD-MM-YYYY HH:mm"
                                                               :ref "dateTo"
                                                               :className "form-control"
                                                               :placeholder "End date"})
                                                   (dom/span #js {:className "input-group-addon"}
                                                             (dom/span #js {:className "glyphicon glyphicon-calendar"})))))
                        (dom/button #js {:type "button"
                                         :className  "btn btn-primary btn-large"
                                         :onClick (fn [e]
                                                    (let [start (-> (om/get-node owner "dateFrom")
                                                                    .-value)
                                                          end   (-> (om/get-node owner "dateTo")
                                                                    .-value)
                                                          range (str start ";" end)]
                                                      (history/set-token-search! history [start end])
                                                      (om/update! cursor [:chart :range] {:start-date start
                                                                                          :end-date end})))}
                                    "Select dates"))))))

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
   (let [row      (first (filter #(= (:id %) selected) data))]
     (let [{:keys [description name
                   latitude longitude]} (:location row)]
       (dom/div nil
                (dom/h3 nil (apply str  "Device Detail "  (interpose \/ (remove nil? [description name])))) ;; TODO add a '-'
                (dom/p nil (str "Latitude: " latitude))
                (dom/p nil (str "Longitude: " longitude)))))))

(defn title-for [cursor & {:keys [title-key] :or {title-key :name}}]
  (some->> (get (row-for cursor) title-key)
           (str " - ")))

(defn sensor-selection-button [cursor owner]
  (om/component
   (dom/div nil
            (dom/a #js {:href "#"
                        :className (str "btn btn-primary btn-large" (when (:selected cursor) "disabled"))
                        :data-toggle "modal"
                        :data-target "#sensor-selection-dialog"
                    }
                   (dom/i {:className "icon-white icon-edit"})
                   "Define data set"))))

(def truthy? (complement #{"false"}))

(defn new-checkbox [id v cursor]
  (dom/div #js {:className "checkbox"
                ;; TODO hack alert!!
                :style {:margin 0}}
           (dom/label #js {:className "checkbox-inline"}
                      (dom/input #js {:type "checkbox"
                                      :value v
                                      :onClick (fn [e] (let [v (truthy? (.. e -target -checked))]
                                                        (if v
                                                          (om/transact! cursor :sensor-group #(conj % id))
                                                          (om/transact! cursor :sensor-group #(disj % id)))))}))))

(defn render-row [row cols id-fn cursor]
  (into-array
   (for [[k {:keys [checkbox]}] cols]
     (let [k (if (vector? k) k (vector k))
           v (get-in row k)
           id (id-fn row)]
       (dom/td
        nil
        (if checkbox (new-checkbox id v cursor) v))))))

(defn sensor-select-table [cursor owner]
  (om/component
   (let [cols (get-in cursor [:header :cols])]
     (dom/table
      #js {:id "sensor-select-table"
           :className "table table-bordered hecuba-table "} ;; table-hover table-stripedso,
      (dom/thead
       nil
       (dom/tr nil
               (into-array
                (for [[_ {:keys [label]}] cols]
                  (dom/th nil label)))))
      (dom/tbody
       nil
       (into-array
        (for [row (:data cursor)]
          (dom/tr
           nil
           (render-row row cols #(str (:type %) "-" (:deviceId %)) cursor)))))))))

(defn button
  ([text kind dismiss]
     (button text kind dismiss nil))
  ([text kind dismiss on-click]
     (dom/button #js {:type "button"
                      :className (str "btn btn-" kind)
                      :data-dismiss "modal"
                      :onClick on-click}
                 text)))

(defn primary-button
  ([text dismiss]
     (primary-button text dismiss nil))
  ([text dismiss on-click]
     (button text "primary" dismiss on-click)))

(defn default-button
  ([text dismiss]
     (default-button text dismiss nil))
  ([text dismiss on-click]
     (button text "default" dismiss on-click)))

(defn sensor-selection-dialog [{:keys [sensor-select properties]} owner]
  (om/component
   (dom/div
    #js {:id "sensor-selection-dialog"
         :className "modal fade"}
    (dom/div
     #js {:className "modal-dialog"}
     (dom/div
      #js {:className "modal-content"}
      (dom/div
       #js {:className "modal-header"}
       (dom/button
        #js {:type "button"
             :className "close"
             :data-dismiss "modal"
             :aria-hidden "true"})
       (dom/h3
        #js {:className "modal-title"}
        "Define data set"))
      (dom/div
       #js {:className "modal-body"}
       (dom/label nil "Sensors")
       (om/build sensor-select-table sensor-select)
       (dom/div
        #js {:className "form-group has-feedback"}
        (dom/label nil "Data set name")
        (dom/input
         #js {:className "form-control"
              :type "text"
              :onBlur (fn [e] (println "Changed" (om/update! sensor-select :data-set-name (.. e -target -value))))})))
      (dom/div
       #js {:className "modal-footer"}
       (default-button "Close" "modal")
       (primary-button "Define" "modal" (fn [e]
                                          (.preventDefault e)
                                          (POST (str "/3/entities/" (:selected @properties) "/datasets")
                                                {:params (select-keys @sensor-select [:sensor-group :data-set-name])
                                                 :handler #(println "Yah!")
                                                 :error-handler #(println "Error!")
                                                 :response-format "application/edn"
                                                 :keywords? true})))))))))

(defn sensor-selection [data owner]
  (om/component
   (dom/div nil
            (dom/a {:href "#"
                    :className "btn btn-primary btn-large"
                    }
                   (dom/i {:className "icon-white icon-edit"})
                   "Group Sensors"))))

(defn programmes-tab [data owner]
  (let [{:keys [programmes projects properties devices sensors measurements sensor-select]} (:tables data)]
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
          (ajax (tap-history) sensors {:template "/3/entities/:property/devices/:device"
                                    :content-type "application/json"
                                    :selection-key :sensor})
          (ajax (tap-history) sensor-select {:template     "/3/entities/:property/sensors"
                                             :content-type "application/json"})
          (ajax (tap-history) measurements {:template      "/3/entities/:property/devices/:device/measurements"
                                            :content-type  "application/json"
                                            :selection-key :measurement})
          (chart-ajax (tap-history) (:chart data) {:template "/3/entities/:property/devices/:device/measurements?startDate=:start-date&endDate=:end-date"
                                                   :content-type  "application/json"
                                                   :selection-key :range})
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
                 (dom/h2 {:id "sensors"} "Sensors" (title-for devices))
                 (om/build sensor-table data {:opts {:histkey :sensor :path :readings}})
                 (om/build sensor-selection-button data)
                 (dom/h2 nil "Chart")
                 (dom/div #js {:id "date-picker"})
                 (dom/p nil "Note: When you select something to plot on a given axis, you will only be able to plot other items of the same unit on that axis.")
                 (om/build date-picker data {:opts {:histkey :range}})
                 (dom/div #js {:id "chart"})
                 (om/build chart/chart-figure (:chart data))
                 (om/build sensor-selection-dialog sensor-select))))))

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
  (let [path [:tab-container :tabs 3 :chart]]
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
