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
            [cljs-time.format :as tf]
            [cljs-time.core :as t]
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


;;;;;;;;;;;;;;;;;;;;;; Datetimepicker ;;;;;;;;;;;;;;;;;;;;

(defn- invalid-dates [selection start end]
  (put! selection {:selection-key :range
                   :value {:range {:start-date start :end-date end}
                           :message "End date must be later than start date."}}))

(defn- valid-dates [selection start end]
  (put! selection {:selection-key :range
                   :value {:range {:start-date start :end-date end}
                           :message ""}}))

(defn evaluate-dates
  [selection start-date end-date]
  (let [formatter (tf/formatter "yyyy-MM-dd HH:mm:ss")
        start     (tf/parse formatter start-date)
        end       (tf/parse formatter end-date)]
    
    (cond
     (t/after? start end)       (invalid-dates selection start-date end-date)
     (= start-date end-date)    (invalid-dates selection start-date end-date)
     (not= start-date end-date) (valid-dates selection start-date end-date))))

(defn date-picker [data owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [selection]}]
      (dom/div nil
               (dom/div #js {:className "container"}
                        (dom/div #js {:className "col-sm-3"}
                                 (dom/div #js {:className "form-group"}
                                          (dom/div #js {:className "input-group date" :id "dateFrom" }
                                                   (dom/input #js
                                                              {:type "text"
                                                               :ref "dateFrom"
                                                               :data-format "YYYY-MM-DD HH:mm:ss"
                                                               :className "form-control"
                                                               :placeholder "Start date"
                                                               :value (if (empty? (get-in data [:chart :range]))
                                                                        ""
                                                                        (get-in data [:chart :range :start-date]))})
                                                   (dom/span #js {:className "input-group-addon"}
                                                             (dom/span #js {:className "glyphicon glyphicon-calendar"})))))
                        (dom/div #js {:className "col-sm-3"}
                                 (dom/div #js {:className "form-group"}
                                          (dom/div #js {:className "input-group date" :id "dateTo"}
                                                   (dom/input #js
                                                              {:type "text"
                                                               :data-format "YYYY-MM-DD HH:mm:ss"
                                                               :ref "dateTo"
                                                               :className "form-control"
                                                               :placeholder "End date"
                                                               :value (if (empty? (get-in data [:chart :range]))
                                                                        ""
                                                                        (get-in data [:chart :range :end-date]))})
                                                   (dom/span #js {:className "input-group-addon"}
                                                             (dom/span #js {:className "glyphicon glyphicon-calendar"})))))
                        (dom/button #js {:type "button"
                                         :id "select-dates-btn"
                                         :className  "btn btn-primary btn-large"
                                         :onClick
                                         (fn [e]
                                           (let [start (-> (om/get-node owner "dateFrom") .-value)
                                                 end   (-> (om/get-node owner "dateTo") .-value)]
                                             (evaluate-dates selection start end)))}
                                    "Select dates"))))))


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
        data             (into [] measurements)
        unit             (get-in cursor [:unit])
        dimple-chart     (.setBounds (Chart. svg) "5%" "15%" "80%" "50%")
        x                (.addCategoryAxis dimple-chart "x" "timestamp")
        y                (.addMeasureAxis dimple-chart "y" "value")
        s                (.addSeries dimple-chart "sensor" js/dimple.plot.line (clj->js [x y]))]
    (aset s "data" (clj->js data))
    (.addLegend dimple-chart "15%" "10%" "20%" "10%" "right")
    (.draw dimple-chart)
    (.text (.-titleShape y) unit)
    (let [n (count data)] (clean-axis x (Math/round (+ (/ n 50) 0.5))))
    (.attr (.selectAll (.-shapes x) "text") "transform" (fn [d]
                                                          (let [transform (.attr (.select d3 (js* "this")) "transform")]
                                                            (when-not (empty?
                                                                       transform)
                                                              (str transform " rotate(-45)")))))))

(defn chart [data owner]
  (reify
    om/IWillMount
    (will-mount [_])
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
      (let [measurements (get-in data [:measurements])
            range        (get-in data [:range])]
        (when-not (empty? measurements)
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

(defn sensor-row [selection]
  (fn [the-item owner]
    (om/component
     (let [type      (get the-item :type)
           device-id (get the-item :device-id)
           entity-id (get the-item :entity-id)
           unit      (get the-item :unit)
           history   (om/get-shared owner :history)]
       (dom/tr #js {:className "gradeA" :width "100%" :style (if (:hidden the-item) #js {:display "none"} {})} 
               (dom/td nil type)
               (dom/td nil device-id)
               (dom/td nil entity-id)
               (dom/td nil unit)
               (dom/td nil (dom/div #js {:className "checkbox"}
                                    (dom/input
                                     #js {:type "checkbox"
                                          :value type
                                          :onChange
                                          (fn [e]
                                            (let [checked   (= (.. e -target -checked) true)
                                                  new-id    (string/join "-" [device-id type entity-id])]
                                              (put! selection {:checked checked
                                                               :selection-key :sensors
                                                               :value new-id})))}))))))))

(defn sensors-select-table [data owner]
  (reify
    om/IWillMount
    (will-mount [_])
    om/IRenderState
    (render-state [_ {:keys [selection]}]
      (let [sensors (:data data)]
        (dom/table #js {:className "table table-striped table-bordered"}
                   (dom/thead nil (dom/tr nil (dom/th nil "Type")
                                          (dom/th nil "Device Id")
                                          (dom/th nil "Entity Id")
                                          (dom/th nil "Unit")
                                          (dom/th nil "Select")))
                   (dom/tbody nil (om/build-all (sensor-row selection) sensors)))))))



;;;;;;;;;;;;;;;;;;;;;;;;;;; Properties form ;;;;;;;;;;;;;;;;;;;;;;;

(defn property-row [selection]
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
                                                                  (put! selection {:checked checked
                                                                                   :selection-key :properties
                                                                                   :value id})))}))))))))

(defn properties-select-table [data owner]
  (reify
    om/IInitState
    (init-state [_] {:text ""})
    om/IRenderState
    (render-state [_ {:keys [text selection]}]
      (let [properties (:data data)]
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
                                   (om/build-all (property-row selection) properties
                                                 {:fn (fn [x]
                                                        (if-not (< (count text) 3)
                                                          (cond-> x
                                                                  (not (zero? (.indexOf (:addressStreetTwo x) text)))
                                                                  (assoc :hidden true))
                                                          x))
                                                  })))))))) ;; TODO allow to search by address, property code, if, project name




