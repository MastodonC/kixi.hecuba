(ns kixi.hecuba.web.map
  (:require
   [mrhyde.core :as mrhyde]
   ))

(def L (this-as ct (aget ct "L")))

(mrhyde/bootstrap)

(def tile-url
  "http://{s}.tile.cloudmade.com/BC9A493B41014CAABB98F0471D759707/22677/256/{z}/{x}/{y}.png")

(let [mappy (-> L (.map "mappy") 
                  (.setView [53.0, -1.5] 6))]

  (-> L (.tileLayer tile-url
                    {:maxZoom 16
                     :attribution "Map data &copy; 2011 OpenStreetMap contributors, Imagery &copy; 2012 CloudMade"})
        (.addTo mappy))

  (-> L (.marker [53.0086, -1.5]) 
        (.addTo mappy)
        (.bindPopup "<b>Hello world!</b><br />I am a popup.")
        (.openPopup))

  (let [popup (-> L .popup)]
    (.on mappy "click" (fn [{:keys [latlng]} e]
                         (.log js/console "***** e is: " e)
                         (-> popup (.setLatLng latlng)
                             (.setContent (str "You clicked the map at " latlng))
                             (.openOn mappy))))))
