(ns travis.builds
 (:require
  travis.core
  travis.data)
 (:refer-clojure :exclude [find]))

; https://developer.travis-ci.com/resource/builds#builds
(def builds (partial travis.core/api! (travis.data/token) "builds"))

; https://developer.travis-ci.com/resource/builds#find
(defn find
 [user repo]
 (flatten
  (map
   :builds
   (travis.core/api!
    (travis.data/token)
    ["repo" (travis.core/github-user+repo->travis-slug user repo) "builds"]))))
