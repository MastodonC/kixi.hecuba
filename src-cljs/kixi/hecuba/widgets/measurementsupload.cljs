(ns kixi.hecuba.widgets.measurementsupload
    (:require [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [kixi.hecuba.widgets.fileupload :refer (alert upload)]))

;; FIXME bit of a bodge here. hard coded date formats. Perhaps these should be returned from the server.
(def date-formats [{:display "dd/mm/yyyy hh:mm" :format "dd'/'mm'/'yyyy"} ;; defaults to first
                   {:display "dd-mm-yyyy hh:mm" :format "dd'-'mm'-'yyyy"}
                   {:display "yyyy/mm/dd hh:mm" :format "yyyy'/'mm'/'dd"}
                   {:display "yyyy-mm-dd hh:mm" :format "yyyy'-'mm'-'dd"}
                   {:display "yyyy-mm-dd hh:mm" :format "yyyy'-'mm'-'dd"}])

(defn measurements-upload [url id]
  (fn [data owner {:keys [method]}]
    (reify
      om/IInitState
      (init-state [_]
        {:status ""})
      om/IRenderState
      (render-state [_ {:keys [status]}]
        (let [default-date-format (first date-formats)
              hidden-id (str id "-date-format")]
          (html
           [:div
            (alert "alert alert-info "
                   [:div
                    [:div {:class "fa fa-spinner fa-spin"}] ;; needs to be separate as otherwise it spins the text as well
                    [:p "Upload in progress"]]
                   (= :uploading status)
                   (str id "-uploading"))

            (alert "alert alert-success "
                   [:div
                    [:div {:class "fa fa-check-square-o"} " File uploaded successfully."]]
                   (= :success status)
                   (str id "-success"))

            (alert "alert alert-danger "
                   [:div
                    [:div {:class "fa fa-exclamation-triangle"} " Failed to parse CSV."]]
                   (= :failure status)
                   (str id "-failure"))

            [:div
             [:form {:role "form" :id id :enc-type "multipart/form-data"}
              [:div.form-group
               [:div.dropdown
                [:button {:class "btn btn-default dropdown-toggle"
                          :type "button"
                          :id "dateformatMenu1"
                          :title "Date Format"
                          :data-toggle "dropdown"}
                 (:display default-date-format)
                 [:span.caret.pull-right]]
                [:label {:for "dateformatMenu1"} "Date Format"]
                [:ul.dropdown-menu {:role "menu"
                                    :aria-labelledby "dateformatMenu1"}
                 (for [item date-formats]
                   [:li {:role "presentation"}
                    [:a {:role "menuitem" :tab-index "-1" :href "#"
                         :onClick (fn [e]
                                    (.preventDefault e)
                                    (println "hidden-id:" hidden-id)
                                    (aset (.getElementById js/document hidden-id) "value" (:format item))
                                    false)} (:display item)]])]] [:input {:type "file" :name "data"
                 :title "Browse files"}]
               [:input {:type "hidden" :name "dateformat" :id hidden-id :value (:format default-date-format)}]]

              [:button {:type "button"
                        :class "btn btn-primary"
                        :onClick (fn [_] (upload owner (.getElementById js/document id) url method))}
               "Upload"]]]]))))))
