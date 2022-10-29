(ns space.matterandvoid.todomvc.todo.subscriptions
  (:require
    [reagent.ratom]
    [space.matterandvoid.subscriptions.fulcro :as fulcro.subs :refer [deflayer2-sub defsub defsubraw]]
    [space.matterandvoid.subscriptions.fulcro-eql :as subs.eql]
    [space.matterandvoid.todomvc.todo.model :as todo.model]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.application :as fulcro.app]))

(defsub selected-tab
  (fn [_ {:keys [filter]}]
    (case filter "completed" :completed, "active" :active, :all)))

(deflayer2-sub todos-list :root/todo-list)
(deflayer2-sub form-todo-ident :root/new-todo)

(defsubraw form-todo
  [db_]
  (when-let [ident (:root/new-todo @db_ nil)]
    (get-in @db_ ident nil)))

(defsub fresh-todo-from-form :<- [form-todo] todo.model/fresh-todo)


(def todo-eql (subs.eql/create-component-subs todo.model/todo-component {}))

(defn get-todo [fulcro-app todo-id]
  (fdn/db->tree todo.model/todo-query (todo.model/ident todo-id) (fulcro.app/current-state fulcro-app))
  ;; same as:
  ;(todo-eql fulcro-app {::todo.model/id todo-id subs.eql/query-key todo.model/todo-query})

  )

;; This is equivalent to the below version using db->tree, however the subscriptions are cached so should result in less
;; re-renders than using db->tree directly which must deref the state atom any time the state atom changes, which is
;; quite costly as the entire todolist will be recomputed (db->tree invocation per todoitem) - obviously in this app
;; it doesn't matter, but the point is using the EQL lib will result in more significant performance gains for larger
;; applications.

(def todos-list-full (subs.eql/expand-ident-list-sub todos-list todo-eql))

;(defsubraw todos-list-full [db_]
;  (->> (todos-list db_) (mapv #(fdn/db->tree todo.model/todo-query % @db_))))

(defsub incomplete-todos :<- [todos-list-full] #(remove todo.model/todo-completed? %))
(defsub complete-todos :<- [todos-list-full] #(filter todo.model/todo-completed? %))

(defsub todos-list-main :<- [todos-list-full] :<- [incomplete-todos] :<- [complete-todos] :<- [selected-tab]
  (fn [[todos incomplete complete tab]]
    (case tab
      :all todos
      :active incomplete
      :completed complete)))

(defsub all-complete? :<- [todos-list-full] #(every? todo.model/todo-completed? %))
(defsub complete-todos-count :<- [todos-list-full] #(count (filter todo.model/todo-completed? %)))
(defsub incomplete-todos-count :<- [todos-list-full] #(count (remove todo.model/todo-completed? %)))
(defsub any-completed? :<- [complete-todos] #(< 0 (count %)))
(defsub my-dynamic-sub :-> #(:dyn-sub % [complete-todos-count]))

(comment (meta my-dynamic-sub)
  (meta todos-list-full))
