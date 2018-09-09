(ns github.data
 (:require
  api.core))

(def base-url "https://api.github.com/")

(defn token [] (api.core/token :github-token "Github"))
