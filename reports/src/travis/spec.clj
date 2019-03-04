(ns travis.spec
 (:require
  [clojure.spec.alpha :as spec]))

(spec/def ::duration nat-int?)

(spec/def ::build
 (spec/keys
  :req-un
  [::duration]))
