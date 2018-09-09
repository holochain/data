(ns github.spec
 (:require
  [clojure.spec.alpha :as spec]
  time.spec))

(spec/def ::created_at :time.spec/iso8601)
(spec/def ::closed_at (spec/nilable :time.spec/iso8601))

(spec/def ::pr
 (spec/keys
  :req-un
  [::created_at
   ::closed_at]))
