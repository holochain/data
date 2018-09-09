(ns time.spec
 (:require
  [clojure.spec.alpha :as spec]))

(defn datetime?
 [maybe-datetime]
 (instance? org.joda.time.DateTime maybe-datetime))

(spec/def ::iso8601 string?)
(spec/def ::datetime datetime?)
