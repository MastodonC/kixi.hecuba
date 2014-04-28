(ns kixi.hecuba.multiple-properties-charts
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:refer-clojure :exclude [chars])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [mrhyde.core :as mrhyde]
            [ajax.core :refer (GET POST)]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.history :as h]
            [kixi.hecuba.common :refer (interval)]
            [clojure.string :as string]
            [cljs.reader :as reader]
            [cljs.core.async :refer [<! chan put! sliding-buffer]]))

(def dimple (this-as ct (aget ct "dimple")))
(def d3 (this-as ct (aget ct "d3")))

(mrhyde/bootstrap)
(enable-console-print!)

(extend-type string
  ICloneable
  (-clone [s] (js/String. s)))

(extend-type number
  ICloneable
  (-clone [n] (js/Number. n)))

(defn change [e owner]
  (om/set-state! owner
                 :text (.. e -target -value)))


;;;;;;;;;;;;;;;;;;;;;;;;;; Chart ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Should be called after the draw function
(defn clean-axis [axis interval]
  (if (> (.-length (.-shapes axis)) 0)
    (let [del (atom 0)]
      (when (> interval 1)
        (let [text (.selectAll (.-shapes axis) "text")]
          (.each text (fn [d]
                        (when (not= (mod @del interval) 0)
                          (.remove (js* "this"))
                          (.each (.selectAll (.-shapes axis) "line") (fn [d2]
                                                                       (if (= d d2)
                                                                         (.remove (js* "this"))))))
                        (swap! del inc))))))))

