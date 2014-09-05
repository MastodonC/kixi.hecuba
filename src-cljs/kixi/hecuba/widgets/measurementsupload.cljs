(ns kixi.hecuba.widgets.measurementsupload
    (:require [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [kixi.hecuba.widgets.fileupload :refer (alert upload)]))

;; FIXME bit of a bodge here. hard coded date formats. Perhaps these should be returned from the server.
(def date-formats [{:display "automatic" :format ""} ;; defaults to first
                   {:display "dd/mm/yyyy hh:mm"    :format "dd/MM/yyyy HH:mm"}
                   {:display "dd-mm-yyyy hh:mm"    :format "dd-MM-yyyy HH:mm"}
                   {:display "yyyy/mm/dd hh:mm"    :format "yyyy/MM/dd HH:mm"}
                   {:display "yyyy-mm-dd hh:mm"    :format "yyyy-MM-dd HH:mm"}
                   {:display "yyyy-mm-dd hh:mm:ss" :format "yyyy-MM-dd HH:mm:ss"}])

(defn validate-size-and-upload [uploads owner id url method]
  (if (not js/window.FileReader)
    (om/update! uploads :alert {:status true
                                :class "alert alert-danger"
                                :text "The file API is not supported on this browser yet."})
    (let [input (.getElementById js/document "file")
          file  (aget (.-files input) 0)]
      (if file
        (let [name (aget file "name")
              size (aget file "size")]
          (if (> size (* 16 1024 1024))
            (om/update! uploads :alert {:status true
                                        :class "alert alert-danger"
                                        :text "Maximum file size is 16MB."})
            (upload owner (.getElementById js/document id) url method)))
        (om/update! uploads :alert {:status true
                                    :class "alert alert-danger"
                                    :text "Please select a file."})))))

(defn measurements-upload [url id]
  (fn [uploads owner {:keys [method]}]
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
                    [:div {:class "fa fa-check-square-o"} " File accepted for processing."]]
                   (= :success status)
                   (str id "-success"))

            (alert "alert alert-danger "
                   [:div
                    [:div {:class "fa fa-exclamation-triangle"} " Failed to parse CSV."]]
                   (= :failure status)
                   (str id "-failure"))

            [:form {:role "form" :id id :enc-type "multipart/form-data" :encoding "multipart/form-data"}
             [:div.form-group
              [:label {:for "dateformat"} "Date Format"]
              [:select.form-control {:name "dateformat" :id "dateformat"}
               (for [item date-formats]
                 [:option {:value (:format item)}
                  (:display item)])]]
             [:div.form-group
              [:label {:for "file"} "File Input"]
              [:input {:type "file" :id "file" :name "data" :title "Browse files"}]]
             [:button {:type "button"
                       :class "btn btn-primary"
                       :onClick (fn [_] (validate-size-and-upload uploads owner id url method))}
              "Upload"]]]))))))
