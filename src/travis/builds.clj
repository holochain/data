(ns travis.builds
 (:require
  travis.core
  travis.data
  cemerick.url))

; https://developer.travis-ci.com/resource/builds#builds
(def builds (partial travis.core/api! (travis.data/token) "builds"))

; https://developer.travis-ci.com/resource/builds#find
(defn find
 [repo]
 (:builds
  (travis.core/api!
   (travis.data/token)
   ["repo" (cemerick.url/url-encode repo) "builds"])))
