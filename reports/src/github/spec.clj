(ns github.spec
 (:require
  [clojure.spec.alpha :as spec]
  time.spec))

(spec/def ::number pos-int?)
(spec/def ::title string?)

(spec/def ::created_at :time.spec/iso8601)
(spec/def ::closed_at (spec/nilable :time.spec/iso8601))

(spec/def ::additions nat-int?)
(spec/def ::deletions nat-int?)

(spec/def ::pr
 (spec/keys
  :req-un
  [::created_at
   ::closed_at
   ::number
   ::title]))

(spec/def ::pr--full
 (spec/merge
  ::pr
  (spec/keys
   :req-un
   [::additions
    ::deletions])))
