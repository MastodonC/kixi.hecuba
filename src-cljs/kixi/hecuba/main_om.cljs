(ns kixi.hecuba.main-om
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [sablono.core :as html :refer (html) :include-macros true]
   [cljs.core.async :refer [<! chan put! sliding-buffer]])
  )

(defn menuitem [data owner ch]
  (om/component
      (dom/li nil
           (dom/a #js {:onClick (fn [e]
                                  ;;(.log js/console "Name is :- " (:name (om/read data om/value)))
                                  ;;(.log js/console "Path is :- " (.-path data))
                                  (put! ch (:name (om/read data om/value))))}
                (:label data)))))

(defn menu [app owner]
  (let [in (chan (sliding-buffer 1))]
    (reify
      om/IWillMount
      (will-mount [_] (go (while true (let [n (<! in)] (.log js/console "Got an event!" n)))))
      om/IRender
      (render [_]
        (dom/ul #js {:className "nav"}
             #_(.log js/console "Active: " (:active app))
             (om/build-all menuitem
                 (:menuitems app)
                 ;; React says: 'Each child in an array should have a
                 ;; unique "key" prop. Check the render method of
                 ;; undefined.'. This is the purpose of the :key entry in
                 ;; the map below
                 {:key :name :opts in}))))))

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

(om/root app-model menu (.getElementById js/document "menu"))
