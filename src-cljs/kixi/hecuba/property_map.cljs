(ns kixi.hecuba.property-map
  (:require
   [om.core :as om :include-macros true]
   [ajax.core :refer (GET)]
   [kixi.hecuba.widgets.map :as map]
   [kixi.hecuba.common :refer (log)]
   [kixi.hecuba.history :as history]))

(def data-model
  (atom
   {:properties []}))

(defn property-map [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (GET "/4/entities/having-locations/" {:handler (fn [data] (om/update! cursor :properties (:entities data)))
                                            :headers {"Accept" "application/edn"}}))
    om/IRender
    (render [_]
      (om/build map/map-item (:properties cursor)))))

(when-let [map (.getElementById js/document "property_map")]
  (om/root property-map
           data-model
           {:target map
            :shared {:history (history/new-history [:TBD])}}))
