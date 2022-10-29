(ns space.matterandvoid.todomvc.malli-registry
  (:require
    [malli.core :as m]
    [malli.registry :as mr]
    [malli.util :as mu]
    [datalevin.core :as d])
  (:import [java.time LocalDateTime ZoneId]
           [java.util Date]))

(def start-schemas
  (merge
    (m/base-schemas)
    (m/predicate-schemas)
    (m/type-schemas)
    (m/sequence-schemas)
    (mu/schemas)
    {:inst  (m/-simple-schema
              {:type            :inst
               :pred            inst?
               :type-properties {:decode/string (fn [v]
                                                  (-> v
                                                    (LocalDateTime/parse)
                                                    (.atZone (ZoneId/systemDefault))
                                                    (.toEpochSecond)
                                                    (Date.)))}})
     :db/id (:int (m/type-schemas))}
    {:datalevin-db         (m/-simple-schema {:type :datalevin-db :pred d/db?})
     :datalevin-conn-or-db (m/-simple-schema
                             {:type :datalevin-conn-or-db
                              :pred (fn [x] (or (d/conn? x) (d/db? x)))})}))

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
