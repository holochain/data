(ns report.pr-cycle-time
 (:require
  [clojure.spec.alpha :as spec]
  time.core
  github.prs
  time.spec
  github.core))

(spec/def ::start :time.spec/datetime)
(spec/def ::end :time.spec/datetime)

(spec/def ::datum
 (spec/keys
  :req
  [::start
   ::end]))

(defn pr->datum
 [pr]
 {:pre [(spec/valid? :github.spec/pr pr)]
  :post [(spec/valid? ::datum %)]}
 {::start (-> pr :created_at github.core/iso8601->)
  ; fallback to now if the pr is still open
  ::end
  (or
   (some-> pr :closed_at github.core/iso8601->)
   (clj-time.core/now))})

(defn datum->cycle-time
 [datum]
 {:pre [(spec/valid? ::datum datum)]
  :post [(spec/valid? int? %)]}
 (clj-time.core/in-minutes
  (clj-time.core/interval
   (::start datum)
   (::end datum))))

(defn do-it!
 [user repo]
 (let [prs (github.prs/all-prs! user repo {:state "closed"})]
  (map
   (comp datum->cycle-time pr->datum)
   prs)))
