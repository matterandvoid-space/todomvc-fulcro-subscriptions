(ns space.matterandvoid.todomvc.todo.model
  (:require
    #?(:clj [datalevin.core])
    [space.matterandvoid.todomvc.server.db :as-alias system.db]
    [com.fulcrologic.fulcro.raw.components :as rc]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [space.matterandvoid.todomvc.util :as util]
    [space.matterandvoid.todomvc.malli-registry :as malli-registry]
    [malli.util :as mu]))

(def required-todo-keys [::id ::text ::completed-state])
(def optional-todo-keys [::completed-at])
(def all-todo-keys (into required-todo-keys optional-todo-keys))
(def global-keys [:db/created-at :db/updated-at :db/id])
(def db-todo-keys (into all-todo-keys global-keys))

(def TodoSchema
  [:map
   ::id
   ::text
   ::completed-state
   [::completed-at {:optional true}]
   [::system.db/created-at {:optional true}]
   [::system.db/updated-at {:optional true}]
   [:db/id {:optional true}]])

(def pathom-output [::id ::text ::completed-state ::completed-at ::system.db/created-at ::system.db/updated-at])

(malli-registry/register!
  (merge {::id                   :uuid
          ::text                 :string
          ::completed-state      [:enum :complete :incomplete]
          ::system.db/created-at [:maybe :inst]
          ::system.db/updated-at [:maybe :inst]
          ::completed-at         [:maybe :inst]
          ::todo                 TodoSchema}))

(defn ident
  ([todo-or-id] [::id (if (map? todo-or-id) (::id todo-or-id) todo-or-id)])
  ([todo-or-id attr] (conj (ident todo-or-id) attr)))

(defn make-todo
  {:malli/schema [:=> [:cat :map] ::todo]}
  [{::keys [id text completed-state completed-at]
    :or    {completed-state :incomplete
            text            ""
            id              (util/uuid)}}]
  (cond->
    {::id              id
     ::text            text
     ::completed-state completed-state}
    completed-at (assoc ::completed-at completed-at)))

(defn fresh-todo
  {:malli/schema [:=> [:cat ::todo] ::todo]}
  [todo]
  (assoc todo ::id (util/uuid)))

(defn complete-todo [date todo]
  (assoc todo ::completed-at date
              ::completed-state :complete))

(defn uncomplete-todo [date todo]
  (-> todo
    (assoc ::completed-state :incomplete)
    #?(:clj (assoc ::system.db/updated-at date))
    (dissoc ::completed-at)))

(defn todo-completed? [todo] (= :complete (::completed-state todo)))

(defn toggle-complete-state [todo] (if (todo-completed? todo) :incomplete :complete))

(def todo-db-query [::id ::text ::completed-state ::completed-at :ui/editing? ::system.db/created-at ::system.db/updated-at])

(def todo-db-component (rc/nc todo-db-query {:componentName `Todo}))

(def todo-component
  (rc/nc (conj todo-db-query fs/form-config-join)
    {:componentName `TodoUI
     :form-fields   #{::text}}))

(def todo-query (rc/get-query todo-component))
