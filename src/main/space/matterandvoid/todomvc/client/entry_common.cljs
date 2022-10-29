(ns space.matterandvoid.todomvc.client.entry-common
  (:require
    ["react" :as react]
    ["react-dom/client" :as rdom]
    [com.fulcrologic.fulcro.application :as fulcro.app]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [helix.core :refer [$ defnc]]
    [helix.dom :as d]
    [space.matterandvoid.subscriptions.fulcro :as subs]
    [space.matterandvoid.todomvc.client.fulcro-app :refer [fulcro-app]]
    [space.matterandvoid.todomvc.client.ui.main-page :refer [main-page]]
    [space.matterandvoid.todomvc.todo.mutations :as todo.mutations]
    ["react-router-dom" :as react-router]))

(def Root (rc/nc [] {:componentName `Root}))

(def router (react-router/createHashRouter
              #js[#js{:path         "/"
                      :element      ($ main-page)
                      :errorElement (d/h1 "ERROR")}
                  #js{:path ":filter" :element ($ main-page)}]))

(defnc app []
  (helix.core/provider {:context subs/datasource-context :value fulcro-app}
    ($ react-router/RouterProvider {:router router})))

(defonce root_ (volatile! nil))

(defn render-root [strict? root]
  (.render root (if strict? ($ react/StrictMode ($ app)) ($ app))))

(defn hydrate-root [strict?]
  (rdom/hydrateRoot (js/document.getElementById "app")
    (if strict? ($ react/StrictMode ($ app)) ($ app))))

;; in the future you could have the node.js server add a flag in the rendered document to determine if you're
;; hydrating from node.js or just using clojure backend.
(def hydrate? false)

(defn init [strict?]
  (fulcro.app/set-root! fulcro-app Root {:initialize-state? false})
  (todo.mutations/init-data! fulcro-app)
  (if hydrate?
    (vreset! root_ (hydrate-root strict?))
    (do
      (vreset! root_ (rdom/createRoot (js/document.getElementById "app")))
      (render-root strict? @root_))))
