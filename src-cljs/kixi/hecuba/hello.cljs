(ns kixi.hecuba.hello
  (:require
   [dommy.utils :as utils]
   [dommy.core :as dommy]
   [ajax.core :refer (GET POST)]
   )
  (:require-macros
   [dommy.macros :refer [node sel sel1]]
   )
  )

(.log js/console "Hello!!!!")
(dommy/append! (sel1 :#content) (node [:p "Hello Bruce!!!!"]))

(defn handler [response]
  (dommy/append! (sel1 :#content) (node [:div
                                         [:p "Postcode: " (get response "postcode")]
                                         [:p "UUID: " (get response "uuid")]]))
  (.log js/console (str response))
)

(GET "/api" {:handler handler})
