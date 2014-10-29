(ns kixi.hecuba.model)

(def app-model
  (atom
   {:name     :programmes
    :title    "Programmes"
    :search {:term nil
             :data []
             :selected nil
             :fetching false
             :stats {}}
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
                 :alert {}
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
                           :alert {}}
                 :datasets {:sensors []
                            :datasets []
                            :property-id nil
                            :alert {}
                            :new-dataset {:operation nil :series []}
                            :editable false
                            :edited-dataset {}}
                 :chart    {:property ""
                            :sensors #{}
                            :units {}
                            :range {}
                            :measurements []
                            :all-groups []
                            :message ""
                            :fetching false}
                 :raw-data {:name "Raw Data"
                            :data []
                            :sensors []
                            :selected nil
                            :date nil}
                 :profiles {:alert {}}
                 :downloads {:files []}
                 :uploads {:files []
                           :alert {}}}}))
