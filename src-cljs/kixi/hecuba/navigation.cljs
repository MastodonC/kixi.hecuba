(ns kixi.hecuba.navigation
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! chan put! sliding-buffer]]))

(defn sidemenu-item [data owner ch]
  (let [attrs (when-let [active (:active? data)]
                #js {:className "active"})
        {:keys [label href icon]} data]
    (om/component
     (dom/li attrs
             (dom/a #js {:href href 
                         :onClick (fn [e]
                                    (.preventDefault e)
                                    (put! ch (:name (om/read data om/value))))}
                    (dom/i #js {:className (str "fa fa-" icon)})
                    (str " " label))))))

(defn- navbar-sidenav [{:keys [header nav]} owner]
  (let [in (chan (sliding-buffer 1))]
    (reify
      om/IWillMount
      (will-mount [_]
        (go-loop [] 
          (let [n (<! in)] (.log js/console "Got an event!" n)
               (when (not= "stop" n)
                 (recur)))))
      om/IWillUnmount ;; TODO requires go block to be terminate-able.
      (will-unmount [_]
        (put! in "stop"))
      om/IRender
      (render [_]
        (dom/div #js {:className "collapse navbar-collapse navbar-ex1-collapse"}
                 (dom/ul #js {:className "nav navbar-nav side-nav"}
                         (om/build-all sidemenu-item (:menuitems nav) {:key :id :opts in})))))))

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

(defn nav [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/nav #js {:className "navbar navbar-inverse navbar-fixed-top" 
                    :role "navigation"}
               (om/build navbar-rightnav app)
               (om/build navbar-sidenav app )))))

(defn FOO-add-message [app name message]
  (swap! app (fn [xs x]
                     (update-in xs [:messages] 
                                conj x))
         {:id (rand-int 1000) 
          :avatar "http://placekitten.com/50/50"
          :name name
          :message message
          :time "4:34PM"}))

