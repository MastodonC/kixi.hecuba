(ns kixi.hecuba.model)

(def app-model
  (atom
   {:name     :programmes
    :title    "Programmes"
    :programmes {:name     "Programmes"
                 :data     []
                 :selected nil
                 :editing false
                 :edited-row nil
                 :adding-programme false}
    :projects {:name     "Projects"
               :data     []
               :selected nil
               :editing false
               :edited-row nil
               :adding-project false}
    :properties {:name     "Properties"
                 :data     []
                 :selected nil
                 :adding-property false}
    :property-details {:data {}}
    :devices {:name     "Devices"
              :data     []
              :selected nil}
    :sensors {:name     "Sensors"
              :data     []
              :selected #{}}
    :measurements {:name     "Measurements"
                   :header   {:cols {:timestamp {:label "Timestamp"}
                                     :type      {:label "Type"}
                                     :value     {:label "Value"}
                                     :error     {:label "Error"}}
                              :sort [:timestamp]}
                   :data     []
                   :selected nil}
    :sensor-select {:name     "Sensors"
                    :header   {:cols {[:location :name] {:label "Name"}
                                      :type     {:label "Type"}
                                      :unit     {:label "Unit"}
                                      :select   {:label "Select" :checkbox true}}
                               :sort [:name]}
                    :data     []
                    :sensor-group {:members #{}
                                   :name nil}}
    :sensor-edit {:editing false
                  :row nil}
    :uploads []
    :downloads {:files []}
    :chart    {:property ""
               :sensors #{}
               :unit ""
               :range {}
               :measurements []
               :message ""}}))
