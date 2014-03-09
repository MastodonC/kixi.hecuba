(ns kixi.hecuba.navigation
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! chan close! put! sliding-buffer]]))

(defn- navbar-sidenav [data owner {:keys [in out]}]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [out (chan (sliding-buffer 1))]
        (go-loop []
                 (when-let [n (<! out)]
                   (println n)
                   (recur)))))

    om/IWillUnmount
    (will-unmount [_]
      (close! (om/get-state owner :out)))

    om/IRender
    (render [_]
      (dom/div #js {:className "collapse navbar-collapse navbar-ex1-collapse"}
           (dom/ul #js {:className "nav navbar-nav side-nav"}
                (into-array
                 (for [item (:menuitems data)]
                   (dom/li
                        (when (= (:active data) (:name item))
                          #js {:className "active"})
                        (dom/a #js {:href "#"
                                    :onClick (fn [e]
                                               (om/transact! data :active (constantly (:name (om/read item om/value))))
                                               (put! out (:name (om/read item om/value))))}
                             (:label item))))))))))


(defn- message-preview [{:keys [avatar name message time]}]
  (dom/li #js {:className "message-preview"}
          (dom/a #js {:href "#"}
                 (dom/span #js {:className "avatar"}
                           (dom/img #js {:src avatar}))
                 (dom/span #js {:className "name"} name)
                 (dom/span #js {:className "message"} message)
                 (dom/span #js {:className "time"}
                           (dom/i {:className "fa fa-clock-o"})
                           (str " " name)))))

(defn messages-dropdown [messages owner]
  (om/component
   (apply dom/ul #js {:className "dropdown-menu"}
          (dom/li {:className "dropdown-header"}
                  (str (count messages) " New Messages"))
          (map message-preview messages))))

(defn navbar-rightnav [{:keys [messages]} owner]
  (let [n (count messages)]
    (om/component
     (dom/ul #js {:className "nav navbar-nav navbar-right navbar-user"}
             (dom/li #js {:className "dropdown messages-dropdown"}
                     (apply dom/a #js {:href "#"
                                       :className  "dropdown-toggle"
                                       :data-toggle "dropdown"}
                            (dom/i #js {:className "fa fa-envelope"})
                            " Messages"
                            (when (pos? n)
                              [(dom/span #js {:className "badge"} (str " " n))
                               (dom/b #js {:className "caret"})]))
                     (om/build messages-dropdown messages))))))

(defn nav [pair]
  (fn [app owner]
    (reify
      om/IRender
      (render [_]
        (dom/nav #js {:className "navbar navbar-inverse navbar-fixed-top"
                      :role "navigation"}
             (om/build navbar-rightnav app)
             (om/build navbar-sidenav (:nav app) {:opts pair}))))))

(defn FOO-add-message [app name message]
  (swap! app (fn [xs x]
                     (update-in xs [:messages]
                                conj x))
         {:id (rand-int 1000)
          :avatar "http://placekitten.com/50/50"
          :name name
          :message message
          :time "4:34PM"}))
