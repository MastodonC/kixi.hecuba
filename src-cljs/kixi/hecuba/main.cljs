(ns kixi.hecuba.main
  (:require
   [dommy.utils :as utils]
   [dommy.core :as dommy]
   [ajax.core :refer (GET POST)]
   )
  (:require-macros
   [dommy.macros :refer [node sel sel1]]))

(defn handler [response]
  (let [tbody
        (node [:tbody
               (for [row (js->clj response)]
                 [:tr [:td {:colspan 5} (str row)]])])]
    (dommy/replace! (sel1 [:#projects :table :tbody]) tbody)))

(defn table [cols]
  [:table.full
   [:thead
    [:tr
     (for [colname cols]
       [:th colname])]]
   [:tbody]])

(defn main []
  (dommy/prepend! (sel1 :#content)
         (node
          [:div
           [:h1 "Overview"]
           [:div#programmes
            [:h2 "Programmes"]
            (table ["Name"
                    "Public access"
                    "Projects"
                    "Number of properties"
                    "Number of users"])]
           [:div#projects
            [:h2 "Projects"]
            (table ["Programme" "Project" "Project code" "Last edit" "Leaders" "Properties"])
            [:p
             [:a.view-all {:href "/projects"} "View all »"]
             [:a {:href "/projects/new"} "Add a new project"]]]
           [:div#properties
            [:h2 "Properties"]
            (table ["" "" "Property" "Property code" "Project" "Last activity" "Monitoring level" "Contextual data completeness" "% Raw Data Completion (6 Months)"])
            [:p
             [:a.view-all {:href "/projects"} "View all »"]
             [:a {:href "/projects/new"} "Add a new project"]]]
           ]))
  (GET "/projects/" {:handler handler}))

(set! (.-onload js/window) main)
