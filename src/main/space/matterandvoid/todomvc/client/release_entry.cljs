(ns space.matterandvoid.todomvc.client.release-entry
  (:require
    [space.matterandvoid.todomvc.client.entry-common :as common]))

(defn init {:export true} [] (common/init false))
