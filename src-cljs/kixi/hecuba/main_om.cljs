(ns kixi.hecuba.main-om
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! chan put! sliding-buffer]]
   [ajax.core :refer (GET POST)]))

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
               {:name "api_users" :label "API users"}]

   })

(def projects (atom {:projects [{:name "Ealing Heating" :leaders "Donnie"}
                                {:name "Bow Housing" :leaders "Paul"}
                                {:name "Archway Insulation" :leaders "Dave"}]}))

(.log js/console "Starting Hecuba Om")

(GET "/projects/" {:handler (fn [response]
                              (.log js/console "Hecuba data is in: " (str response))
                              #_(swap! projects update-in [:projects] conj {:name "Late arriving project" :leaders "Jerry"})
                              (swap! projects assoc-in [:projects] response)
                              )
                   :headers {"Accept" "application/edn"}})

;;(om/root app-model menu (.getElementById js/document "hecuba-menu"))

(let [ch (chan (sliding-buffer 1))]
  (om/root projects table (.getElementById js/document "hecuba-projects")))

(.log js/console "Starting Hecuba Om... Done")
