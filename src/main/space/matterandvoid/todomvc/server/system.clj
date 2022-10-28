(ns space.matterandvoid.todomvc.server.system
  (:require
    [clojure.pprint :refer [pprint]]
    [cognitect.transit :as transit]
    [com.fulcrologic.fulcro.server.api-middleware :refer [handle-api-request]]
    [com.nivekuil.nexus :as nx]
    [dv.tick-util :as tu]
    [hiccup.page :refer [html5]]
    [malli.dev]
    [muuntaja.core :as muu]
    [muuntaja.middleware]
    [reitit.ring :as reitit-ring]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [ring.adapter.undertow :refer [run-undertow]]
    [ring.middleware.defaults :as ring.defaults :refer [wrap-defaults]]
    [ring.middleware.session :refer [wrap-session]]
    [ring.middleware.session.memory :as session.memory]
    [space.matterandvoid.todomvc.malli-registry]
    [space.matterandvoid.todomvc.server.db :as system.db]
    [space.matterandvoid.todomvc.server.pathom :as pathom-parser]
    [taoensso.timbre :as log])
  (:import [java.util Date]))

(def transit-type "application/transit+json")

(defn make-muuntaja [{:keys [dev?]}]
  (let [muuntaja-config
        (-> muu/default-options
          (assoc :default-format transit-type)
          (update-in
            [:formats transit-type]
            ;; add :verbose true for debugging transit
            merge
            {:decoder-opts {:verbose dev? :handlers tu/tick-transit-reader}
             :encoder-opts {:verbose dev? :handlers (assoc tu/tick-transit-writer-handler-map
                                                      java.lang.Exception (transit/write-handler (constantly "err") str))}}))]
    (muu/create muuntaja-config)))

;; If the response object contains a "Content-Type" header then muuntaja
;; will not encode the body.
;; If it is not present it will encoding using the default encoding if Accept header
;; is not present in the request or is */*
;; So we remove the header here so muuntaja will add it and encode the response body.
(defn api-handler [{:keys [body-params env-map] :as req}]
  (log/info "In Api handler")
  (when body-params
    (handle-api-request body-params (fn [tx]
                                      (log/info "Calling parser:\n" (with-out-str (pprint tx)))
                                      @(pathom-parser/process-tx {:ring/request (dissoc req :env-map)
                                                                  :env-map      env-map
                                                                  :eql-tx       tx})))))

(defn get-js-filename []
  "main.js")

(defn index [csrf-token]
  (if-let [js-filename (get-js-filename)]
    (do (log/info "Serving index.html")
        (html5
          [:head {:lang "en"}
           [:title "Application"]
           [:meta {:charset "utf-8"}]
           [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"}]
           [:link {:rel "shortcut icon" :href "data:image/x-icon;," :type "image/x-icon"}]
           [:link {:href "/styles/index.css" :rel "stylesheet"}]
           [:script (str "var fulcro_network_csrf_token = '" csrf-token "';")]]
          [:body
           [:div#app]
           [:script {:src (str "/js/main/" js-filename)}]]))
    (throw (Exception. (str "Error reading JavaScript filename from shadow-cljs manifest.edn file.
    The filename is nil, you probably need to wait for the shadow-cljs build to complete or start it again.
    Check manifest.edn in the shadow-cljs build directory.")))))

(defn html-response [html]
  {:status 200 :body html :headers {"Content-Type" "text/html"}})

(defn csrf-token [req] (:anti-forgery-token req))

(defn html-handler [req] (html-response (index (csrf-token req))))

(defn wrap-html-handler [handler] (wrap-defaults handler (assoc ring.defaults/site-defaults :session false)))

(defn log-request [handler]
  (fn [req]
    (log/info "REQUEST: " (sort (keys req)))
    (handler req)))

(defn wrap-api-handler
  [handler db-conn now]
  (let [wrapped-handler (wrap-defaults handler (-> ring.defaults/site-defaults
                                                 (assoc-in [:security :anti-forgery] false)
                                                 (assoc-in [:responses :content-types] false)
                                                 (assoc :session false)))]
    (fn [req]
      (wrapped-handler (assoc req :env-map {:db-conn db-conn :now now})))))

(defonce session-map (atom {}))
(def session-store (session.memory/memory-store session-map))

(def muuntaja-instance (make-muuntaja {:dev? true}))

(defn make-handler [db-conn]
  (reitit-ring/ring-handler
    (reitit-ring/router
      [["/" {:handler (wrap-html-handler html-handler)}]
       ["/api" {:post {:handler (wrap-api-handler api-handler db-conn (Date.))}}]]
      {:data {:muuntaja muuntaja-instance
              :middleware
              [muuntaja/format-negotiate-middleware
               muuntaja/format-response-middleware
               muuntaja/format-request-middleware]}})
    (reitit-ring/routes
      (reitit-ring/create-resource-handler {:path "/" :root "public"})
      (reitit-ring/create-default-handler {:not-found (constantly {:status 404 :body "Not Found"})}))
    {:middleware [[wrap-session {:store session-store}]]}))

(nx/def webserver-handler [_] {}
  (make-handler system.db/conn))

(nx/def webserver [{::keys [webserver-handler opts]}]
  {::nx/halt (fn [server]
               (log/info "Stop server: " server)
               (.stop server))}
  (log/info "Starting webserver, opts: " opts)
  (run-undertow webserver-handler opts))

(defn start! [{:keys [port] :or {port 8499}}]
  (nx/init {::opts {:port port}} [::webserver]))

(defn stop! [system] (nx/halt! system))

(defn -main [& args]
  (start! {})
  (.join (Thread/currentThread)))
