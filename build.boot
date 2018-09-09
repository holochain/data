(set-env!
 :source-paths #{"src"}
 :dependencies
 '[
   [org.clojure/clojure "1.10.0-alpha7"]
   [samestep/boot-refresh "0.1.0"]
   [http-kit "2.2.0"]
   [environ "1.1.0"]
   [com.taoensso/timbre "4.10.0"]
   [cheshire "5.8.0"]])

(require
 '[samestep.boot-refresh :refer [refresh]])

(deftask repl-server
 []
 (comp
  (watch)
  (refresh)
  (repl :server true)))

(deftask repl-client
 []
 (repl :client true))
