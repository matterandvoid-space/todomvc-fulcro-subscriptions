(ns space.matterandvoid.todomvc.client.dev-entry
  (:require
    [com.fulcrologic.fulcro.inspect.inspect-client :as inspect]
    [malli.dev.cljs :as md]
    [helix.core :refer [$]]
    [space.matterandvoid.todomvc.client.entry :as entry]
    [space.matterandvoid.todomvc.client.fulcro-app :refer [fulcro-app]]
    [space.matterandvoid.todomvc.todo.ui]
    ["react" :as react]
    [taoensso.timbre :as log]))

(enable-console-print!)
(log/set-level! :debug)

(defn render-root [root]
  (log/debug "render-root" root)
  (.render root ($ react/StrictMode ($ entry/app))))

(defn start! []
  (md/start!))

(defn refresh {:dev/after-load true} []
  (log/debug "Hot Reload")
  (js/setTimeout start! 10)
  (render-root @entry/root_))

(defn init {:export true} []
  (log/debug "Booting app")
  (entry/init)
  (js/setTimeout start! 10)
  (inspect/app-started! fulcro-app))
