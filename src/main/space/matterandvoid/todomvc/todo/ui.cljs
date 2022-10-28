(ns space.matterandvoid.todomvc.todo.ui
  (:require
    [applied-science.js-interop :as j]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [helix.core :refer [defnc $]]
    [helix.dom :as d]
    [helix.hooks :as hooks]
    [space.matterandvoid.todomvc.todo.model :as todo.model]
    [goog.object :as gobj]
    [taoensso.timbre :as log]
    ["react-hook-form" :refer [useForm]]))

(defn render-todo-item
  {:malli/schema
   [:=> [:cat [:merge [:map
                       [:ui/editing? {:optional true} [:maybe :boolean]]
                       [:on-save fn?]
                       [:on-change fn?]
                       [:on-cancel fn?]
                       [:on-edit fn?]
                       [:on-toggle-complete fn?]
                       [:on-delete fn?]]
               ::todo.model/todo]]
    :react-element]}
  [{:ui/keys          [editing?]
    :keys             [on-save on-change on-cancel on-edit on-toggle-complete on-delete]
    ::todo.model/keys [text id] :as todo}]
  (let [input-ref    (hooks/use-ref nil)
        complete?    (todo.model/todo-completed? todo)
        field-name   (str id)
        {::fs/keys [pristine-state]} (::fs/config todo)
        form-methods (useForm #js{:defaultValues (js-obj field-name text)})]
    (hooks/use-effect [editing?] (when editing? (.focus @input-ref)))
    (d/li {:on-double-click (fn [] (on-edit id))
           :class           (cond editing? "editing" complete? "completed")}
      (d/div {:class "view"}
        (d/input {:class     "toggle"
                  :type      "checkbox"
                  :on-change (fn [] (on-toggle-complete id))
                  :checked   complete?})
        (d/label text)
        (d/button {:class    "destroy"
                   :on-click (fn [] (on-delete id))}))
      (j/let [^:js {:keys [onChange onBlur ref]} (.register form-methods field-name
                                                   #js {:onBlur   (fn [] (on-save id))
                                                        :onChange (fn [e] (on-change e id))})]
        (d/input {:type      "text"
                  :class     "edit"
                  :onChange  onChange
                  :onBlur    onBlur
                  :name      field-name
                  :ref       (fn [e] (ref e) (reset! input-ref e))
                  :onKeyDown (fn [e]
                               (when (= "Enter" (j/get e :key)) (on-save id))
                               (when (= "Escape" (j/get e :key))
                                 (on-cancel id)
                                 (.resetField form-methods field-name #js{:defaultValue (::todo.model/text pristine-state)})))})))))

(defnc todo-item
  [todo]
  {:wrap [helix.core/memo]}
  (render-todo-item todo))

(defnc ui-todo-list
  [{:keys [todos-list on-save on-delete on-change on-cancel on-edit on-toggle-complete]}]
  {:wrap [helix.core/memo]}
  (d/ul {:class "todo-list"}
    (map (fn [{::todo.model/keys [id] :as todo}]
           ($ todo-item {:& (assoc todo
                              :on-save on-save
                              :on-delete on-delete
                              :on-change on-change
                              :on-cancel on-cancel
                              :on-edit on-edit
                              :on-toggle-complete on-toggle-complete
                              :key (str id))}))
      todos-list)))
