(ns kixi.hecuba.main-om
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! chan put! sliding-buffer]]
   [ajax.core :refer (GET POST)]
   [kixi.hecuba.navigation :as nav]))

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

(def nav-model
  (atom {:messages []
         :nav {:active "dashboard"
               :menuitems [{:name "dashboard" :label "Dashboard" :href "/index.html" :icon "dashboard" :active? true}
                           {:name "overview" :label "Overview" :href "/charts.html" :icon "bar-chart-o"}
                           {:name "users" :label "Users"}
                           {:name "programmes" :label "Programmes"}
                           {:name "projects" :label "Project"}
                           {:name "properties" :label "Properties"}
                           {:name "about" :label "About"}
                           {:name "documentation" :label "Documentation"}
                           {:name "api_users" :label "API users"}
                           ]}}))

(def projects (atom {:projects []}))

;; Add navigation.
(om/root nav-model nav/nav (.getElementById js/document "hecuba-nav"))

;; Attach projects to a table component at hecuba-projects
(om/root projects table (.getElementById js/document "hecuba-projects"))

(GET "/messages/" {:handler #(swap! nav-model assoc-in [:messages] %)
                   :headers {"Accept" "application/edn"}})

;; Get the real project data
(GET "/projects/" {:handler #(swap! projects assoc-in [:projects] %)
                   :headers {"Accept" "application/edn"}})
