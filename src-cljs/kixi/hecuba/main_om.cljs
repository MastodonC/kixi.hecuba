(ns kixi.hecuba.main-om
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! chan put! sliding-buffer]]
   [ajax.core :refer (GET POST)]))

;; Deprecated, just left in to show how to pass through a core.async
;; channel and when to put stuff on it - from Om counters example
(defn menuitem [data owner ch]
  (om/component
      (dom/li nil
           (dom/a
                #js {:onClick
                     (fn [_] (put! ch (:name (om/read data om/value))))}
                (:label data)))))

;; Deprecated, just let in to show how and when to run a go block in a
;; component, and how to pass in the channel into sub-components
(defn menu [app owner]
  (let [in (chan (sliding-buffer 1))]
    (reify
      om/IWillMount
      (will-mount [_] (go (while true (let [n (<! in)] (.log js/console "Got an event!" n)))))
      om/IRender
      (render [_]
        (dom/ul #js {:className "dropdown-menu"}
             #_(.log js/console "Active: " (:active app))
             (om/build-all menuitem
                 (:menuitems app)
                 ;; React says: 'Each child in an array should have a
                 ;; unique "key" prop. Check the render method of
                 ;; undefined.'. This is the purpose of the :key entry in
                 ;; the map below
                 {:key :name :opts in}))))))

(defn table-row [data owner]
  (om/component
      (dom/tr #js {:onClick (fn [e] (.log js/console "ooh!"))}
           (dom/td nil (:name data))
           (dom/td nil (apply str (interpose ", " (:leaders data)))))))

(defn table [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "table-responsive"}
           (dom/table #js {:className "table table-bordered table-hover table-striped"}
                (dom/thead nil
                     (dom/tr nil
                          (dom/th nil "Name")
                          (dom/th nil "Leaders")))
                (dom/tbody nil
                     (om/build-all table-row
                         (:projects data)
                         {:key :name})))))))

(def app-model
  {:active "dashboard"
   :menuitems [{:name "dashboard" :label "Dashboard" :href ""}
               {:name "overview" :label "Overview"}
               {:name "users" :label "Users"}
               {:name "programmes" :label "Programmes"}
               {:name "projects" :label "Project"}
               {:name "properties" :label "Properties"}
               {:name "about" :label "About"}
               {:name "documentation" :label "Documentation"}
               {:name "api_users" :label "API users"}]})

(def projects (atom {:projects []}))

;; Attach projects to a table component at hecuba-projects
(om/root projects table (.getElementById js/document "hecuba-projects"))

;; Get the real project data
(GET "/projects/" {:handler #(swap! projects assoc-in [:projects] %)
                   :headers {"Accept" "application/edn"}})
