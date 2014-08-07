(ns kixi.hecuba.widgets.map
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.string :as str]
   [kixi.hecuba.common :refer (log)]))

(defn- ->lat-long [{:keys [latitude longitude]}]
  (when (and latitude longitude)
    (.latLng js/L latitude longitude)))

(defn add-marker [map latlng popup]
  (when latlng
    (-> (.marker js/L latlng)
        (.addTo map)
        (.bindPopup popup))))

(defn draw-map
  [cursor node]
  (let [map   (-> (.map js/L node)
                  (.setView (.latLng js/L 53.0 -1.5) 6))
        tiles (.tileLayer js/L "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png"
                          {:attribution "Map data &copy; 2011 OpenStreetMap contributors, Imagery."})]
    (.addTo tiles map)
    map))

;; TODO I want to build the html using sablono, but it doesn't seem to play nice with leaflet.addMarker().
;;      solutions from YOU! dear reader are most welcome.
(defn build-popup [owner {:keys [property_data name photos] :as e}]
  (let [{:keys [address_street address_street_two address_region address_country description tech-icons]} property_data
        full-address (str/join ", " [address_street address_street_two address_region address_country])
        title address_street ;; TODO what should go here?
        img-url (first photos)
        tech-icons (apply str (for [i (:technology_icons property_data) :when i]
                                (str "<div class=\"col-xs-1 tech-icon-container\"><img alt=\" \" class=\"img-responsive tech-icon\" src=\"" i "\"></div>")))]
    (str "<div class=\"container-fluid container-xs-height\">
            <div class=\"panel panel-default\">
              <div class=\"panel-heading\">
                <h3 class=\"panel-title\">" title "</h3>
              </div>
              <div class=\"panel-body\">
                <div class=\"row row-xs-height\">
                  <div class=\"col-xs-12 col-xs-height\">
                    <div class=\"row row-xs-height\">
                      <div class=\"col-xs-4 col-xs-height\">
                        <img class=\"img-responsive\" alt=\" \" src=\"" img-url "\" ></img>
                      </div>
                      <div class=\"col-xs-8 col-xs-height\">
                      " full-address "
                      </div>
                    </div>
                    <div class=\"row row-xs-height\">
                      " tech-icons "
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>")))

(defn map-item
  [cursor owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil))
    om/IDidMount
    (did-mount [_]
      (om/set-state! owner :map (draw-map cursor (om/get-node owner))))
    om/IDidUpdate
    (did-update [_ _ _]
      (doseq [{:keys [property_data] :as entity} cursor]
        (let [map    (om/get-state owner :map)
              latlng (->lat-long property_data)
              popup  (build-popup owner entity)]
          (add-marker map latlng popup))))))
