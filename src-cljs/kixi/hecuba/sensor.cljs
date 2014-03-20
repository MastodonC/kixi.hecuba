(ns kixi.hecuba.sensor
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [ajax.core :refer (POST)]
              [kixi.hecuba.bootstrap :as bs]
              [kixi.hecuba.history :as history]))

(defn render-row [row cols id-fn cursor]
  (prn "row: " row)
  (into-array
   (for [[k {:keys [checkbox]}] cols]
     (let [k (if (vector? k) k (vector k))
           v (get-in row k)
           id (id-fn row)]
       (dom/td
        nil
        (if checkbox (bs/checkbox id v cursor) v))))))

(defn table [{:keys [tables chart]} owner {:keys [history histkey path]}]
  (reify
    om/IRender
    (render [_]
      ;; Select the first row
      ;;(put! out {:type :row-selected :row (first (om/get-state owner :data))})
      (let [{sensors :sensors} tables
            cols               (get-in sensors [:header :cols])]

        (dom/table
         #js {:className "table table-bordered hecuba-table "} ;; table-hover table-stripedso,
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
                                             (om/update! sensors :selected id)
                                             (om/update! chart :sensor id)
                                             (history/update-token-ids! history histkey id)
                                             )
                                  :className (when (= id (:selected sensors)) "row-selected")}
                                 (into-array
                                  (for [[k {:keys [href]}] cols]
                                    (let [k (if (vector? k) k (vector k))]
                                      (dom/td nil (if href
                                                    (dom/a #js {:href (get row href)} (get-in row k))
                                                    (get-in row k))))))))))))))))


(defn sensors-select-table [cursor owner {:keys [path]}]
  (reify
    om/IRender
    (render [_]
      (let [cols               (get-in cursor [:header :cols])]
        (dom/table
         #js {:className "table table-bordered hecuba-table "} ;; table-hover table-stripedso,
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
                         (dom/tr nil
                                 (into-array
                                  (for [[k {:keys [checkbox]}] cols]
                                    (let [k (if (vector? k) k (vector k))]
                                      (dom/td nil (if checkbox
                                                    (bs/checkbox type (get-in row k) cursor)
                                                    (get-in row k))))))))))))))))



(defn selection-dialog [{:keys [sensor-select properties]} owner {:keys [id on-click]}]
  (om/component
   (dom/div
    #js {:id id
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
       (om/build sensors-select-table sensor-select)
       (dom/div
        #js {:className "form-group has-feedback"}
        (dom/label nil "Data set name")
        (dom/input
         #js {:className "form-control"
              :type "text"
              :onBlur (fn [e] (println "Changed" (om/update! sensor-select :data-set-name (.. e -target -value))))})))
      (dom/div
       #js {:className "modal-footer"}
       (bs/default-button "Close" "modal")
       (bs/primary-button "Define" "modal" (fn [e]
                                             (.preventDefault e)
                                             (POST (str "/3/entities/" (:selected @properties) "/datasets")
                                                   {:params (select-keys @sensor-select [:sensor-group :data-set-name])
                                                    :handler #(println "Yah!")
                                                    :error-handler #(println "Error!")
                                                    :response-format "application/edn"
                                                    :keywords? true})))))))))

(defn define-data-set-button [cursor owner]
  (om/component
   (dom/div nil
            (dom/a #js {:href "#"
                        :className (str "btn btn-primary btn-large" (when (:selected cursor) "disabled"))
                        :data-toggle "modal"
                        :data-target "#sensor-selection-dialog"}
                   (dom/i {:className "icon-white icon-edit"})
                   "Define data set"))))
