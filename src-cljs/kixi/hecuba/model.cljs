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
                 :sort-spec {:sort-key :name
                             :sort-asc true}
                 :editing false
                 :edited-row nil
                 :new-programme {}
                 :adding-programme false
                 :alert {}}
    :projects {:name     "Projects"
               :data     []
               :selected nil
               :sort-spec {:sort-key :name
                           :sort-asc true}
               :editing false
               :edited-row nil
               :adding-project false
               :new-project {}
               :fetching :no-data}
    :properties {:name     "Properties"
                 :data     []
                 :fetching :no-data
                 :selected nil
                 :sort-spec {:sort-key :property_code
                             :sort-fn :property_code
                             :sort-asc true}
                 :adding-property false
                 :active-tab :overview
                 :new-property {}
                 :editing false
                 :selected-property {}
                 :alert {}
                 :devices {:name "Devices"
                           :header   {:cols {[:location :name] {:label "Name"}
                                             :type     {:label "Type"}
                                             :unit     {:label "Unit"}
                                             :select   {:label "Select" :checkbox true}}
                                      :sort [:name]}
                           :alert {}
                           :selected nil
                           :sort-spec {:sort-key :description
                                       :sort-asc true}
                           :new-device {}
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
                           :sort-spec {:sort-key :type
                                       :sort-asc true}
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
                            :fetching false
                            :rollup-type nil}
                 :raw-data {:name "Raw Data"
                            :data []
                            :sensors []
                            :selected nil
                            :date nil}
                 :profiles {:alert {}}
                 :downloads {:files []}
                 :uploads {:files []
                           :alert {}}}}))
