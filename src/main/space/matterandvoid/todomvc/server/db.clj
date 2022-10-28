(ns space.matterandvoid.todomvc.server.db
  (:require
    [datalevin.core :as d]
    [space.matterandvoid.todomvc.todo.db :as todo.db]))

(def schema (merge
              {::created-at {:db/valueType :db.type/instant}
               ::updated-at {:db/valueType :db.type/instant}}
              todo.db/schema))

(def conn (d/get-conn (str "/tmp/datalevin/db") schema))
