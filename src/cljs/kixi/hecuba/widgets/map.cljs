(ns kixi.hecuba.widgets.map
  (:require
   [kixi.hecuba.tabs.slugs :as slugs]
   [om.core :as om :include-macros true]
   [clojure.string :as str]
   [kixi.hecuba.common :refer (log)]
   [kixi.hecuba.tabs.hierarchy.tech-icons :as icons]
   [sablono.core :as html :refer-macros [html]]))

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
(defn build-popup [owner {:keys [property_data name photos entity_id programme_id project_id] :as e}]
  (let [{:keys [address_street address_street_two address_region address_country description tech-icons]} property_data
        full-address (slugs/postal-address property_data)
        title (:property_code property_data) ;; TODO what should go here?
        img-url (:uri (first photos))
        entity-url (str "/app#" programme_id "," project_id "," entity_id)
        tech-icons (apply str (remove nil? (for [ti (:technology_icons property_data)]
                                             (when-let [icon (icons/tech-icon ti)]
                                               (let [[tag src] icon]
                                                 (str "<img src=\"" (:src src) "\"></img>"))))))]
    (str "<div class=\"container-fluid container-xs-height container-popup\">
            <div class=\"panel panel-default\">
              <div class=\"panel-heading\">
                <h3 class=\"panel-title\">" title "</h3>
              </div>
              <div class=\"panel-body\">
                <div class=\"panel\">
                  <div class=\"\">" full-address "</div>
                  <div class=\"tech-icon-container-sm\">" tech-icons "</div>
                </div>
                <img class=\"img-responsive constrained\" alt=\" \" src=\"" img-url "\" ></img>
                <a href=\"" entity-url "\">view</a>
              </div>
            </div>
          </div>")))

(defn map-item
  [cursor owner]
  (reify
    om/IRender
    (render [_]
      (html [:div]))
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
