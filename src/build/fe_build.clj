(ns fe-build
  (:require
    [shadow.cljs.devtools.config :as config]
    [shadow.cljs.build-report :as report]
    [shadow.cljs.devtools.api :as sh]))

(defn get-config [build-id]
  (get-in
    (assoc-in
      (config/load-cljs-edn!)
      [:builds build-id :modules :main :init-fn] 'space.matterandvoid.todomvc.client.entry/init)
    [:builds build-id]))

(defn release-build [{:keys [id] :or {id :main}}]
  (sh/with-runtime
    (let [build-config (get-config id)]
      (sh/release* build-config {})))
  :done)

(defn build-report [{:keys [id] :or {id :main}}]
  ;(println "id: " id)
  ;(println "type: " (type id))
  (report/generate (get-config id)
    {:print-table true
     :report-file "fe-report.html"})
  :done)
