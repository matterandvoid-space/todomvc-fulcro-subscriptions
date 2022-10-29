(ns space.matterandvoid.todomvc.malli-registry-release
  (:require
    [malli.core :as m]
    [malli.registry :as mr]))

(def start-schemas
  (merge
    (m/base-schemas)
    (m/predicate-schemas)
    (m/type-schemas)
    {:inst          (m/-simple-schema
                      {:type            :inst
                       :pred            inst?
                       :type-properties {:decode/string (fn [v] (js/Date. v))}})
     :db/id         (:int (m/type-schemas))}))

(defonce registry* (atom start-schemas))

(defn register!
  "With one argument takes a map of schemas to merge into the registry,
   or with two arguments: one key -> schema pair to assoc into the registry.
   Returns nil."
  ([m]
   (swap! registry* merge m) nil)

  ([type schema]
   (swap! registry* assoc type schema) nil))

(mr/set-default-registry! (mr/mutable-registry registry*))
