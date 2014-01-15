(ns kixi.hecuba.web.messages
  (:require
   [liberator.core :refer (defresource)]
   [kixi.hecuba.protocols :refer (upsert! item items)]
   [bidi.bidi :refer (->Redirect path-for)]
   [clojure.edn :as edn]))

(def base-media-types ["application/json" "application/edn"])

(def messages [{:id 1
                :avatar "http://placekitten.com/50/50"
                :name "John Smith"
                :message "1.Hey there, I wanted to ask you something..."
                :time "4:34PM"
                }
               {:id 2
                :avatar "http://placekitten.com/50/50"
                :name "John Smith"
                :message "2.Hey there, I wanted to ask you something..."
                :time "4:34PM"
                }
               {:id 3
                :avatar "http://placekitten.com/50/50"
                :name "John Smith"
                :message "3.Hey there, I wanted to ask you something..."
                :time "4:34PM"}])

(defresource messages-resource [querier commander]
  :allowed-methods #{:get}
  :available-media-types base-media-types
  :exists? (fn [_] {::messages messages})
  :handle-ok (fn [{messages ::messages {mt :media-type} :representation :as ctx}]
               (case mt
                 "application/edn" (pr-str messages)
                 messages)))

(defn create-routes [querier commander]
  (let [messages (messages-resource querier commander)]
    [""
     [["messages/" messages]
      ["messages" (->Redirect 307 messages)]]]))
