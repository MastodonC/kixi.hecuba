(ns kixi.hecuba.main
  (:require
   [dommy.utils :as utils]
   [dommy.core :as dommy]
   [ajax.core :refer (GET POST)]
   )
  (:require-macros
   [dommy.macros :refer [node sel sel1]]
   )
  )


(defn handler [response]
  (dommy/append! (sel1 :#content)
         (node [:div
                [:p "Postcode: " (get response "postcode")]
                [:p "UUID: " (get response "uuid")]]))
  (.log js/console (str response))
)

(defn table [cols]
  [:table.full
   [:tr
    (for [colname cols]
      [:th colname])]])

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
           ])))

;;(GET "/api" {:handler handler})

(set! (.-onload js/window) main)
