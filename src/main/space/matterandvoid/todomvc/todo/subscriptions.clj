(ns space.matterandvoid.todomvc.todo.subscriptions
  (:require
    [space.matterandvoid.subscriptions.datalevin-eql :as subs.eql]
    [space.matterandvoid.todomvc.server.db :as-alias system.db]
    [space.matterandvoid.todomvc.todo.model :as todo.model]
    [space.matterandvoid.todomvc.todo.db :as todo.db]))

(def todo-eql (subs.eql/create-component-subs todo.model/todo-db-component {}))

(defn complete-todos [conn] (filterv todo.model/todo-completed? (todo.db/get-todos conn)))
(defn incomplete-todos [conn] (vec (remove todo.model/todo-completed? (todo.db/get-todos conn))))
(defn all-complete? [todos] (every? todo.model/todo-completed? todos))
(defn sorted-todos [conn] (sort-by ::system.db/created-at (todo.db/get-todos conn)))

