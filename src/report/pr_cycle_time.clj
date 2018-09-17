(ns report.pr-cycle-time
 (:require
  [clojure.spec.alpha :as spec]
  time.core
  github.prs
  time.spec
  github.core
  incanter.charts
  incanter.core
  clj-time.coerce))

(spec/def ::open :time.spec/datetime)
(spec/def ::close :time.spec/datetime)
(spec/def ::cycle-time int?)
(spec/def ::number :github.spec/number)
(spec/def ::title :github.spec/title)

(spec/def ::datum
 (spec/keys
  :req
  [::open
   ::close
   ::cycle-time
   ::number
   ::title]))

(defn calculate-cycle-time
 [datum]
 (clj-time.core/in-hours
  (clj-time.core/interval
   (::open datum)
   (::close datum))))

(defn with-cycle-time
 [datum]
 (merge
  datum
  {::cycle-time (calculate-cycle-time datum)}))

(defn pr->datum
 [pr]
 {:pre [(spec/valid? :github.spec/pr pr)]
  :post [(spec/valid? ::datum %)]}
 (->
  {::open (-> pr :created_at github.core/iso8601->)
   ; fallback to now if the pr is still open
   ::close
   (or
    (some-> pr :closed_at github.core/iso8601->)
    (clj-time.core/now))
   ::number (:number pr)
   ::title (:title pr)}
  with-cycle-time))

(defn prs->histogram!
 [prs]
 (incanter.core/view
  (incanter.charts/histogram
   (map
    (comp ::cycle-time pr->datum)
    prs))))

(defn prs->time-series!
 [prs]
 (let [open-times (map (comp clj-time.coerce/to-long ::open pr->datum) prs)
       cycle-times (map (comp ::cycle-time pr->datum) prs)]
  (incanter.core/view
   (incanter.charts/time-series-plot
    open-times
    cycle-times))))

(defn do-it!
 "shows histogram and time series for cycle times for closed PRs"
 ([user repo]
  (do-it! user repo {:state "closed" :base "develop"}))
 ([user repo params]
  (let [prs (github.prs/all-prs! user repo params)]
   (taoensso.timbre/debug "user:" user)
   (taoensso.timbre/debug "repository:" repo)
   (taoensso.timbre/debug "params:" params)
   (taoensso.timbre/debug "pull request count:" (count prs))
   (prs->histogram! prs)
   (prs->time-series! prs))))
