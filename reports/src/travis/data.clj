(ns travis.data
 (:require
  api.core))

(def base-url "https://api.travis-ci.org/")

(defn token [] (api.core/token :travis-token "Travis"))
