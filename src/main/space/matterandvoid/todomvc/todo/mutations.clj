(ns space.matterandvoid.todomvc.todo.mutations
  (:require
    [com.wsscode.pathom3.connect.operation :as pco :refer [defmutation]]
    [space.matterandvoid.todomvc.todo.model :as todo.model]
    [space.matterandvoid.todomvc.todo.db :as todo.db]
    [space.matterandvoid.todomvc.todo.subscriptions :as todo.subs]
    [taoensso.timbre :as log]))

(defmutation save-todo [{:keys [db-conn now]} todo]
  (log/info "SAVE TODO: " todo)
  (todo.db/save-todo! db-conn todo now)
  todo)

(defmutation toggle-todo [{:keys [db-conn now]} {::todo.model/keys [id]}]
  (log/info "Toggle TODO: " id)
  (if-let [todo (todo.db/get-todo db-conn id)]
    (todo.db/toggle-todo! db-conn todo now)
    (throw (Exception. (str "Todo does not exist: " (pr-str id)))))
  {})

(defmutation toggle-all [{:keys [db-conn now]} {::todo.model/keys [completed-state]}]
  (log/info "Toggle all TODOs: " completed-state)
  (let [todos         (todo.db/get-todos db-conn)
        all-complete? (todo.subs/all-complete? todos)
        update-fn     (if all-complete? todo.model/uncomplete-todo todo.model/complete-todo)
        todos         (mapv (partial update-fn now) todos)]
    (todo.db/save-todos! db-conn todos now)
    {}))

(defmutation delete-todo [{:keys [db-conn]} {:keys [id]}]
  (log/info "DELETE TODO" id)
  (todo.db/delete-todo! db-conn id)
  {})

(defmutation delete-completed [{:keys [db-conn]} _]
  (let [completed-todos (todo.subs/complete-todos db-conn)]
    (log/info "DELETE COMPLETED")
    (todo.db/delete-todos! db-conn completed-todos)
    {}))

(defn mutations [] [save-todo delete-todo delete-completed toggle-todo toggle-all])
