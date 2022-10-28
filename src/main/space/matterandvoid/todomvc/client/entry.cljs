(ns space.matterandvoid.todomvc.client.entry
  (:require
    [com.fulcrologic.fulcro.application :as fulcro.app]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [helix.core :refer [$ defnc]]
    [helix.dom :as d]
    space.matterandvoid.todomvc.todo.ui
    [space.matterandvoid.subscriptions.fulcro :as subs]
    [space.matterandvoid.todomvc.client.fulcro-app :refer [fulcro-app]]
    [space.matterandvoid.todomvc.client.ui.main-page :refer [main-page]]
    [space.matterandvoid.todomvc.todo.mutations :as todo.mutations]
    ["react" :as react]
    ["react-dom/client" :as rdom]
    ["react-router-dom" :as react-router :refer [Link NavLink useLoaderData]]
    [taoensso.timbre :as log]))

(def router (react-router/createHashRouter
              #js[#js{:path         "/"
                      :element      ($ main-page)
                      :errorElement (d/h1 "ERROR")}
                  #js{:path ":filter" :element ($ main-page)}]))

(defnc app []
  (helix.core/provider {:context subs/datasource-context :value fulcro-app}
    ($ react-router/RouterProvider {:router router})))

; Mount the app / init
;=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

(defonce root_ (volatile! nil))

(defn render-root [root]
  (.render root ($ app)))

(defn hydrate-root []
  (rdom/hydrateRoot (js/document.getElementById "app") ($ react/StrictMode ($ app))))

;; in the future you could have the node.js server add a flag in the rendered document to determine if you're
;; hydrating from node.js or just using clojure backend.
(def hydrate? false)

(def Root (rc/nc [] {:componentName `Root}))

(defn init {:export true} []
  (log/debug "Booting app")

  (fulcro.app/set-root! fulcro-app Root {:initialize-state? false})
  (todo.mutations/init-data! fulcro-app)

  (if hydrate?
    (vreset! root_ (hydrate-root))
    (do
      (vreset! root_ (rdom/createRoot (js/document.getElementById "app")))
      (render-root @root_))))
