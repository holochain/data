(ns time.core
 (:require
  clj-time.core
  clj-time.format
  time.spec
  [clojure.spec.alpha :as spec]))

(defn ->format
 [fmt d]
 (clj-time.format/unparse
  (clj-time.format/formatters fmt)
  d))

(defn format->
 [fmt s]
 {:pre [(string? s)]
  :post [(spec/valid? :time.spec/datetime %)]}
 (clj-time.format/parse
  (clj-time.format/formatters fmt)
  s))
