(ns kixi.hecuba.tabs.hierarchy.datasets
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :refer (static-text) :as bs]
            [ajax.core :refer [GET POST PUT]]
            [clojure.string :as string]
            [kixi.hecuba.common :refer (log) :as common]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; POST, PUT, DELETE and VALIDATE                                                        ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn post-new-dataset [event-chan refresh-chan owner cursor dataset property-id]
  (let [resource {:entity_id property-id
                  :operation (string/lower-case (:operation dataset))
                  :name (:name dataset)
                  :operands (:series dataset)}]
    (common/post-resource (str "/4/entities/" property-id "/datasets/")
                          resource
                          (fn [_]
                            (om/update! cursor {})
                            (put! refresh-chan {:event :datasets})
                            (put! event-chan {:event :adding-dataset :value false})
                            (put! event-chan {:event :editing-dataset :value false})
                            (put! event-chan {:event :alert :value {:status true
                                                                    :class "alert alert-success"
                                                                    :text "Dataset was created successfully."}}))
                          (fn [{:keys [status status-text]}]
                            (om/set-state! owner :alert {:status true
                                                         :class "alert alert-danger"
                                                         :text status-text})))))

(defn put-edited-dataset [event-chan refresh-chan owner cursor dataset entity_id dataset_id]
  (let [resource {:operation (string/lower-case (:operation dataset))
                  :name      (:name dataset)
                  :operands  (:series dataset)}]
    (common/put-resource (str "/4/entities/" entity_id "/datasets/" dataset_id)
                          resource
                          (fn [_]
                            (om/update! cursor {})
                            (put! refresh-chan {:event :datasets})
                            (put! event-chan {:event :adding-dataset :value false})
                            (put! event-chan {:event :editing-dataset :value false})
                            (put! event-chan {:event :alert :value {:status true
                                                                    :class "alert alert-success"
                                                                    :text "Dataset was edited successfully."}}))
                          (fn [{:keys [status status-text]}]
                            (om/set-state! owner :alert {:status true
                                                         :class "alert alert-danger"
                                                         :text status-text})))))

(defn delete-dataset [event-chan refresh-chan entity_id dataset]
  (let [{:keys [dataset_id device_id]} @dataset]
    (common/delete-resource (str "/4/entities/" entity_id "/datasets/" dataset_id)
                            (fn []
                              (om/update! dataset {})
                              (put! refresh-chan {:event :datasets})
                              (put! event-chan {:event :editing-dataset :value false})
                              (put! event-chan {:event :editing-dataset :value false})
                              (put! event-chan {:event :alert :value {:status true
                                                                      :class "alert alert-success"
                                                                      :text "Dataset was deleted successfully."}}))
                            (fn [{:keys [status status-text]}]
                              (put! event-chan {:event :alert :value {:status true
                                                                      :class "alert alert-danger"
                                                                      :text "Unable to delete dataset."}})))))


