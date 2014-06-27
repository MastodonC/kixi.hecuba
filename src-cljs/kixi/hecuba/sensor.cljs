(ns kixi.hecuba.sensor
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [ajax.core :refer (POST)]
              [kixi.hecuba.bootstrap :as bs]
              [kixi.hecuba.history :as history]
              [sablono.core :as html :refer-macros [html]]))

(defn status-label [status]
  (if (= status "OK")
    [:span {:class "label label-success"} status]
    [:span {:class "label label-danger"} status]))

(defn table [data owner {:keys [histkey path]}]
  (reify
    om/IRender
    (render [_]
      ;; Select the first row
      ;;(put! out {:type :row-selected :row (first (om/get-state owner :data))})
      (let [sensors (:sensors data)
            chart   (:chart data)
            cols    (get-in sensors [:header :cols])
            history (om/get-shared owner :history)
            table-id "sensors-table"]

        (html
         [:table {:className "table table-hover"}
          [:thead
           [:tr [:th "Type"] [:th "Unit"] [:th "Period"] [:th "Device"] [:th "Status"]]]
          [:tbody
           (for [row (sort-by :type (-> sensors :data :readings))]
             (let [{:keys [deviceId type unit period status]} row
                   id (str type "-" deviceId)]
               [:tr {:onClick (fn [_ _]
                                (om/update! sensors :selected id)
                                (om/update! chart :sensor id)
                                (om/update! chart :unit unit)
                                (history/update-token-ids! history :sensors id))
                     :className (if (= id (:selected sensors)) "success")
                     :id (str table-id "-selected")}
                [:td type]
                [:td unit]
                [:td period]
                [:td deviceId]
                [:td (status-label status)]]))]])))))


(defn sensors-select-table [cursor owner {:keys [path]}]
  (reify
    om/IRender
    (render [_]
      (let [cols    (get-in cursor [:header :cols])
            members (get-in cursor [:sensor-group :members])]
        (dom/table
         #js {:className "table table-condensed hecuba-table col-lg-10"
              :id "sensor-select-table"
              } ;; table-hover table-stripedso,
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
                         (dom/tr #js {:className (if (> (rand) 0.5) "has-error" "")}
                                 (into-array
                                  (for [[k {:keys [checkbox]}] cols]
                                    (let [k (if (vector? k) k (vector k))]
                                      (dom/td nil (if checkbox
                                                    (bs/checkbox (get-in row k)
                                                                 cursor
                                                                 (fn [e] (if (.. e -target -checked)
                                                                          (om/transact! members #(conj % id))
                                                                          (om/transact! members #(disj % id)))))
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
       (bs/form-horizontal-with-validation
        (dom/div
         #js {:className "col-lg-12"}
         (om/build sensors-select-table sensor-select))
        (dom/div
         #js {:className "form-group"}
         (bs/with-control-label "Name"
           (bs/text-field (fn [e] (om/update! sensor-select
                                             [:sensor-group :name]
                                             (.. e -target -value)))))
         (bs/with-control-label "Type"
           (bs/dropdown "type" ["One" "Two" "Three"])))))
      (dom/div
       #js {:className "modal-footer"}
       (bs/default-button "Close" "modal")
       (bs/primary-button "Define" "modal" (fn [e]
                                             ;; (println "POSTING!")
                                             (POST (str "/4/entities/" (:selected @properties) "/datasets/")
                                                   {:params (:sensor-group @sensor-select)
                                                    ;; :handler #(println "TODO: Refresh the devices table somehow!")
                                                    ;; :error-handler #(println "Error!" %)
                                                    :keywords? true})
                                             ;; (println "POSTED! " (:selected @properties))
                                             )))
      )))))

(defn define-data-set-button [cursor owner]
  (om/component
   (dom/div nil
            (dom/a #js {:href "#"
                        :className (str "btn btn-primary btn-large" (when (:selected cursor) "disabled"))
                        :data-toggle "modal"
                        :data-target "#sensor-selection-dialog"}
                   (dom/i {:className "icon-white icon-edit"})
                   "Define data set"))))
