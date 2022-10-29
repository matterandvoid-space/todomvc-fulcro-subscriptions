(ns taoensso.timbre-noop
  #?(:cljs (:require-macros [taoensso.timbre-noop])))

;;;; Config

(def default-timestamp-opts)

(declare stacktrace)
(defn default-output-fn
  ([data])
  ([opts data]))

;;; Alias core appenders here for user convenience
(declare default-err default-out)
#?(:cljs (def println-appender))
#?(:cljs (def console-appender))

(def default-config)

(def ^:dynamic *config* nil)

(defmacro with-config [config & body])
(defmacro with-merged-config [config & body])

(declare swap-config!)
(defn set-config! [m])
(defn merge-config! [m])
(defn swap-config! [f & args])

(defn set-level! [level])
(defmacro with-level [level & body])

(defn- valid-level? [x])
(defn- valid-level [x])
(defn- valid-level->int [x])

(defn #?(:clj level>= :cljs ^:boolean level>=) [x y])

(defn- #?(:clj may-log-ns? :cljs ^boolean may-log-ns?) [ns-filter ns])

(def ^:private ns->?min-level nil)

;;;; Combo filtering

(defn- get-min-level [default x ns])

(defn- legacy-ns-filter [ns-whitelist ns-blacklist])

(defn #?(:clj may-log? :cljs ^:boolean may-log?)
  ([level])
  ([level ?ns-str])
  ([level ?ns-str ?config])
  ([default-min-level level ?ns-str ?config]))

;;;; Compile-time filtering

#?(:clj (def ^:private compile-time-min-level))
#?(:clj (def ^:private compile-time-ns-filter))

#?(:clj
   (defn -elide? [level-form ns-str-form]))

;;;; Utils

(defn- str-join [xs])

#?(:clj
   (defonce ^:private get-agent nil))

(defonce ^:private get-rate-limiter nil)

;;;; Internal logging core

(def ^:dynamic *context* "General-purpose dynamic logging context" nil)
(defmacro with-context [context & body])

(defmacro with-context+ [context & body])
(defn- parse-vargs [?err msg-type vargs])
(defn- get-timestamp [timestamp-opts instant])

(defn -log! "Core low-level log fn. Implementation detail!"
  ;; Backward-compatible arities for convenience of AOT tools, Ref.
  ;; https://github.com/fzakaria/slf4j-timbre/issues/20
  ([config level ?ns-str ?file ?line msg-type ?err vargs_ ?base-data])
  ([config level ?ns-str ?file ?line msg-type ?err vargs_ ?base-data callsite-id])
  ([config level ?ns-str ?file ?line msg-type ?err vargs_ ?base-data callsite-id spying?]))

(defn- fline [and-form])

(defmacro log! [level msg-type args & [opts]])

;;;; Main public API-level stuff
;;; Log using print-style args
(defmacro log* [config level & args] `(log! ~level :p ~args ~{:?line (fline &form) :config config}))
(defmacro log [level & args] `(log! ~level :p ~args ~{:?line (fline &form)}))
(defmacro trace [& args] `(log! :trace :p ~args ~{:?line (fline &form)}))
(defmacro debug [& args] `(log! :debug :p ~args ~{:?line (fline &form)}))
(defmacro info [& args] `(log! :info :p ~args ~{:?line (fline &form)}))
(defmacro warn [& args] `(log! :warn :p ~args ~{:?line (fline &form)}))
(defmacro error [& args] `(log! :error :p ~args ~{:?line (fline &form)}))
(defmacro fatal [& args] `(log! :fatal :p ~args ~{:?line (fline &form)}))
(defmacro report [& args] `(log! :report :p ~args ~{:?line (fline &form)}))

;;; Log using format-style args
(defmacro logf* [config level & args] `(log! ~level :f ~args ~{:?line (fline &form) :config config}))
(defmacro logf [level & args] `(log! ~level :f ~args ~{:?line (fline &form)}))
(defmacro tracef [& args] `(log! :trace :f ~args ~{:?line (fline &form)}))
(defmacro debugf [& args] `(log! :debug :f ~args ~{:?line (fline &form)}))
(defmacro infof [& args] `(log! :info :f ~args ~{:?line (fline &form)}))
(defmacro warnf [& args] `(log! :warn :f ~args ~{:?line (fline &form)}))
(defmacro errorf [& args] `(log! :error :f ~args ~{:?line (fline &form)}))
(defmacro fatalf [& args] `(log! :fatal :f ~args ~{:?line (fline &form)}))
(defmacro reportf [& args] `(log! :report :f ~args ~{:?line (fline &form)}))

(defmacro -log-errors [?line & body])

(defmacro -log-and-rethrow-errors [?line & body])

(defmacro -logged-future [?line & body] `(future (-log-errors ~?line ~@body)))

(defmacro log-errors [& body] `(-log-errors ~(fline &form) ~@body))
(defmacro log-and-rethrow-errors [& body] `(-log-and-rethrow-errors ~(fline &form) ~@body))
(defmacro logged-future [& body] `(-logged-future ~(fline &form) ~@body))

#?(:clj (defn handle-uncaught-jvm-exceptions! [& [handler]]))

(defmacro -spy [?line config level name expr])

(defmacro spy
  "Evaluates named expression and logs its result. Always returns the result.
  Defaults to :debug logging level and unevaluated expression as name."
  ([expr])
  ([level expr])
  ([level name expr])
  ([config level name expr]))

(defmacro get-env [])

#?(:clj
   (defn color-str [color & xs]))

#?(:clj (def default-out))
#?(:clj (def default-err))
(defmacro with-default-outs [& body]
  `(binding [*out* default-out, *err* default-err] ~@body))

#?(:clj (defn get-?hostname "Returns live local hostname, or nil." []))

#?(:clj (def get-hostname "Returns cached hostname string."))

#?(:clj (def ^:private default-stacktrace-fonts))

(defn stacktrace ([err]) ([err opts]))



