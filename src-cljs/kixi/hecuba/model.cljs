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
                 :adding-programme false
                 :alert {}}
    :projects {:name     "Projects"
               :data     []
               :selected nil
               :editing false
               :edited-row nil
               :adding-project false}
    :properties {:name     "Properties"
                 :data     []
                 :selected nil
                 :adding-property false
                 :alert {}}
    :property-details {:data {}}
    :measurements {:name     "Measurements"
                   :header   {:cols {:timestamp {:label "Timestamp"}
                                     :type      {:label "Type"}
                                     :value     {:label "Value"}
                                     :error     {:label "Error"}}
                              :sort [:timestamp]}
                   :data     []
                   :selected nil}
    :devices {:name "Devices"
              :header   {:cols {[:location :name] {:label "Name"}
                                :type     {:label "Type"}
                                :unit     {:label "Unit"}
                                :select   {:label "Select" :checkbox true}}
                         :sort [:name]}
              :alert {}
              :selected nil
              :adding false
              :editing false
              :edited-device nil}
    :sensors {:name     "Sensors"
              :header   {:cols {[:location :name] {:label "Name"}
                                :type     {:label "Type"}
                                :unit     {:label "Unit"}
                                :select   {:label "Select" :checkbox true}}
                         :sort [:name]}
              :selected nil
              :editing false
              :row nil}
    :uploads []
    :downloads {:files []}
    :chart    {:property ""
               :sensors #{}
               :unit ""
               :range {}
               :measurements []
               :message ""}
    :raw-data {:name "Raw Data"
               :data []
               :selected nil
               :message ""
               :range {:start-date nil :end-end nil}}
    :profiles {:alert {}}}))
