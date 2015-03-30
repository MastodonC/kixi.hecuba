(ns kixi.hecuba.env.dev
  (:require [figwheel.client :as figwheel]
            [kixi.hecuba.main :as m]))


(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback (fn [] (m/main)))

(m/main)
