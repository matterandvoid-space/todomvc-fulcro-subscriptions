(ns space.matterandvoid.todomvc.todo.db
  (:require
    [space.matterandvoid.todomvc.server.db :as-alias system.db]
    [space.matterandvoid.todomvc.todo.model :as todo.model]
    [space.matterandvoid.todomvc.server.db-util :refer [->db]]
    [datalevin.core :as d]
    [taoensso.timbre :as log]))

(def schema
  {::todo.model/id              {:db/valueType :db.type/uuid :db/unique :db.unique/identity}
   ::todo.model/text            {:db/valueType :db.type/string :db/fulltext true}
   ::todo.model/completed-state {:db/valueType :db.type/keyword}
   ::todo.model/completed-at    {:db/valueType :db.type/instant}})

(defn get-todo [db-or-conn todo-or-id]
  (when-let [entid (d/entid (->db db-or-conn) (todo.model/ident todo-or-id))]
    (d/pull (->db db-or-conn) todo.model/pathom-output entid)))

(defn get-todos
  {:malli/schema [:function
                  [:=> [:cat :datalevin-conn-or-db] [:vector ::todo.model/todo]]
                  [:=> [:cat :datalevin-conn-or-db [:sequential ::todo.model/todo]] [:vector ::todo.model/todo]]]}
  ([db-or-conn]
   (mapv first (d/q [:find (list 'pull '?todo todo.model/pathom-output)
                     :where '[?todo ::todo.model/id]]
                 (->db db-or-conn))))
  ([db-or-conn todos]
   (mapv first (d/q
                 [:find (list 'pull '?todo todo.model/pathom-output)
                  :in '$ '[?todo-id ...]
                  :where '[?todo ::todo.model/id ?todo-id]]
                 (->db db-or-conn)
                 (if (-> todos first uuid?)
                   todos
                   (mapv ::todo.model/id todos))))))

;; copied from weavejester/medley
(defn index-by
  "Returns a map of the elements of coll keyed by the result of f on each
  element. The value at each key will be the last element in coll associated
  with that key. This function is similar to `clojure.core/group-by`, except
  that elements with the same key are overwritten, rather than added to a
  vector of values."
  [f coll]
  (persistent! (reduce #(assoc! %1 (f %2) %2) (transient {}) coll)))

(defn save-todos! [conn todos now]
  (let [existing-todos (index-by ::todo.model/id (get-todos conn todos))
        todos          (mapv (fn [t]
                               (let [existing (get existing-todos (::todo.model/id t))
                                     t'       (merge existing t)]
                                 (cond-> (assoc t' ::system.db/updated-at now)
                                   (not (contains? t' ::system.db/created-at))
                                   (assoc ::system.db/created-at now))))
                         (sort-by ::todo.model/id todos))]
    (log/info "TRANSACTING: " todos)
    (d/transact! conn todos)))

(defn save-todo! [conn todo now]
  (save-todos! conn [todo] now))

(defn toggle-todo! [conn todo now]
  (let [curr-complete-state (::todo.model/completed-state todo)
        new-complete-state  (todo.model/toggle-complete-state todo)
        ent-id              (d/entid (->db conn) (todo.model/ident todo))
        tx-data             (cond->
                              [[:db/add ent-id ::system.db/updated-at now]
                               [:db/cas ent-id ::todo.model/completed-state curr-complete-state new-complete-state]]
                              (= new-complete-state :complete) (conj [:db/add ent-id ::todo.model/completed-at now]))]
    (log/info "TOGGLE TODO: " todo)
    (d/transact! conn tx-data)))

(defn delete-todo! [conn todo-or-id]
  (d/transact! conn [[:db.fn/retractEntity (d/entid (d/db conn) (todo.model/ident todo-or-id))]]))

(defn delete-todos! [conn todos]
  (d/transact! conn
    (mapv #(do [:db.fn/retractEntity (d/entid (d/db conn) (todo.model/ident %))]) todos)))
