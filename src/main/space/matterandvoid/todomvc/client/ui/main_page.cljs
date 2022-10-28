(ns space.matterandvoid.todomvc.client.ui.main-page
  (:require
    [helix.core :refer [$ defnc]]
    [helix.hooks :as hooks]
    [helix.dom :as d]
    [applied-science.js-interop :as j]
    [goog.object :as gobj]
    ["react" :as react]
    ["react-router-dom" :as router]
    [space.matterandvoid.subscriptions.fulcro :as subs]
    [space.matterandvoid.subscriptions.react-hooks-fulcro :refer [use-sub-map]]
    [space.matterandvoid.todomvc.client.ui.components.input :as ui.input]
    [space.matterandvoid.todomvc.todo.model :as todo.model]
    [space.matterandvoid.todomvc.todo.mutations :as todo.mutations]
    [space.matterandvoid.todomvc.todo.subscriptions :as todo.subscriptions]
    [space.matterandvoid.todomvc.todo.ui :refer [ui-todo-list]]
    [space.matterandvoid.todomvc.client.ui.components.list-footer :refer [ui-list-footer]]))

(defnc main-page []
  (let [fulcro-app         (react/useContext subs/datasource-context)
        current-filter     (j/get (router/useParams) :filter)
        todo-list-args     (react/useMemo (fn [] {:filter current-filter}) #js[current-filter])
        on-clear-completed (hooks/use-callback :once (fn [_] (todo.mutations/delete-completed! fulcro-app)))
        on-save            (hooks/use-callback [] (fn [id] (todo.mutations/save-todo-edits! fulcro-app {:id id})))
        on-delete          (hooks/use-callback [] (fn [id] (todo.mutations/delete-todo! fulcro-app {:id id})))
        on-change          (hooks/use-callback [] (fn [e id] (todo.mutations/set-todo-text! fulcro-app id (.. e -target -value))))
        on-cancel          (hooks/use-callback [] (fn [id] (todo.mutations/cancel-edit-todo! fulcro-app {:id id})))
        on-edit            (hooks/use-callback [] (fn [id] (todo.mutations/edit-todo! fulcro-app {:id id :editing? true})))
        on-toggle-complete (hooks/use-callback [] (fn [id] (todo.mutations/toggle-todo! fulcro-app id)))
        on-toggle-all      (hooks/use-callback [] (fn [] (todo.mutations/toggle-all! fulcro-app)))
        {:keys [form-todo todos-list incomplete-count selected-tab any-completed?]}
        (use-sub-map {:form-todo        [todo.subscriptions/form-todo]
                      :todos-list       [todo.subscriptions/todos-list-main todo-list-args]
                      :selected-tab     [todo.subscriptions/selected-tab {:filter current-filter}]
                      :incomplete-count [todo.subscriptions/incomplete-todos-count]
                      :any-completed?   [todo.subscriptions/any-completed?]})]
    (d/div
      #_(when goog/DEBUG
          (d/div
            (d/div "Form todo:")
            (d/pre (with-out-str (cljs.pprint/pprint form-todo)))))
      (d/div
        (d/section {:class "todoapp"}
          (d/header {:class "header"} (d/h1 "todos")
            ($ ui.input/ui-input {:value       (:todo/text form-todo)
                                  :placeholder "What needs to be done?"
                                  :name        "form-todo"
                                  :class       "new-todo"
                                  :on-change   (fn [v] (todo.mutations/set-todo-text! fulcro-app form-todo v))
                                  :on-submit   (fn [_]
                                                 (when (not-empty (::todo.model/text form-todo))
                                                   (todo.mutations/save-new-todo! fulcro-app)))}))
          (d/section {:class "main"}
            (d/input {:type "checkbox" :id "toggle-all" :class "toggle-all" :on-change on-toggle-all})
            (d/label {:for "toggle-all"} "Mark all as complete")
            ($ ui-todo-list {:todos-list         todos-list
                             :on-save            on-save
                             :on-delete          on-delete
                             :on-change          on-change
                             :on-cancel          on-cancel
                             :on-edit            on-edit
                             :on-toggle-complete on-toggle-complete}))
          ($ ui-list-footer {:incomplete-count   incomplete-count
                             :selected-tab       selected-tab
                             :any-completed?     any-completed?
                             :on-clear-completed on-clear-completed}))

        (d/footer {:class "info"}
          (d/p "Double-click to edit a todo")
          (d/p (d/span "Credits:")
            (d/a {:target "_blank" :href "https://twitter.com/danvingo"} "Daniel Vingo"))
          (d/p (d/span "Part of ")
            (d/a {:target "_blank" :href "http://todomvc.com"} "TodoMVC")))))))
