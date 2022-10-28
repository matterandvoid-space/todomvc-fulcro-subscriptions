(ns space.matterandvoid.todomvc.todo.resolvers
  (:require
    [com.wsscode.pathom3.connect.operation :as pco :refer [defresolver]]
    [space.matterandvoid.todomvc.todo.subscriptions :as todo.subs]
    [space.matterandvoid.todomvc.todo.model :as todo.model]
    [taoensso.timbre :as log]))

(defresolver get-todos [{:keys [db-conn]} _]
  {::pco/output [{:root/todo-list [todo.model/pathom-output]}]}
  {:root/todo-list (todo.subs/sorted-todos db-conn)})

(defn resolvers [] [get-todos])
