(ns space.matterandvoid.todomvc.client.dev-entry
  (:require
    [com.fulcrologic.fulcro.inspect.inspect-client :as inspect]
    [malli.dev.cljs :as md]
    [space.matterandvoid.subscriptions.fulcro :as subs]
    [space.matterandvoid.todomvc.client.entry-common :as common]
    [space.matterandvoid.todomvc.client.fulcro-app :refer [fulcro-app]]
    [taoensso.timbre :as log]))

(enable-console-print!)
(log/set-level! :debug)

(defn malli-start! [] (md/start!))

(defn init {:export true} []
  (log/debug "Booting app")
  (common/init true)
  (inspect/app-started! fulcro-app)
  ;; async, so errors show up in their own stack in browser devtools
  (js/setTimeout malli-start! 10))

(defn refresh {:dev/after-load true} []
  (log/debug "Hot Reload")
  (subs/clear-subscription-cache! fulcro-app)
  (js/setTimeout malli-start! 10)
  (common/render-root true @common/root_))
