(ns space.matterandvoid.todomvc.server.db-util
  (:require
    [datalevin.core :as d]))

(defn ->db
  {:malli/schema [:=> [:cat :datalevin-conn-or-db] :datalevin-db]}
  [db-or-conn]
  (cond-> db-or-conn (d/conn? db-or-conn) d/db))
