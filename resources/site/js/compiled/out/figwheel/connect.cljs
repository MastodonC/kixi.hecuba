(ns figwheel.connect (:require [geom-om.core] [figwheel.client] [figwheel.client.utils]))
(figwheel.client/start {:build-id "dev", :on-jsload (fn [& x] (if js/geom-om.core.on-js-reload (apply js/geom-om.core.on-js-reload x) (figwheel.client.utils/log :debug "Figwheel: :on-jsload hook 'geom-om.core/on-js-reload' is missing"))), :websocket-url "ws://localhost:3449/figwheel-ws"})

