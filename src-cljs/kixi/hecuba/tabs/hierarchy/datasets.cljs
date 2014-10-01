(ns kixi.hecuba.tabs.hierarchy.datasets
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :refer (static-text) :as bs]
            [ajax.core :refer [GET POST PUT]]
            [clojure.string :as string]
            [kixi.hecuba.common :refer (log) :as common]))

;; TODO this is duplicate from devices.cljs
(defn alert [class body status id owner]
  [:div {:id id :class class :style {:display (if status "block" "none")}}
   [:button.close {:type "button"
                   :onClick (fn [e] (om/set-state! owner :alert {:status false}))}
    [:span {:class "fa fa-times"}]]
   body])

(defn dropdown [owner id required? label path items]
  [:div.form-group
   [:label.control-label.col-md-2 {:for id} label]
   [:div {:class (str "col-md-10 " (when required? "required"))}
    [:select.form-control {:on-change #(om/set-state! owner path (string/lower-case (.-value (.-target %))))}
     (for [item items]
       [:option (:display-name item)])]]])


(defn error-handler [event-chan]
  (fn [{:keys [status status-text]}]
    (put! event-chan {:event :alert :value {:status true
                                            :class "alert alert-danger"
                                            :text status-text}})))

(defn enrich-series-for-dropdowns
  ([datasets sensors]
     (let [d (mapv #(assoc % :display-name (str (:name %) "-" (:device_id %))) datasets)
           s (mapv #(assoc % :display-name (:id %)) sensors)]
       (concat [{:display-name "Select series"}] d s)))
  ([datasets sensors members edited-dataset]
     (let [d (mapv #(assoc % :display-name (str (:name %) "-" (:device_id %))) datasets)
           s (mapv #(assoc % :display-name (:id %)) sensors)]
       (remove #(or
                 (= (:display-name %) edited-dataset))
               (concat d s)))))

(def available-operations [{:display-name "sum"}
                           {:display-name "subtract"}
                           {:display-name "divide"}])

(defn put-new-dataset [event-chan refresh-chan owner dataset property-id]
  (let [resource {:entity_id property-id
                  :operation (string/lower-case (:operation dataset))
                  :name (:name dataset)
                  :members (:series dataset)}]
    (common/post-resource (str "/4/entities/" property-id "/datasets/")
                          resource
                          (fn [_]
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

(defn valid-dataset? [dataset]
  (let [{:keys [operation series name]} dataset]
    (and (and (seq operation) (seq series) (some #{operation} ["sum" "subtract" "divide"]))
         (= (count series) (count (set series)))
         (seq name))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Edit dataset

(defn dropdown-component [{:keys [dataset data]} owner {:keys [id label placeholder path required]}]
  (om/component
   (html
    [:div.form-group
     [:label.control-label.col-md-2 {:for id} label]
     [:div {:class (str "dropdown col-md-10 " (when required "required"))}
      [:button {:class "btn btn-default dropdown-toggle"
                :type "button"
                :style {:width "97%"}
                :id id
                :data-toggle "dropdown"}
       (if-let [selected (seq (get-in dataset path))]
         selected placeholder)
       [:span.caret.pull-right]]
      [:ul.dropdown-menu {:role "menu"
                          :aria-labelledby id}
       (for [item data]
         (let [display-name (:display-name item)]
           [:li {:role "presentation"}
            [:a {:role "menuitem" :tab-index "-1"
                 :onClick (fn [_]
                            (when (and (= path [:operation]) (not= display-name "sum"))
                              (om/transact! dataset :members (fn [members] (into [] (take 2 members)))))
                            (when (and (= path [:operation]) (= display-name "sum"))
                              (om/transact! dataset :members (fn [members] (into members (vec (take (- 4 (count members))
                                                                                                    (cycle [nil])))))))
                            (om/update! dataset path display-name))}
             display-name]]))]]])))

(defn edit-dataset-form [{:keys [datasets sensors selected-dataset]} owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [entity_id device_id members name operation]} selected-dataset
            refresh-chan   (om/get-shared owner :refresh)
            {:keys [event-chan edited-dataset-chan]} (om/get-state owner)
            {:keys [status text]} (:alert state)
            all-series     (enrich-series-for-dropdowns
                           datasets sensors members (str name "-" device_id))
            members-no     (count members)]
          (html
           [:div
            (alert "alert alert-danger" [:p text] status "edit-dataset-form-alert" owner)
            [:h3 "Editing dataset"]
            [:form.form-horizontal {:role "form"}
             [:div.col-md-6
              [:div.form-group
               [:div.btn-toolbar
                [:button {:type "button"
                          :class "btn btn-success"
                          :onClick (fn [_]
                                     (let [dataset (assoc @selected-dataset :series
                                                          (into [] (remove nil? (:members @selected-dataset))))]
                                       (if (valid-dataset? dataset)
                                         (put-new-dataset event-chan refresh-chan owner dataset entity_id)
                                         (om/set-state! owner :alert {:status true
                                                                      :class "alert alert-danger"
                                                                      :text  " Please enter required dataset data."}))))} "Save"]
                [:button {:type "button"
                          :class "btn btn-danger"
                          :onClick (fn [_]
                                     (put! event-chan {:event :editing-dataset :value false})
                                     ;; deselect and clear edited cursor
                                     (put! edited-dataset-chan {:dataset {} :selected? false}))} "Cancel"]]]
              (bs/static-text selected-dataset :device_id "ID")
              (bs/static-text selected-dataset :name "Unique Name")
              (om/build dropdown-component {:dataset selected-dataset :data available-operations}
                        {:opts {:id "operation-dropdown" :label "Operation" :placeholder "Select operation" :path [:operation]
                                :required true}})
              [:div
               (for [i (if (= "sum" operation) (range 4) (range members-no))]
                 (om/build dropdown-component {:dataset selected-dataset :data all-series}
                           {:opts {:id (str "series-dropdown-" i) :label (str "Series " (+ i 1)) :placeholder "Select series"
                                   :path [:members i] :required (if (< i 2) true false)}}))]]]])))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Add dataset

(defn new-dataset-form [datasets owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       (let [refresh-chan          (om/get-shared owner :refresh)
             event-chan            (om/get-state owner :event-chan)
             {:keys [status text]} (:alert state)
             property-id           (:property-id datasets)
             all-series            (enrich-series-for-dropdowns
                                    (:datasets datasets)
                                    (:sensors datasets))]
         [:div
          (alert "alert alert-danger" [:p text] status "new-dataset-form-alert" owner)
          [:h3 "Add new dataset"]
          [:form.form-horizontal {:role "form"}
           [:div.col-md-6
            [:div.form-group
             [:div.btn-toolbar
              [:button {:type "button"
                        :class "btn btn-success"
                        :onClick (fn [_]
                                   (let [dataset         (om/get-state owner :dataset)
                                         selected-series (vec (vals (select-keys dataset [:series1 :series2 :series3 :series4])))
                                         parsed-dataset  (-> dataset
                                                             (assoc :series selected-series)
                                                             (dissoc :series1 :series2 :series3 :series4))]
                                     (if (valid-dataset? parsed-dataset)
                                       (put-new-dataset event-chan refresh-chan owner parsed-dataset property-id)
                                       (om/set-state! owner :alert {:status true
                                                                    :class "alert alert-danger"
                                                                    :text  " Please enter required dataset data."}))))}
               "Save"]
              [:button {:type "button"
                        :class "btn btn-danger"
                        :onClick (fn [_]
                                   (put! event-chan {:event :adding-dataset :value false}))}
               "Cancel"]]]
            (bs/text-input-control nil owner :dataset :name "Name" true)
            (dropdown owner "operation-dropdown" true "Operation" [:dataset :operation] (into [{:display-name "Select operation"}] available-operations))
            (dropdown owner "series1-dropdown" true "First Series" [:dataset :series1] all-series)
            (dropdown owner "series2-dropdown" true "Second Series" [:dataset :series2] all-series)
            (when (-> (om/get-state owner :dataset) :operation (= "sum"))
              [:div
               (dropdown owner "series3-dropdown" false "Third Series" [:dataset :series3] all-series)
               (dropdown owner "series4-dropdown" false "Fourth Series" [:dataset :series4] all-series)])]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Display existing datasets

(defn members-column [members]
  [:div
   (for [member members]
     (let [[type device_id] (string/split member #"-")]
       [:div.row {:style {:min-height "100%"}}
        [:div.col-md-3 type]
        [:div.col-md-4 device_id]]))])

(defn dataset-row [dataset owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       (let [{:keys [name operation members device_id selected]} dataset
             {:keys [edited-dataset-chan]} (om/get-state owner)]
         [:tr {:onClick (fn [_] (put! edited-dataset-chan {:dataset @dataset :selected? (not selected)}))
               :class (when selected "success")}
          [:td name]
          [:td operation]
          [:td (members-column (into [] members))]
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
             (sorting-th owner "Unique Name" :name)
             (sorting-th owner "Operation" :operation)
             (sorting-th owner "Members" :members)
             (sorting-th owner "ID" :device_id)]]
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
            (om/update! datasets :edited-dataset (if selected? (update-in dataset [:members] #(into [] %)) {})))
          (recur))))
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [adding-dataset editing-dataset edited-dataset]} datasets
            {:keys [event-chan edited-dataset-chan]} (om/get-state owner)]
        (html
         [:div.col-md-12
          [:h3 "Datasets"
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
                      :onClick (fn [_] (om/update! datasets :editing-dataset true))}]]]

          ;; Alert
          [:div {:id "alert-div" :style {:padding-top "10px"}}
           (om/build bs/alert (:alert datasets))]
          (when (and (seq (:datasets datasets)) (not adding-dataset) (not editing-dataset))
            (om/build datasets-table (:datasets datasets) {:opts {:edited-dataset-chan edited-dataset-chan}}))
          ;; Forms
          (when adding-dataset
            (om/build new-dataset-form datasets {:init-state {:event-chan event-chan}}))
          (when (and (seq edited-dataset) editing-dataset)
            (om/build edit-dataset-form {:sensors (:sensors datasets)
                                         :datasets (:datasets datasets)
                                         :selected-dataset (:edited-dataset datasets)}
                      {:init-state {:event-chan event-chan
                                    :edited-dataset-chan edited-dataset-chan}}))])))))
