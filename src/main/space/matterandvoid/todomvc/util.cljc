(ns space.matterandvoid.todomvc.util
  (:refer-clojure :exclude [uuid])
  (:require
    [com.yetanalytics.squuid :refer [generate-squuid]]))

(defn uuid [] (generate-squuid))
