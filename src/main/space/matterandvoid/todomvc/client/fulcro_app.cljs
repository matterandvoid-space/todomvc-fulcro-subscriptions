(ns space.matterandvoid.todomvc.client.fulcro-app
  (:require
    [com.fulcrologic.fulcro.raw.application :as fulcro.app]
    [com.fulcrologic.fulcro.networking.http-remote :as fulcro.http]
    [edn-query-language.core :as eql]
    [space.matterandvoid.subscriptions.fulcro :as subs]
    [taoensso.timbre :as log]))

(defn global-eql-transform
  [ast]
  (cond-> (fulcro.app/default-global-eql-transform ast)
    ;; For *queries*, make sure that if Pathom sends errors, Fulcro does not remove it:
    (not= :call (:type ast)) ; skip mutations
    (update :children conj (eql/expr->ast :com.wsscode.pathom3.connect.runner/attribute-errors))))

(def request-middleware
  (->
    (fulcro.http/wrap-csrf-token (or (and (exists? js/fulcro_network_csrf_token) js/fulcro_network_csrf_token) "TOKEN NO IN HTML!"))
    (fulcro.http/wrap-fulcro-request)))

(defonce fulcro-app
  (subs/with-headless-fulcro
    (fulcro.app/fulcro-app {:initial-db
                            {:selected-tab :all}

                            :global-eql-transform
                            global-eql-transform

                            :global-error-action
                            (fn [{{:keys [body status-code error-text]} :result :as env}]
                              (log/error "WARN: Remote call failed"
                                status-code
                                error-text
                                (:com.wsscode.pathom3.connect.runner/attribute-errors body)))

                            :remote-error?
                            (fn [result]
                              (or
                                (fulcro.app/default-remote-error? result)
                                (:com.wsscode.pathom3.connect.runner/attribute-errors (:body result))))

                            :remotes
                            {:remote (fulcro.http/fulcro-http-remote {:url "/api" :request-middleware request-middleware})}})))
