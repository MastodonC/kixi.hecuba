(ns kixi.hecuba.tabs.hierarchy.tech-icons)

(defmulti tech-icon (fn [[k v]] k))
(defmethod tech-icon :ventilation_systems [[_ v]]
  (when v
    [:img {:src "/images/icons/mechanical_ventilation_with_heat_recovery.png"}]))
(defmethod tech-icon :photovoltaics [[_ v]]
  (when v
    [:img {:src "/images/icons/solar_pv.png"}]))
(defmethod tech-icon :solar_thermals [[_ v]]
  (when v
    [:img {:src "/images/icons/solar_thermal.png" }]))
(defmethod tech-icon :wind_turbines [[_ v]]
  (when v
    [:img {:src "/images/icons/wind_turbine.png"}]))
(defmethod tech-icon :small_hydros [[_ v]]
  (when v
    [:img {:src "/images/icons/hydroelectricity.png"}]))
(defmethod tech-icon :heat_pumps [[_ v]]
  (when v
    [:img {:src "/images/icons/air_source_heat_pump.png"}]))
(defmethod tech-icon :chps [[_ v]]
  (when v
    [:img {:src "/images/icons/micro_chp.png"}]))
(defmethod tech-icon :solid_wall_insulation [[_ v]]
  (when v
    [:img {:src "/images/icons/solid_wall_insulation.png"}]))
(defmethod tech-icon :cavity_wall_insulation [[_ v]]
  (when v
    [:img {:src "/images/icons/cavity_wall_insulation.png"}]))