(defn valid-dataset? [dataset]
  (let [{:keys [operation series name]} dataset]
    (and (and (seq operation) (seq series))
         (= (count series) (count (set series)))
         (> (count series) 1)
         (seq name))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; HELPERS                                                                               ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def available-operations [{:display "Sum multiple series"      :value "sum"}
                           {:display "Subtract multiple series" :value "subtract"}
                           {:display "Divide multiple series"   :value "divide"}
                           {:display "Multiply series by field" :value "multiply-series-by-field"}
                           {:display "Divide series by field"   :value "divide-series-by-field"}
                           {:display "Select operation"         :value "none"}])

(def available-fields [{:display "Total occupancy"  :value "occupancy_total" :unit "occupancy"}
                       {:display "Total volume"     :value "total_volume" :unit "m3"}
                       {:display "Total area"       :value "gross_internal_area" :unit "m3"}
                       {:display "Electricity cost" :value "electricity_cost" :unit "£"}
                       {:display "Gas cost"         :value "gas_cost" :unit "£"}
                       {:display "Select field"     :value "none"}])

(defn update-series
  "Turn set of operands into key value pairs format used in dropdowns, e.g. :series1 \"fd323ds\" "
  [dataset]
  (let [{:keys [operands operation]} dataset]
    (cond
     (some #{operation} #{"sum" "divide" "subtract"}) (merge dataset (apply merge (map-indexed (fn [idx itm] {(keyword (str "series" (+ idx 1))) itm}) operands)))
     :else (let [[field unit] (string/split (last operands) #"~")]
             (assoc dataset :series1 (first operands) :field field)))))

(defn error-handler [event-chan]
  (fn [{:keys [status status-text]}]
    (put! event-chan {:event :alert :value {:status true
                                            :class "alert alert-danger"
                                            :text status-text}})))

(defn enrich-series-for-dropdowns
  [device_id sensors]
  (let [clean-sensors (remove #(= (:device_id %) device_id) sensors)
        s (mapv #(assoc % :display (str (-> % :parent-device :description) " : " (:type %) " : " (:device_id %))
                        :value (:id %)) clean-sensors)]
    (concat s [{:display "Select series" :value "none"}])))

(defn alert [class body status id owner]
  [:div {:id id :class class :style {:display (if status "block" "none")}}
   [:button.close {:type "button"
                   :onClick (fn [e] (om/set-state! owner :alert {:status false}))}
    [:span {:class "fa fa-times"}]]
   body])

(defmulti selected-series (fn [dataset] (:operation dataset)))
(defmethod selected-series "sum" [dataset]
  (mapv name (vec (remove nil? (vals (select-keys dataset [:series1 :series2 :series3 :series4]))))))
(defmethod selected-series "subtract" [dataset]
  (mapv name (vec (remove nil? (vals (select-keys dataset [:series1 :series2]))))))
(defmethod selected-series "divide" [dataset]
  (mapv name (vec (remove nil? (vals (select-keys dataset [:series1 :series2]))))))
(defmethod selected-series "multiply-series-by-field" [dataset]
  (mapv name (vec (remove nil? (vals (select-keys dataset [:series1 :field]))))))
(defmethod selected-series "divide-series-by-field" [dataset]
  (mapv name (vec (remove nil? (vals (select-keys dataset [:series1 :field]))))))

(defn text-input-control [cursor owner {:keys [path id label required dropdown-chan]}]
  (om/component
   (html
    [:div.form-group
     [:label.control-label.col-md-2 {:for id} label]
     [:div {:class (str (if required "required " "") "col-md-10")}
      [:input {:default-value (get-in cursor path)
               :on-change #(let [value (.-value (.-target %))]
                             (put! dropdown-chan {:path path :value value}))
               :class "form-control"
               :type "text"}]]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DROPDOWNS                                                                             ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn dropdown [{:keys [default items]} owner {:keys [id required label path dropdown-chan]}]
  (om/component
   (html
    [:div.form-group
     [:label.control-label.col-md-2 {:for id} label]
     [:div {:class (str "col-md-10 " (when required "required"))}
      [:select.form-control
       {:default-value (or default "none") :id id
        :on-change (fn [e]
                     (let [v (.-value (aget (.-options (.-target e))
                                            (.-selectedIndex (.-options (.-target e)))))]
                       (put! dropdown-chan {:path path
                                            :value (if-not (= v "none") v nil)})))}
       (for [item items]
         [:option {:value (:value item)} (:display item)])]]])))

(defmulti dropdowns (fn [cursor owner opts] (:operation cursor)))

(defmethod dropdowns "sum" [{:keys [operation items selected-dataset]} owner {:keys [dropdown-chan]}]
  (om/component
    (html
     [:div
      (om/build dropdown {:default (:series1 selected-dataset) :items items}
                {:opts {:id "series1-dropdown" :required true :label "First Series"
                        :path [:series1] :dropdown-chan dropdown-chan}})
      (om/build dropdown {:default (:series2 selected-dataset) :items items}
                {:opts {:id "series2-dropdown" :required true :label "Second Series"
                        :path [:series2] :dropdown-chan dropdown-chan}})
      (om/build dropdown {:default (:series3 selected-dataset) :items items}
                {:opts {:id "series3-dropdown" :required false :label "Third Series"
                        :path [:series3] :dropdown-chan dropdown-chan}})
      (om/build dropdown {:default (:series4 selected-dataset) :items items}
                {:opts {:id "series4-dropdown" :required false :label "Fourth Series"
                        :path [:series4] :dropdown-chan dropdown-chan}})])))

(defmethod dropdowns "subtract" [{:keys [operation items selected-dataset]} owner {:keys [dropdown-chan]}]
  (om/component
   (html
    [:div
     (om/build dropdown {:default (:series1 selected-dataset) :items items}
               {:opts {:id "series1-dropdown" :required true :label "First Series"
                       :path [:series1] :dropdown-chan dropdown-chan}})
     (om/build dropdown {:default (:series2 selected-dataset) :items items}
               {:opts {:id "series2-dropdown" :required true :label "Second Series"
                       :path [:series2] :dropdown-chan dropdown-chan}})])))

(defmethod dropdowns "divide" [{:keys [operation items selected-dataset]} owner {:keys [dropdown-chan]}]
  (om/component
   (html
    [:div
     (om/build dropdown {:default (:series1 selected-dataset) :items items}
               {:opts {:id "series1-dropdown" :required true :label "First Series"
                       :path [:series1] :dropdown-chan dropdown-chan}})
     (om/build dropdown {:default (:series2 selected-dataset) :items items}
               {:opts {:id "series2-dropdown" :required true :label "Second Series"
                       :path [:series2] :dropdown-chan dropdown-chan}})])))

(defmethod dropdowns "multiply-series-by-field" [{:keys [operation items selected-dataset]} owner {:keys [dropdown-chan]}]
  (om/component
   (html
    [:div
     (om/build dropdown {:default (:series1 selected-dataset) :items items}
               {:opts {:id "series-dropdown" :required true :label "Series"
                       :path [:series1] :dropdown-chan dropdown-chan}})
     (om/build dropdown {:default (:field selected-dataset) :items available-fields}
               {:opts {:id "field-dropdown" :required true :label "Field"
                       :path [:field] :dropdown-chan dropdown-chan}})])))

(defmethod dropdowns "divide-series-by-field" [{:keys [operation items selected-dataset]} owner {:keys [dropdown-chan]}]
  (om/component
   (html
    [:div
     (om/build dropdown {:default (:series1 selected-dataset) :items items}
               {:opts {:id "series-dropdown" :required true :label "Series"
                       :path [:series1] :dropdown-chan dropdown-chan}})
     (om/build dropdown {:default (:field selected-dataset) :items available-fields}
               {:opts {:id "field-dropdown" :required true :label "Field"
                       :path [:field] :dropdown-chan dropdown-chan}})])))

(defmethod dropdowns :default [_ owner all-series]
  (om/component
   (html
    [:div])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Edit dataset

(defn edit-dataset-form [{:keys [series sensors selected-dataset]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:dropdown-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [dropdown-chan]} (om/get-state owner)
              {:keys [path value]}    (<! dropdown-chan)]
          (om/update! selected-dataset path (if (= path [:field])
                                              (str value "~" (-> (filter #(= (:value %) value) available-fields)
                                                                 first
                                                                 :unit))
                                              value)))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [entity_id dataset_id device_id operands operation]} selected-dataset
            refresh-chan   (om/get-shared owner :refresh)
            {:keys [event-chan edited-dataset-chan dropdown-chan]} (om/get-state owner)
            {:keys [status text]} (:alert state)]
          (html
           [:div
            (alert "alert alert-danger" [:p text] status "edit-dataset-form-alert" owner)
            [:h3 "Editing dataset"]
            [:form.form-horizontal {:role "form"}
             [:div.col-md-12
              [:div.form-group
               [:div.btn-toolbar
                [:button {:type "button"
                          :class "btn btn-success"
                          :onClick (fn [_]
                                     (let [dataset @selected-dataset
                                           series  (selected-series dataset)
                                           parsed-dataset  (-> dataset
                                                               (update-in [:operation] name)

                                                               (assoc :series series)
                                                               (dissoc :series1 :series2 :series3 :series4 :field :editable :members))]
                                       (if (valid-dataset? parsed-dataset)
                                         (put-edited-dataset event-chan refresh-chan owner selected-dataset
                                                             parsed-dataset entity_id dataset_id)
                                         (om/set-state! owner :alert {:status true
                                                                      :class "alert alert-danger"
                                                                      :text  " Please enter required dataset data."}))))} "Save"]
                [:button {:type "button"
                          :class "btn btn-danger"
                          :onClick (fn [_]
                                     (put! event-chan {:event :editing-dataset :value false})
                                     ;; deselect and clear edited cursor
                                     (put! edited-dataset-chan {:dataset {} :selected? false}))} "Cancel"]
                [:button {:type "button"
                          :class "btn btn-danger pull-right"
                          :onClick (fn [_]
                                     (delete-dataset event-chan refresh-chan entity_id selected-dataset))}
                 "Delete Dataset"]]]
              (bs/static-text selected-dataset :device_id "ID")
              (bs/static-text selected-dataset :name "Unique Name")
              (om/build dropdown {:default operation :items available-operations}
                        {:opts {:id "operation-dropdown" :required true :label "Operation"
                                :path [:operation] :dropdown-chan dropdown-chan}})
              (om/build dropdowns {:selected-dataset selected-dataset :items series :operation operation}
                        {:opts {:dropdown-chan dropdown-chan}})]]])))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Add dataset

(defn new-dataset-form [{:keys [new-dataset series property-id]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:dropdown-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [dropdown-chan]} (om/get-state owner)
              {:keys [path value] :as v}    (<! dropdown-chan)]
          (om/update! new-dataset path (if (= path [:field])
                                         (str value "~" (-> (filter #(= (:value %) value) available-fields)
                                                            first
                                                            :unit))
                                         value)))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (html
       (let [refresh-chan            (om/get-shared owner :refresh)
             {:keys [dropdown-chan
                     event-chan]}    (om/get-state owner)
             {:keys [status text]}   (:alert state)]
         [:div
          (alert "alert alert-danger" [:p text] status "new-dataset-form-alert" owner)
          [:h3 "Add new dataset"]
          [:form.form-horizontal {:role "form"}
           [:div.col-md-12
            [:div.form-group
             [:div.btn-toolbar
              [:button {:type "button"
                        :class "btn btn-success"
                        :onClick (fn [_]
                                   (let [dataset         @new-dataset
                                         series          (selected-series dataset)
                                         parsed-dataset  (-> dataset
                                                             (update-in [:operation] name)

                                                             (assoc :series series)
                                                             (dissoc :series1 :series2 :series3 :series4 :field))]
                                     (if (valid-dataset? parsed-dataset)
                                       (post-new-dataset event-chan refresh-chan owner new-dataset parsed-dataset property-id)
                                       (om/set-state! owner :alert {:status true
                                                                    :class "alert alert-danger"
                                                                    :text  " Please enter required dataset data."}))))}
               "Save"]
              [:button {:type "button"
                        :class "btn btn-danger"
                        :onClick (fn [_]
                                   (put! event-chan {:event :adding-dataset :value false}))}
               "Cancel"]]]
            (om/build text-input-control new-dataset {:opts {:id "dataset-name" :path [:name] :required true
                                                             :label "Name" :dropdown-chan dropdown-chan}})
            (om/build dropdown {:default "none" :items available-operations}
                      {:opts {:id "operation-dropdown" :required true :label "Operation"
                              :path [:operation] :dropdown-chan dropdown-chan}})
            (om/build dropdowns {:items series :operation (:operation new-dataset)}
                      {:opts {:dropdown-chan dropdown-chan}})]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Display existing datasets

(defn operands-column [operands]
  [:div
   (for [operand operands]
     (let [[type device_id] (string/split operand #"~")]
       [:div.row {:style {:min-height "100%" :word-wrap "break-word"}}
        [:div.col-md-5 type]
        [:div.col-md-7 device_id]]))])

(defn dataset-row [dataset owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       (let [{:keys [name operation operands device_id selected dataset_id]} dataset
             {:keys [edited-dataset-chan]} (om/get-state owner)]
         [:tr {:onClick (fn [_] (put! edited-dataset-chan {:dataset @dataset :selected? (not selected)}))
               :class (when selected "success")}
          [:td name]
          [:td operation]
          [:td (operands-column (into [] operands))]
          [:td dataset_id]
          [:td device_id]])))))

(defn sorting-th [owner label header-key]
  (let [{:keys [sort-spec th-chan]} (om/get-state owner)
        {:keys [sort-key sort-asc]} sort-spec]
    [:th {:onClick (fn [_ _] (put! th-chan header-key))}
     (str label " ")
     (if (= sort-key header-key)
       (if sort-asc
         [:i.fa.fa-sort-asc]
         [:i.fa.fa-sort-desc]))]))

(defn datasets-table [datasets owner opts]
  (reify
    om/IInitState
    (init-state [_]
      {:th-chan (chan)
       :sort-spec {:sort-key :name
                   :sort-asc true}})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [th-chan sort-spec]} (om/get-state owner)
              {:keys [sort-key sort-asc]} sort-spec
              th-click                    (<! th-chan)]
          (if (= th-click sort-key)
            (om/update-state! owner #(assoc %
                                       :sort-spec {:sort-key th-click
                                                   :sort-asc (not sort-asc)}))
            (om/update-state! owner #(assoc %
                                       :sort-spec {:sort-key th-click
                                                   :sort-asc true}))))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [sort-key sort-asc]} (:sort-spec state)
            table-id                    "datasets-table"]
        (html
         [:div.col-md-12 {:style {:overflow "auto"}}
          [:table {:class "table table-hover table-condensed"}
           [:thead
            [:tr
             (sorting-th owner "Name" :name)
             (sorting-th owner "Operation" :operation)
             (sorting-th owner "Operands (Sensors: Type/Device ID. Fields: Field/Unit)" :operands)
             (sorting-th owner "Dataset ID" :dataset_id)
             (sorting-th owner "Synthetic Device ID" :device_id)]]
           [:tbody
            (om/build-all dataset-row (if sort-asc
                                        (sort-by sort-key datasets)
                                        (reverse (sort-by sort-key datasets)))
                          {:key :device_id
                           :init-state {:edited-dataset-chan (:edited-dataset-chan opts)}})]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entire datasets tab view

(defn datasets-div [datasets owner]
  (reify
    om/IInitState
    (init-state [_]
      {:event-chan (chan)
       :edited-dataset-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (let [{:keys [event-chan edited-dataset-chan]} (om/get-state owner)]
        (go-loop []
          (let [{:keys [event value]}  (<! event-chan)]
            (om/update! datasets event value))
          (recur))
        (go-loop []
          (let [{:keys [dataset selected?]} (<! edited-dataset-chan)]
            (om/transact! datasets [:datasets] (fn [d] (mapv #(if (= (:device_id %) (:device_id dataset))
                                                                (assoc % :selected selected?)
                                                                (assoc % :selected false)) d)))
            (om/update! datasets :edited-dataset (if selected?
                                                   (-> dataset
                                                       (update-series))
                                                   {})))
          (recur))))
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [adding-dataset editing-dataset edited-dataset editable]} datasets
            {:keys [event-chan edited-dataset-chan]} (om/get-state owner)]
        (html
         [:div.col-md-12
          [:h3 "Datasets"
           (when editable
             ;; Buttons
             [:div.btn-toolbar.pull-right
              ;; Add new dataset
              [:button {:type "button"
                        :title "Add Dataset"
                        :class (str "btn btn-primary fa fa-plus " (when (or adding-dataset editing-dataset) "hidden"))
                        :onClick (fn [_] (om/update! datasets :adding-dataset true))}]
              ;; Edit dataset
              [:button {:type "button"
                        :title "Edit Dataset"
                        :class (str "btn btn-primary fa fa-pencil-square-o "
                                    (when (or adding-dataset editing-dataset (not (seq edited-dataset))) "hidden"))
                        :onClick (fn [_] (om/update! datasets :editing-dataset true))}]])]

          ;; Alert
          [:div {:id "alert-div" :style {:padding-top "10px"}}
           (om/build bs/alert (:alert datasets))]
          (when (and (seq (:datasets datasets)) (not adding-dataset) (not editing-dataset))
            (om/build datasets-table (:datasets datasets) {:opts {:edited-dataset-chan edited-dataset-chan}}))
          ;; Forms
          (when adding-dataset
            (om/build new-dataset-form {:new-dataset (:new-dataset datasets)
                                        :property-id (:property-id datasets)
                                        :series (enrich-series-for-dropdowns (:datasets datasets)
                                                                             (:sensors datasets))}
                      {:init-state {:event-chan event-chan}}))
          (when (and (seq edited-dataset) editing-dataset)
            (let [{:keys [operands device_id name]} edited-dataset]
              (om/build edit-dataset-form {:sensors (:sensors datasets)
                                           :series (enrich-series-for-dropdowns (:datasets datasets)
                                                                                (:sensors datasets))
                                           :selected-dataset (:edited-dataset datasets)}
                        {:init-state {:event-chan event-chan
                                      :edited-dataset-chan edited-dataset-chan}})))])))))
