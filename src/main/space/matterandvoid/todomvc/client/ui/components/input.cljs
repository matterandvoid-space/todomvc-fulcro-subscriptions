(ns space.matterandvoid.todomvc.client.ui.components.input
  (:require
    [applied-science.js-interop :as j]
    [helix.core :refer [fnc]]
    [helix.dom :as d]
    ["react" :as react]
    ["react-hook-form" :as react-form :refer [useForm]]))

(def ui-input
  (react/forwardRef
    (fnc ui-input
      [{:keys [value on-submit on-change name on-key-down on-blur on-cancel] :as args
        :or   {on-key-down identity, on-blur identity, on-cancel identity}} input-ref]
      (assert name "Must pass a name to ui-input")
      (let [{:keys [register resetField setValue] :as form-opts} (j/lookup (useForm))
            args' (dissoc args :value :on-submit :on-change :name)
            {:keys [name onChange onBlur ref]} (j/lookup (register name))]
        (d/input {:& (merge {:type         "text"
                             :defaultValue value
                             :onBlur       (fn [e] (onBlur e) (on-blur e))
                             :name         name
                             :ref          (or input-ref ref)
                             :onKeyDown    (fn [e]
                                             (when (= "Enter" (j/get e :key))
                                               (resetField name)
                                               (on-submit))
                                             (when (= "Escape" (j/get e :key))
                                               (on-cancel))
                                             (on-key-down e form-opts))
                             :onChange     (fn [e]
                                             (when on-change (on-change (.. e -target -value)))
                                             (onChange e))}
                       args')})))))
