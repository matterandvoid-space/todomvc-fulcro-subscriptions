(ns space.matterandvoid.todomvc.todo.mutations
  (:require
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as nstate]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [edn-query-language.core :as eql]
    [space.matterandvoid.todomvc.todo.model :as todo.model]
    [space.matterandvoid.todomvc.todo.subscriptions :as todo.subscriptions]
    [space.matterandvoid.todomvc.util :as util]))

(defmutation set-filter-type [{:keys [filter-val]}]
  (action [{:keys [state]}]
    (swap! state assoc :selected-tab filter-val)))

(defn set-filter-type! [app t] (rc/transact! app [(set-filter-type {:filter-val t})]))

(defmutation set-todo-text [{:keys [value]}]
  (action [{:keys [state ref]}]
    (swap! state (fn [s] (assoc-in s (conj ref ::todo.model/text) value)))))

(defn set-todo-text! [app todo-or-id value]
  (rc/transact! app [(set-todo-text {:value value})] {:ref (todo.model/ident todo-or-id) :compressible? true}))

(defn toggle-todo*
  [state now id]
  (let [todo     (get-in state (todo.model/ident id))
        new-todo (if (todo.model/todo-completed? todo)
                   (todo.model/uncomplete-todo now todo)
                   (todo.model/complete-todo now todo))]
    (update-in state (todo.model/ident id) merge new-todo)))

(defmutation toggle-todo [{::todo.model/keys [id]}]
  (action [{:keys [state]}]
    (swap! state toggle-todo* (js/Date.) id))
  (remote [_] true))

(defn toggle-todo! [app id]
  (rc/transact! app [(toggle-todo {::todo.model/id id})]))

(defn toggle-all* [state]
  (let [all-todo-idents (todo.subscriptions/todos-list state)
        now             (js/Date.)
        update-fn       (if (todo.subscriptions/all-complete? state) todo.model/uncomplete-todo todo.model/complete-todo)]
    (reduce (fn [acc ident] (update-in acc ident (partial update-fn now)))
      state all-todo-idents)))

(defmutation toggle-all [_]
  (action [{:keys [state]}]
    (swap! state toggle-all*))
  (remote [_] true))

(defn toggle-all! [app] (rc/transact! app [(toggle-all)]))

(defn save-new-todo* [state new-todo]
  (let [new-todo  (select-keys new-todo todo.model/todo-db-query)
        new-ident (todo.model/ident new-todo)]
    (-> state
      (assoc-in new-ident new-todo)
      (fs/add-form-config* todo.model/todo-component new-ident)
      (fs/pristine->entity* (todo.subscriptions/form-todo-ident state))
      (update :root/todo-list conj new-ident))))

(defmutation save-new-todo [new-todo]
  (action [{:keys [state]}]
    (swap! state save-new-todo* new-todo))
  (remote [_] (eql/query->ast1 `[(save-todo ~(todo.model/make-todo new-todo))])))

(defn save-new-todo! [app] (rc/transact! app [(save-new-todo (todo.subscriptions/fresh-todo-from-form app))]))

(defmutation save-todo-edits [{:keys [id]}]
  (action [{:keys [state]}]
    (swap! state (fn [s] (-> s
                           (assoc-in (todo.model/ident id :ui/editing?) false)
                           (fs/entity->pristine* (todo.model/ident id))))))
  (remote [{:keys [app]}]
    (eql/query->ast1 `[(save-todo ~(todo.model/make-todo (todo.subscriptions/get-todo app id)))])))

(defn save-todo-edits! [app args] (rc/transact! app [(save-todo-edits args)]))

(defmutation delete-todo [{:keys [id]}]
  (action [{:keys [state]}]
    (swap! state (fn [s] (-> s (nstate/remove-entity (todo.model/ident id))))))
  (remote [_] true))

(defn delete-todo! [app args] (rc/transact! app [(delete-todo args)]))

(defmutation delete-completed [_]
  (action [{:keys [state]}]
    (let [completed (todo.subscriptions/complete-todos state)]
      (swap! state
        (fn [s]
          (reduce (fn [acc todo] (nstate/remove-entity acc (todo.model/ident todo)))
            s
            completed)))))
  (remote [_] true))

(defn delete-completed! [app] (rc/transact! app [(delete-completed)]))

(defmutation edit-todo [{:keys [id editing?]}]
  (action [{:keys [state]}]
    (swap! state (fn [s] (assoc-in s (todo.model/ident id :ui/editing?) editing?)))))

(defn edit-todo! [app args] (rc/transact! app [(edit-todo args)]))

(defmutation cancel-edit-todo [{:keys [id]}]
  (action [{:keys [state]}]
    (swap! state (fn [s] (-> s
                           (assoc-in (todo.model/ident id :ui/editing?) false)
                           (fs/pristine->entity* (todo.model/ident id)))))))

(defn cancel-edit-todo! [app args] (rc/transact! app [(cancel-edit-todo args)]))

(defmutation setup-new-todo
  "Used at app boot to create the state for the new todo form"
  [{:keys [id]}]
  (action [{:keys [state]}]
    (swap! state
      (fn [s]
        (let [new-todo (todo.model/make-todo {::todo.model/id id})]
          (-> s
            (merge/merge-component todo.model/todo-component new-todo)
            (fs/add-form-config* todo.model/todo-component (todo.model/ident id))
            (assoc :root/new-todo (todo.model/ident id))))))))

(defn setup-new-todo! [app args] (rc/transact! app [(setup-new-todo args)]))

(defmutation init-todos-list [_]
  (action [{:keys [state]}]
    (let [todos (todo.subscriptions/todos-list state)]
      (swap! state
        (fn [s]
          (reduce (fn [acc todo] (fs/add-form-config* acc todo.model/todo-component todo))
            s todos))))))

(defn load-todos! [fulcro-app]
  (df/load! fulcro-app :root/todo-list todo.model/todo-db-component
    {:post-mutation `init-todos-list}))

(defn init-data! [fulcro-app]
  (setup-new-todo! fulcro-app {:id (util/uuid)})
  (load-todos! fulcro-app))
