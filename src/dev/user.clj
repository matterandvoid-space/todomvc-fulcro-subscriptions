(ns user
  (:require
    [malli.dev :as md]
    [lambdaisland.classpath.watch-deps :as watch-deps]
    [space.matterandvoid.todomvc.malli-registry]
    [space.matterandvoid.todomvc.server.db :as system.db]
    [space.matterandvoid.todomvc.todo.model :as todo.model]
    [space.matterandvoid.todomvc.server.pathom :as pathom-parser]
    [space.matterandvoid.todomvc.server.system :refer [start! stop!]]))

(comment
  (md/start!)
  (watch-deps/start! {:aliases [:dev]})
  (def s (start! {}))
  (stop! s)
  )

;; example executing the pathom parser for debugging

(comment
  @(pathom-parser/process-tx {:ring/request {}
                              :env-map      {:db-conn system.db/conn}
                              :eql-tx       [{:root/todo-list
                                              [::todo.model/id
                                               ::todo.model/text
                                               ::todo.model/completed-state
                                               ::todo.model/completed-at
                                               ::system.db/created-at
                                               ::system.db/updated-at]}
                                             :com.wsscode.pathom3.connect.runner/attribute-errors]}))
