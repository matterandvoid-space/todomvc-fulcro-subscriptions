(ns space.matterandvoid.todomvc.server.node-server
  (:refer-clojure :exclude [uuid])
  (:require-macros [hiccups.core :as hiccups])
  (:require
    [applied-science.js-interop :as j]
    [clojure.string :as str]
    [com.fulcrologic.fulcro.algorithms.denormalize :as dnorm]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.application :as fulcro.app]
    [space.matterandvoid.todomvc.todo.subscriptions :as todo.subscriptions]
    [com.fulcrologic.fulcro.components :as c :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.inspect.inspect-client :as fulcro.inspect]
    [com.fulcrologic.fulcro.mutations :as mut :refer [defmutation]]
    [com.fulcrologic.fulcro.networking.http-remote :as fulcro.http]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [edn-query-language.core :as eql]
    [goog.object :as g]
    [helix.core :refer [$ <> defnc fnc]]
    [helix.dom :as d]
    [hiccups.runtime :as hiccupsrt]
    [space.matterandvoid.todomvc.client.fulcro-app :refer [fulcro-app]]
    [space.matterandvoid.subscriptions.fulcro :as subs :refer [<sub defregsub defsub reg-sub]]
    [space.matterandvoid.subscriptions.fulcro-eql :as subs.eql]
    [space.matterandvoid.todomvc.todo.model :as todo.model]
    [space.matterandvoid.todomvc.client.ui.main-page :refer [main-page]]
    [space.matterandvoid.todomvc.todo.mutations :as todo.mutations]
    ["react" :as react]
    ["react-dom/client" :as rdom]
    ["react-hook-form" :as react-form :refer [useForm]]
    ["react-router-dom/server" :as react-router]
    [taoensso.timbre :as log]
    ["http" :as http]
    ["react-dom/server" :as react-dom]
    ["react" :as react]))

(defn router []
  ($ react-router/unstable_createStaticRouter
    #js[#js{:path         "/"
            :element      ($ main-page)
            :errorElement (d/h1 "ERROR")}
        #js{:path "/another-route" :element (d/div "Another route")}
        #js{:path "/somewhere" :element (d/div "Somewhere")}]))
(comment (router))

(defnc app [req]
  (helix.core/provider {:context subs/datasource-context :value fulcro-app}
    (d/div
      ($ react-router/unstable_StaticRouterProvider {:router (router) :context #js{:location (.-url req)}}))))

(defonce server-ref (volatile! nil))

(defn get-js-filename []
  (str "http://localhost:8499/js/main/main.js"))

(defn index-page [csrf-token app-src]
  (let [title      "My application"
        stylesheet "http://localhost:8499/styles/index.css"
        script-src (get-js-filename)]
    (str
      "<!DOCTYPE html>
      <html lang=\"en\">
      <head>
      <title>" title "</title>
      <meta charset=\"utf-8\">
      <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">
      <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">
      <link rel=\"stylesheet\" href=" stylesheet ">
      </head>
      <body>
      <script>var fulcro_network_csrf_token=\"" csrf-token "\"</script>
      <div id=\"app\">" app-src "</div>
      <script src=\"" script-src "\"></script>
      </body>
      </html>
    ")))
(comment (index-page "token" (react-dom/renderToString ($ app {:req #js{:url "/"}}))))

(defn request-handler [req ^js res]
  (.log js/console (g/getKeys req))
  (.log js/console (pr-str (select-keys (j/lookup req) [:url :statusCode :method :client :httpVersion :headers])))
  (.end res (index-page "token" (react-dom/renderToString ($ app {:req req})))))

(defn main [& args]
  (log/info "starting server ")
  (let [server (http/createServer #(request-handler %1 %2))]
    (.listen server 3020
      (fn [err]
        (if err
          (log/info "server failed to start")
          (log/info "server running on port 3020"))))
    (vreset! server-ref server)))

(defn start []
  (log/info "start called")
  (main))

(defn stop
  [done]
  (log/info "stop called")
  (when-some [srv @server-ref]
    (.close srv
      (fn [err]
        (log/info "stop completed " err)
        (done)))))

;(log/info "__filename: " js/__filename)
