(ns space.matterandvoid.todomvc.malli-registry
  (:require
    [malli.core :as m]
    [malli.registry :as mr]
    [malli.util :as mu]
    ["react" :as react]))

(def start-schemas
  (merge
    (m/base-schemas)
    (m/predicate-schemas)
    (m/type-schemas)
    (m/sequence-schemas)
    (mu/schemas)
    {:inst          (m/-simple-schema
                      {:type            :inst
                       :pred            inst?
                       :type-properties {:decode/string (fn [v] (js/Date. v))}})
     :db/id         (:int (m/type-schemas))
     :react-element (m/-simple-schema {:type :react-element :pred react/isValidElement})}))

(defonce registry* (atom start-schemas))
(comment (reset! registry* start-schemas))

(defn register!
  "With one argument takes a map of schemas to merge into the registry,
   or with two arguments: one key -> schema pair to assoc into the registry.
   Returns nil."
  ([m]
   (swap! registry* merge m) nil)

  ([type schema]
   (swap! registry* assoc type schema) nil))

(mr/set-default-registry! (mr/mutable-registry registry*))
