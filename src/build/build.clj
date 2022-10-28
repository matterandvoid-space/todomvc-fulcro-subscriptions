(ns build
  (:require
    [clojure.tools.build.api :as b]
    [clojure.edn :as edn]
    [clojure.string :as str]
    [org.corfield.build :as bb])
  (:import [java.time LocalDate]))

(def lib 'space.matterandvoid/todomvc)
(def version (str/replace (str (LocalDate/now)) "-" "."))

(defn clean [opts] (bb/clean opts))

(defn uberjar [opts]
  (-> opts
    (bb/clean)
    (assoc :lib lib :version version :src-dirs ["src/main"] #_#_:src-pom "template/pom.xml"
      :java-opts (get-in (edn/read-string (slurp "deps.edn")) [:aliases :jvm-options :jvm-opts])
      :main 'space.matterandvoid.todomvc.server.system)
    (bb/uber)))

(defn install [opts]
  (-> opts
    (assoc :lib lib :version version :src-dirs ["src/main"] :src-pom "template/pom.xml")
    (bb/install)))

(defn deploy "Deploy the JAR to Clojars." [opts]
  (-> opts
    (assoc :lib lib :version version)
    (bb/deploy)))
