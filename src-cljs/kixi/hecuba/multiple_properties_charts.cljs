(ns kixi.hecuba.multiple-properties-charts
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:refer-clojure :exclude [chars])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer (GET POST)]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.history :as history]
            [clojure.string :as string]
            [cljs.reader :as reader]
            [cljs.core.async :refer [<! chan put! sliding-buffer]]))

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

(defn sensor-row [clicked-chan]
  (fn [the-item owner]
    (om/component
     (let [type      (get the-item "type")
           device-id (get the-item :device-id)
           entity-id (get the-item :entity-id)
           unit      (get the-item "unit")]
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
                                                                  (prn "checked: " type)
                                                                  ))}))))))))

(defn sensors-select-table [data owner]
  (reify
    om/IInitState
    (init-state [_] {:selected {}})
    om/IWillMount
    (will-mount [_]
      (let [clicked-properties (om/get-state owner [:clicked-devices])]
        (go (while true
              (let [sel     (<! clicked-properties)
                    id      (-> sel :id)
                    checked (-> sel :checked)
                    payload (-> sel :data)]
                (if checked
                  (om/update! data [:sensors] (concat (:sensors @data) payload))
                  (om/update! data [:sensors] (remove #(= (:device-id %) id) (:sensors @data)))))))))
    om/IRenderState
    (render-state [_ {:keys [selected]}]
      (let [sensors (:sensors data)]
        (dom/table #js {:className "table table-striped table-bordered"}
                   (dom/thead nil (dom/tr nil (dom/th nil "Type")
                                          (dom/th nil "Device Id")
                                          (dom/th nil "Entity Id")
                                          (dom/th nil "Unit")
                                          (dom/th nil "Select")))
                   (dom/tbody nil (om/build-all (sensor-row selected) sensors)))))))

(defn device-row [clicked-chan]
  (fn [the-item owner]
    (om/component
     (let [description    (:description the-item)
           id             (:id the-item)
           entity-id      (:entity-id the-item)]
       (dom/tr #js {:className "gradeA" :width "100%" :style (if (:hidden the-item) #js {:display "none"})} 
               (dom/td nil id)
               (dom/td nil entity-id)
               (dom/td nil description)
               (dom/td nil (:name the-item))
               (dom/td nil (dom/div
                            #js {:className "checkbox"}
                            (dom/input 
                             #js {:type "checkbox"
                                  :value id
                                  :onChange (fn [e]
                                              (let [checked (= (.. e -target -checked) true)]
                                                (put! clicked-chan {:id id
                                                                    :checked checked
                                                                    :data (when checked 
                                                                            (map #(assoc % :device-id id :entity-id entity-id) (:readings the-item)))})))}))))))))

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


(defn devices-select-table [data owner]
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
                  (om/update! data [:devices] (concat (:devices @data)
                                                      (map (fn [[k v]]
                                                             (merge {:id k} 
                                                                    (reader/read-string v))) payload)))
                  (om/update! data [:devices] (remove #(= (:entity-id %) id) (:devices @data)))))))))
    om/IRenderState
    (render-state [_ {:keys [clicked-devices]}]
      (let [devices (:devices data)]
        (dom/table #js {:className "table table-striped table-bordered"}
                   (dom/thead nil (dom/tr nil (dom/th nil "Id")
                                          (dom/th nil "Property")
                                          (dom/th nil "Description")
                                          (dom/th nil "Name")
                                          (dom/th nil "Select")))
                   (dom/tbody nil (om/build-all (device-row clicked-devices) devices)))))))

;; TOFIX When new property is selected, do not clear existing checked boxes
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




