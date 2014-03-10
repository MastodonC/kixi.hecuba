(ns kixi.hecuba.model)

(def app-model
  (atom
   {:messages []
    :nav {:active "dashboard"
          :menuitems [{:name :dashboard :label "Dashboard" :href "/index.html" :icon "dashboard"}
                      {:name :overview :label "Overview" :href "/charts.html" :icon "bar-chart-o"}
                      {:name :users :label "Users"}
                      {:name :programmes :label "Programmes" :active? true}
                      {:name :projects :label "Project"}
                      {:name :properties :label "Properties"}
                      {:name :charts :label "Charts"}
                      {:name :about :label "About"}
                      {:name :documentation :label "Documentation"}
                      {:name :api_users :label "API users"}
                      ]}

    :tab-container {:selected :programmes
                    :tabs [{:name :about :title "About"}
                           {:name :documentation :title "Documentation"}
                           {:name :users :title "Users"}
                           {:name     :programmes
                            :title    "Programmes"
                            :tables   {:programmes {:name     "Programmes"
                                                    :header   {:cols {:name        {:label "Name" :href :href}
                                                                      :description {:label "Description"}
                                                                      :created_at  {:label "Created at"}
                                                                      }
                                                               :sort [:name :leaders]}
                                                    :data     []
                                                    :selected nil
                                                    }
                                       :projects {:name     "Projects"
                                                  :header   {:cols {:name         {:label "Name" :href :href}
                                                                    :type_of      {:label "Type"}
                                                                    :description  {:label "Description"}
                                                                    ;; TODO Why are these underscores?
                                                                    :created_at   {:label "Created at"}
                                                                    :organisation {:label "Organization"}
                                                                    :project_code {:label "Project code"}}
                                                             :sort [:name]}
                                                  :data     []
                                                  :selected nil
                                                  
                                                  }
                                       :properties {:name     "Properties"
                                                    :header   {:cols {:addressStreetTwo {:label "Address" :href "href"}
                                                                      :addressCounty    {:label "County"}
                                                                      :addressCountry   {:label "Country"}
                                                                      :addressRegion    {:label "Region"}}
                                                               :sort [:addressStreetTwo]}
                                                    :data     []
                                                    :selected nil
                                                    }
                                       :devices {:name     "Devices"
                                                 :header   {:cols {[:location :name] {:label "Name"}
                                                                   :description      {:label "Description"}
                                                                   :privacy          {:label "Privacy"}} 
                                                            :sort [:name :description]}
                                                 :data     []
                                                 :selected nil
                                                 }
                                       :sensors {:name     "Sensors"
                                                 :header   {:cols {:type     {:label "Type"}
                                                                   :unit     {:label "Unit"}
                                                                   :period   {:label "Period"}
                                                                   :deviceId {:label "Device"}
                                                                   :status   {:label "Status"}}
                                                            :sort [:type]}
                                                 :data     []
                                                 :selected nil
                                                 }
                                       :measurements {:name     "Measurements"
                                                      :header   {:cols {:timestamp {:label "Timestamp"}
                                                                        :type      {:label "Type"}
                                                                        :value     {:label "Value"}
                                                                        :error     {:label "Error"}}
                                                                 :sort [:timestamp]}
                                                      :data     []
                                                      :selected nil
                                                      }
                                       :sensor-select {:name     "Sensors"
                                                       :header   {:cols {[:location :name] {:label "Name"}
                                                                         :type     {:label "Type"}
                                                                         :unit     {:label "Unit"}
                                                                         :select   {:label "Select" :checkbox true}}
                                                                  :sort [:name]}
                                                       :data     []
                                                       :sensor-group #{}
                                                       }}
                            :chart    {:property ""
                                       :sensor ""
                                       :range {}
                                       :measurements []
                                       }}

                           ]}}))
