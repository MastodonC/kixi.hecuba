(ns kixi.hecuba.main-om
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! chan put! sliding-buffer]]
   [ajax.core :refer (GET POST)]
   [kixi.hecuba.navigation :as nav]))

(enable-console-print!)

(defn table-row [data owner cols]
  (om/component
      (apply dom/tr nil
             (for [col cols]
               (let [d (get data col)
                     sd (if (keyword? d) (name d) (str d))]
                 (dom/td nil (if (#{:hecuba/href :hecuba/parent-href} col) (dom/a #js {:href sd} sd) sd)))))))

(defn table [data owner]
  (om/component
      (dom/div #js {:className "table-responsive"}
           (dom/table #js {:className "table table-bordered table-hover table-striped"}
                (dom/thead nil
                     (apply dom/tr nil
                            (for [col (:cols data)]
                              (dom/th nil (name col)))))
                (dom/tbody nil
                     (om/build-all table-row (:rows data)
                         {:key :hecuba/id :opts (:cols data)}))))))

(defn blank-card [data owner]
  (om/component
      (dom/p nil "This page is unintentionally left blank")))

(defn about-card [data owner card]
  (om/component
      (dom/p nil "I'm the About card")))

(defn programmes-card [data owner card]
  (om/component
      (dom/div nil
           (dom/h1 nil (:title card))
           (when-let [tabdata (get-in data [:programmes :table])]
             (om/build table tabdata))
           (dom/h1 nil "Projects")
           (when-let [tabdata (get-in data [:projects :table])]
             (om/build table tabdata))
           (dom/h1 nil "Properties")
           (when-let [tabdata (get-in data [:properties :table])]
             (om/build table tabdata)))))

(defn card-container [cards]
  (fn [data owner]
    (om/component
        (dom/div nil
             (let [selected (-> data :card-container :selected)]
               (if-let [card (get cards selected)]
                 (om/build card data {:opts (->> data :card-container :cards (filter #(= (:name %) selected)) first)})
                 (om/build blank-card data)))))))


(def app-model
  (atom {:messages []
         :nav {:active "dashboard"
               :menuitems [{:name :dashboard :label "Dashboard" :href "/index.html" :icon "dashboard" :active? true}
                           {:name :overview :label "Overview" :href "/charts.html" :icon "bar-chart-o"}
                           {:name :users :label "Users"}
                           {:name :programmes :label "Programmes"}
                           {:name :projects :label "Project"}
                           {:name :properties :label "Properties"}
                           {:name :about :label "About"}
                           {:name :documentation :label "Documentation"}
                           {:name :api_users :label "API users"}
                           ]}
         :card-container {:selected :programmes
                          :cards [{:name :about :title "About"}
                                  {:name :documentation :title "Documentation"}
                                  {:name :users :title "Users"}
                                  {:name :programmes :title "Programmes"}
                                  ]}
         :programmes {}
         :projects {}
         :properties {}}))

(defn ^:export handle-left-nav [menu-item]
  ;; Currently we implement a one-to-one correspondence between the
  ;; left-hand-menu and the card container, but in due course 'Project'
  ;; and 'Properties' will cause a scroll to a location under the
  ;; 'Programmes' card
  (swap! app-model assoc-in [:card-container :selected] menu-item))

(defn ^:export change []
  (.log js/console "Hello Malcolm!")
  (change-card "documentation")
  )

;; Add navigation.
(om/root app-model (nav/nav handle-left-nav) (.getElementById js/document "hecuba-nav"))

;; Attach projects to a table component at hecuba-projects
#_(om/root app-model (create-table [:projects]
                                 ["Name" "Project code" "Leaders"]
                                 (make-table-row [:hecuba/name :project-code :leaders])) (.getElementById js/document "hecuba-projects"))

#_(om/root app-model (create-table [:properties]
                                 ["Name" "Address" "Rooms" "Construction date"]
                                 (make-table-row [:hecuba/name :address :rooms :date-of-construction])) (.getElementById js/document "hecuba-properties"))

(om/root app-model (card-container {:about about-card
                                    :programmes programmes-card}) (.getElementById js/document "hecuba-cards"))

(GET "/messages/" {:handler (fn [x]
                              (println "Messages: " x)
                              (swap! app-model assoc-in [:messages] x))
                   :headers {"Accept" "application/edn"}})

(defn handle-get [k]
  (fn [data]
    (let [cols (distinct (mapcat keys data))]
      (swap! app-model assoc-in [k]
             {:raw data
              :table {:cols cols
                      :rows data}
              }))))

;; Get the real project data
(GET "/programmes/" {:handler (handle-get :programmes) :headers {"Accept" "application/edn"}})
(GET "/projects/" {:handler (handle-get :projects) :headers {"Accept" "application/edn"}})
(GET "/propertiesX/" {:handler (handle-get :properties) :headers {"Accept" "application/edn"}})
