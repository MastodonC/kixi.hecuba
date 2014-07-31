(ns kixi.hecuba.widgets.fileupload
  (:import goog.net.XhrIo)
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :refer (alert)]))

(defn clear-form [owner node]
  (.reset node))

(defn upload [owner node post-url method]
  (om/set-state! owner :status :uploading)
  (.send goog.net.XhrIo post-url
         (fn [e]
           (let [status (.getStatus (.-target e))]
             (om/set-state! owner :status (if (some #{status} [201 202]) :success :failure))
             (clear-form owner node)))
         method
         (new js/FormData node)))

(defn file-upload [url id]
  (fn [data owner {:keys [method]}]
    (reify
      om/IInitState
      (init-state [_]
        {:status ""})
      om/IRenderState
      (render-state [_ {:keys [status]}]
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
             [:input {:type "file" :name "data" 
                      :title "Browse files"}]]
            [:button {:type "button"
                      :class "btn btn-primary"
                      :onClick (fn [_] (upload owner (.getElementById js/document id) url method))}
             "Upload"]]]])))))
