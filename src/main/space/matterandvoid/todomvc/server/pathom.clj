(ns space.matterandvoid.todomvc.server.pathom
  (:require
    [com.wsscode.pathom3.connect.indexes :as pci]
    [com.wsscode.pathom3.error :as pc.err]
    [com.wsscode.pathom3.connect.runner :as pcr]
    [com.wsscode.pathom3.interface.async.eql :as p.a.eql]
    [space.matterandvoid.todomvc.todo.resolvers :as todo.resolvers]
    [space.matterandvoid.todomvc.todo.mutations :as todo.mutations]
    [edn-query-language.core :as eql]))

(defn resolvers [] [(todo.mutations/mutations) (todo.resolvers/resolvers)])

(defn default-env []
  (-> {::pc.err/lenient-mode? true}
    (pci/register (resolvers))))

;; Fulcro requires that we query for ::pcr/attribute-errors if we want our code
;; to see it but Pathom complains about it (see https://github.com/wilkerlucio/pathom3/issues/156)
;; (I could also make a Pathom plugin to do this but this is easier for me)
(defn- omit-error-attribute [eql]
  (-> (eql/query->ast eql)
    (update :children (partial remove #(= {:type :prop, :key ::pcr/attribute-errors} (select-keys % [:type :key]))))
    (eql/ast->query)))

(defn process [env eql]
  (p.a.eql/process (merge (default-env) env) (omit-error-attribute eql)))

(defn make-pathom-env
  [ring-request env-map eql-transaction]
  (let [children     (-> eql-transaction eql/query->ast :children)
        query-params (reduce (fn [acc {:keys [type params]}]
                               (cond-> acc
                                 (and (not= :call type) (seq params))
                                 (merge params)))
                       {}
                       children)]
    (merge
      env-map
      {:ring/request ring-request
       :query-params query-params})))

(defn process-tx
  [{:keys [ring/request env-map eql-tx]}]
  (process (make-pathom-env request env-map eql-tx) eql-tx))
