(ns space.matterandvoid.todomvc.todo.subscriptions
  (:require
    [reagent.ratom]
    [space.matterandvoid.subscriptions.fulcro :as fulcro.subs :refer [deflayer2-sub defsub defsubraw]]
    ;[space.matterandvoid.subscriptions.fulcro-eql :as subs.eql]
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


(defn get-todo [fulcro-app todo-id]
  (fdn/db->tree todo.model/todo-query (todo.model/ident todo-id) (fulcro.app/current-state fulcro-app))
  ;(todo-eql fulcro-app {::todo.model/id todo-id subs.eql/query-key todo.model/todo-query})
  )

;; These are equivalent to the below db->tree call.
;(def todo-eql
;  (fulcro.subs/with-name
;    (subs.eql/create-component-subs todo.model/todo-component {})
;    (keyword `todo-eql)))

;(subs.eql/create-component-subs todo.model/todo-component {})

;(def todos-list-full
;  (fulcro.subs/with-name (subs.eql/expand-ident-list-sub todos-list todo-eql) (keyword `todos-list-full)))

(defsubraw todos-list-full
  [db_]
  (->> (todos-list db_) (mapv #(fdn/db->tree todo.model/todo-query % @db_))))

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