(defn- draw-chart [cursor measurements]
  (let [Chart            (.-chart dimple)
        svg              (.newSvg dimple "#chart" "100%" 600)
        [type device-id] (-> (get-in cursor [:sensor])
                             str
                             (str/split #"-"))
        data             (into [] (map #(assoc % :id device-id) measurements))
        unit             (get-in cursor [:unit])
        dimple-chart     (.setBounds (Chart. svg) "5%" "15%" "80%" "50%")
        x                (.addCategoryAxis dimple-chart "x" "timestamp")
        y                (.addMeasureAxis dimple-chart "y" "value")
        s                (.addSeries dimple-chart type js/dimple.plot.line (clj->js [x y]))]
    (aset s "data" (clj->js data))
    (.addLegend dimple-chart "5%" "10%" "20%" "10%" "right")
    (.draw dimple-chart)
    (.text (.-titleShape y) unit)
    (let [n (count data)] (clean-axis x (Math/round (+ (/ n 50) 0.5))))
    (.attr (.selectAll (.-shapes x) "text") "transform" (fn [d]
                                                          (let [transform (.attr (.select d3 (js* "this")) "transform")]
                                                            (when-not (empty?
                                                                       transform)
                                                              (str transform " rotate(-45)")))))))

(defn update-measurements [data entity-id]
  (let [start-date (:start-date (:range @data)) 
        end-date   (:end-date (:range @data))]
    (prn "start-date and end-date: " start-date end-date)
    (when-not (empty? (or start-date end-date))
      (doseq [sensor (:sensors @data)]
        (let [[device-id type] (string/split sensor #"-")
              resource   (case (interval start-date end-date)
                           :raw "measurements"
                           :hourly-rollups "hourly_rollups"
                           :daily-rollups "daily_rollups")
              url              (str "/4/entities/" entity-id "/devices/" device-id "/"
                                    resource "/" type "?startDate=" start-date "&endDate=" end-date)]
          (GET url {:handler #(om/update! data :measurements (concat (:measurements @data) %))}))))))

(defn chart [data owner]
  (reify
    om/IInitState
    (init-state [_] {:selected {}})
    om/IWillMount
    (will-mount [_]
      (let [clicked-sensors (om/get-state owner [:clicked-sensors])]
        (go (while true
              (let [sel       (<! clicked-sensors)
                    id        (-> sel :id)
                    type      (-> sel :type)
                    checked   (-> sel :checked)
                    unit      (-> sel :unit)
                    entity-id (-> sel :entity-id)]
                (if checked
                  (do
                    (om/update! data [:sensors] (conj (:sensors @data) (string/join "-" [id type])))
                    (update-measurements data entity-id))
                  (do
                    (om/update! data [:sensors] (remove #{id} (:sensors @data)))
                    (update-measurements data entity-id))))))))
    om/IRender
    (render [_]
      (dom/div nil))
    om/IDidMount
    (did-mount [_]
      (dom/div nil))
    om/IDidUpdate
    (did-update [_ _ _]
      (let [n (.getElementById js/document "chart")]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (let [measurements (get-in data [:measurements :measurements])
            range        (get-in data [:range])]
        (prn "range: " range)
        (when-not (empty? measurements)
          (prn "measurements: " measurements)
          (draw-chart data measurements))))))


;;;;;;;;;;;;;;;;;;;; Sensors form ;;;;;;;;;;;;;;;;;;;;;;;

(defn sensors-data [payload]
  (let [devices (map (fn [[k v]]
                       (merge {:id k} (reader/read-string v))) payload)]
    (map (fn [device] (into {}
                            (map (fn [reading] 
                                   {:device-id (:id device)
                                    :type (get reading "type")
                                    :entity-id (:entity-id device)
                                    :unit (get reading "unit")
                                    :description (:description device)}) (:readings device)))) devices)))

(defn sensor-row [clicked-chan]
  (fn [the-item owner]
    (om/component
     (let [type      (get the-item :type)
           device-id (get the-item :device-id)
           entity-id (get the-item :entity-id)
           unit      (get the-item :unit)]
       (dom/tr #js {:className "gradeA" :width "100%" :style (if (:hidden the-item) #js {:display "none"} {})} 
               (dom/td nil type)
               (dom/td nil device-id)
               (dom/td nil entity-id)
               (dom/td nil unit)
               (dom/td nil (dom/div #js {:className "checkbox"}
                                    (dom/input #js {:type "checkbox"
                                                    :value type
                                                    :onChange (fn [e]
                                                                (let [checked (= (.. e -target -checked) true)]
                                                                  (put! clicked-chan {:id device-id
                                                                                      :type type
                                                                                      :checked checked
                                                                                      :unit unit
                                                                                      :entity-id entity-id})))}))))))))

(defn sensors-select-table [data owner]
  (reify
    om/IInitState
    (init-state [_] {:selected {}})
    om/IWillMount
    (will-mount [_]
      (let [clicked-properties (om/get-state owner [:clicked-properties])]
        (go (while true
              (let [sel     (<! clicked-properties)
                    id      (-> sel :id)
                    checked (-> sel :checked)
                    payload (-> sel :data)]
                (if checked
                  (om/update! data [:sensors] (concat (:sensors @data) (sensors-data payload)))
                  (om/update! data [:sensors] (remove #(= (:device-id %) id) (:sensors @data)))))))))
    om/IRenderState
    (render-state [_ {:keys [clicked-sensors]}]
      (let [sensors (:sensors data)]
        (dom/table #js {:className "table table-striped table-bordered"}
                   (dom/thead nil (dom/tr nil (dom/th nil "Type")
                                          (dom/th nil "Device Id")
                                          (dom/th nil "Entity Id")
                                          (dom/th nil "Unit")
                                          (dom/th nil "Select")))
                   (dom/tbody nil (om/build-all (sensor-row clicked-sensors) sensors)))))))



;;;;;;;;;;;;;;;;;;;;;;;;;;; Properties form ;;;;;;;;;;;;;;;;;;;;;;;

(defn property-row [clicked-chan]
  (fn [the-item owner]
    (om/component
     (let [code    (:propertyCode the-item)
           id      (:id the-item)]
       (dom/tr #js {:className "gradeA" :width "100%" :style (if (:hidden the-item) #js {:display "none"} {})} 
               (dom/td nil (:addressStreetTwo the-item))
               (dom/td nil code)
               (dom/td nil id)
               (dom/td nil (:projectId the-item))
               (dom/td nil (dom/div #js {:className "checkbox"}
                                    (dom/input #js {:type "checkbox"
                                                    :value code
                                                    :onChange (fn [e]
                                                                (let [checked (= (.. e -target -checked) true)]
                                                                  (put! clicked-chan {:id id
                                                                                      :checked checked
                                                                                      :data (when checked (reader/read-string (:devices the-item)))})))}))))))))


(defn properties-select-table [data owner]
  (reify
    om/IInitState
    (init-state [_] {:text ""})
    om/IRenderState
    (render-state [_ {:keys [text clicked-properties]}]
      (let [properties (:properties data)]
        (dom/div nil
                 (dom/h4 nil "Search for a property:")
                 (dom/form #js {:role "form"}
                           (dom/div #js {:className "form-group"}                     
                                    (dom/input #js {:type "text"
                                                    :className "form-control"
                                                    :ref "text-field"
                                                    :value text
                                                    :onChange #(change % owner)})))
                 (dom/p nil (dom/i nil "Address, Property code, Property ID, Project"))
                 (dom/table #js {:className "table table-striped table-bordered"}
                            (dom/thead nil (dom/tr nil (dom/th nil "Property Address")
                                                   (dom/th nil "Property Code")
                                                   (dom/th nil "Property Id")
                                                   (dom/th nil "Project")
                                                   (dom/th nil "Select")))
                            (apply dom/tbody nil 
                                   (om/build-all (property-row clicked-properties) properties 
                                                 {:fn (fn [x]
                                                        (if-not (< (count text) 3)
                                                          (cond-> x
                                                                  (not (zero? (.indexOf (:addressStreetTwo x) text)))
                                                                  (assoc :hidden true))
                                                          x))
                                                  })))))))) ;; TODO allow to search by address, property code, if, project name




