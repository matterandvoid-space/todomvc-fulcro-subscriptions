(ns space.matterandvoid.todomvc.client.ui.components.list-footer
  (:require
    ["react-router-dom" :refer [NavLink]]
    [helix.core :refer [$ defnc]]
    [helix.dom :as d]))

(defnc ui-list-footer [{:keys [incomplete-count any-completed? selected-tab on-clear-completed]}]
  {:wrap [helix.core/memo]}
  (d/footer {:class "footer"}
    (d/span {:class "todo-count"} (d/strong incomplete-count) " items left")
    (d/ul {:class "filters"}
      (d/li ($ ^:native NavLink {:to    "/"
                                 ;:on-click (fn [_] (todo.mutations/set-filter-type! fulcro-app :all))
                                 :class (when (= :all selected-tab) "selected")} "All"))
      (d/li ($ ^:native NavLink {:to    "/active"
                                 ;:on-click (fn [_] (todo.mutations/set-filter-type! fulcro-app :active))
                                 :class (when (= :active selected-tab) "selected")} "Active"))

      (d/li ($ ^:native NavLink {:to    "/completed"
                                 ;:onClick   (fn [_] (todo.mutations/set-filter-type! fulcro-app :completed))
                                 :class (when (= :completed selected-tab) "selected")} "Completed")))
    (when any-completed?
      (d/button {:class    "clear-completed"
                 :on-click on-clear-completed}
        "Clear completed"))))
