(ns space.matterandvoid.todomvc.client.ui.components.form-controller
  (:require
    [applied-science.js-interop :as j]
    [clojure.string :as str]
    [goog.object :as g]
    [helix.core :refer [$ <> defnc fnc]]
    [helix.dom :as d]
    ["react" :as react]
    ["react-dom/client" :as rdom]
    ["react-hook-form" :as react-form :refer [useForm]]
    ["react-router-dom" :as react-router]
    [taoensso.timbre :as log]))

(defnc form-controller []
  (let [form (useForm)]
    (comment (j/get form' :control))
    (def form' form)
    ($ (.-Controller react-form) {:name    "my input element"
                                  :control (j/get form :control)
                                  :render  (fn [args]
                                             (def args' args)
                                             (let [on-change (j/get-in args [:field :onChange])
                                                   value     (j/get-in args [:field :value])]
                                               (println "Render input component: " args)
                                               (d/input {
                                                         :value     (or value "")
                                                         :on-change (fn [e]
                                                                      (println "on change: " (-> e (.-target) (.-value)))
                                                                      (on-change (str/upper-case (-> e (.-target) (.-value)))))
                                                         :&         (doto (.-field args) (g/remove "onChange")
                                                                                         (g/remove "value"))})))})))
