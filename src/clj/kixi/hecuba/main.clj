(ns kixi.hecuba.main
  "Start up for application"
  (:gen-class)
  (:require [clojure.tools.cli          :refer [cli]]
            [clojure.tools.nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [kixi.hecuba.application.system]
            [com.stuartsierra.component :as component]
            [kixipipe.application       :as kixi]))

(defrecord ReplServer [config]
  component/Lifecycle
  (start [this]
    (println "Starting REPL server " config)
    (assoc this :repl-server
           (apply nrepl-server/start-server :handler cider-nrepl-handler (flatten (seq config)))))
  (stop [this]
    (println "Stopping REPL server with " config)
    (nrepl-server/stop-server (:repl-server this))
    (dissoc this :repl-server)))

(defn mk-repl-server [config]
  (ReplServer. config))

(defn build-application [opts]
  (let [system (kixi.hecuba.application.system/new-system)]
    (-> system
        (cond-> (:repl opts)
                (assoc :repl-server (mk-repl-server {:port (:repl-port opts)}))))))

(defn -main [& args]

  (let [[opts args banner]
        (cli args
             ["-h" "--help" "Show help"
              :flag true :default false]
             ["-N" "--scheduler" "Start scheduler"
              :flag true :default true]
             ["-R" "--repl" "Start a REPL"
              :flag true :default true]
             ["-r" "--repl-port" "REPL server listen port"
              :default 4001 :parse-fn #(Integer. %)])]

    (when (:help opts)
      (println banner)
      (System/exit 0))
    (alter-var-root #'kixi/system (fn [_] (component/start (build-application opts))))))
