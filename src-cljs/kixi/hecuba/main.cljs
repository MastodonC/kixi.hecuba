(ns kixi.hecuba.main
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
   [ajax.core :refer (GET POST)]
   [clojure.string :as str]
   [kixi.hecuba.navigation :as nav]
   [kixi.hecuba.widgets.datetimepicker :as dtpicker]
   [kixi.hecuba.tabs.programmes :as programmes]
   [kixi.hecuba.widgets.chart :as chart]
   [kixi.hecuba.common :refer (index-of map-replace find-first interval)]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.model :refer (app-model)]
   [kixi.hecuba.sensor :as sensor]))

(enable-console-print!)

(defn blank-tab [data owner]
  (om/component
      (dom/p nil "This page is unintentionally left blank")))

(defn charts-tab [data owner]
  (om/component
      (dom/div nil
           (dom/h1 nil (:title data))
           (om/build chart/chart-figure (:chart data)))))

(defn about-tab [data owner]
  (om/component
      (dom/div nil
           (dom/h1 nil (:title data))
           (dom/p nil "I'm the About tab"))))

(defn documentation-tab [data owner]
  (om/component
      (dom/div nil
           (dom/h1 nil (:title data))
           (dom/p nil "Some documentation"))))

(defn users-tab [data owner]
  (om/component
      (dom/div nil
           (dom/h1 nil (:title data))
           (dom/p nil "List of users"))))

(defn make-channel-pair []
  {:in (chan (sliding-buffer 1))
   :out (chan (sliding-buffer 1))})

(defn ^:export handle-left-nav [menu-item]
  ;; Currently we implement a one-to-one correspondence between the
  ;; left-hand-menu and the tab container, but in due course 'Project'
  ;; and 'Properties' will cause a scroll to a location under the
  ;; 'Programmes' tab
  (swap! app-model assoc-in [:tab-container :selected] menu-item))

(defn FOO []
  (let [path [:tab-container :tabs 3 :tables :sensor-select :sensor-group]]
    (println "AM:" (type(-> @app-model (get-in path))))
    (println "AM:" (pr-str (-> @app-model (get-in path))))))

(comment
  (om/root
   (let [{:keys [in out] :as pair} (make-channel-pair)]
     (go-loop []
       (when-let [n (<! out)]
         (handle-left-nav n)
         (recur)))
     (nav/nav pair))
   app-model
   {:target (.getElementById js/document "hecuba-nav")}))

(comment
  (om/root (tab-container {:about about-tab
                           :programmes programmes/programmes-tab
                           :charts charts-tab
                           :documentation documentation-tab
                           :users users-tab})
           app-model
           {:target (.getElementById js/document "hecuba-tabs")
            :shared {:history (history/new-history [:programme :project :property :device :sensor :measurement])}}))

(defn tab-container [tabs]
  (fn [data owner]
    (om/component
        (dom/div nil
             (let [selected (-> data :tab-container :selected)]
               (if-let [tab (get tabs selected)]
                 (om/build tab (->> data :tab-container :tabs (filter #(= (:name %) selected)) first))
                 (om/build blank-tab data)))))))

;; TODO
(om/root (fn [data owner]
           (om/component
            (dom/div nil
                     (let [selected (-> data :tab-container :selected)]
                       (om/build programmes/programmes-tab (->> data :programmes))))))
         app-model
         {:target (.getElementById js/document "hecuba-tabs")
          :shared {:history (history/new-history [:programme :project :property :device :sensor :measurement])}})
