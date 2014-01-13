(ns kixi.hecuba.main
  (:require
   [dommy.utils :as utils]
   [dommy.core :as dommy]
   [ajax.core :refer (GET POST)]
   )
  (:require-macros
   [dommy.macros :refer [node sel sel1]]))

(def project-table
  {:headings {:programme "Programme"
              :name "Project"
              :project-code "Project code"
              :last-edit "Last edit"
              :leaders "Leaders"
              :properties "Properties"}})

(defn make-handler [table-def]
  (fn [response]
    (let [table
          (node
           [:table.full
            [:thead
             [:tr
              (for [h (vals (:headings table-def))]
                (do
                  (.log js/console "h is " h)
                  [:th h]))]]
            [:tbody
             (for [row (js->clj response)]
               (do
                 (.log js/console "processing row: " row)
                 [:tr
                  (for [[k _] (:headings table-def)]
                    [:td (str (get row (name k)))]
                    )
                  ]))]])]
      (dommy/replace! (sel1 [:#projects :table]) table))))

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
  (GET "/projects/" {:handler (make-handler project-table) :headers {"Accept" "application/json"}}))

(set! (.-onload js/window) main)
