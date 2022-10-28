(ns space.matterandvoid.todomvc.malli-registry
  (:require
    [malli.core :as m]
    [malli.registry :as mr]
    [malli.util :as mu]
    #?(:cljs ["react" :as react])
    #?(:clj [datalevin.core :as d]))
  #?(:clj (:import [java.time LocalDateTime ZoneId]
                   [java.util Date])))

(def start-schemas
  (merge
    (m/base-schemas)
    (m/predicate-schemas)
    (m/type-schemas)
    (select-keys (m/sequence-schemas) [:cat])
    (mu/schemas)
    {:inst  (m/-simple-schema
              {:type            :inst
               :pred            inst?
               :type-properties {:decode/string (fn [v]
                                                  #?(:clj
                                                     (-> v
                                                       (LocalDateTime/parse)
                                                       (.atZone (ZoneId/systemDefault))
                                                       (.toEpochSecond)
                                                       (Date.))
                                                     :cljs (js/Date. v)))}})
     :db/id (:int (m/type-schemas))}
    #?(:clj
       {:datalevin-db         (m/-simple-schema {:type :datalevin-db :pred d/db?})
        :datalevin-conn-or-db (m/-simple-schema
                                {:type :datalevin-conn-or-db
                                 :pred (fn [x] (or (d/conn? x) (d/db? x)))})}
       :cljs
       {:react-element (m/-simple-schema {:type :react-element :pred react/isValidElement})})))

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
