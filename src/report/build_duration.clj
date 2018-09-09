(ns report.build-duration
 (:require
  travis.spec
  time.spec
  travis.builds
  [clojure.spec.alpha :as spec]
  incanter.core
  incanter.charts))

(spec/def ::start :time.spec/datetime)
(spec/def ::end :time.spec/datetime)

(spec/def ::datum
 (spec/keys
  :req
  [
   ::start
   ::end]))

(defn build->datum
 [build]
 {:pre [(spec/valid? :travis.spec/build build)]
  :post [(spec/valid? ::datum %)]}
 {
  ::start (-> build :started_at travis.core/iso8601->)
  ::end (-> build :finished_at travis.core/iso8601->)})

(defn datum->duration
 [datum]
 {:pre [(spec/valid? ::datum datum)]
  :post [(spec/valid? int? %)]}
 ; can't use `duration` from the build object provided by Travis as this gives
 ; wall time rather than build time
 (clj-time.core/in-minutes
  (clj-time.core/interval
   (::start datum)
   (::end datum))))

(defn builds->histogram!
 [builds]
 (incanter.core/view
  (incanter.charts/histogram
   (map
    (comp datum->duration build->datum)
    builds))))

(defn builds->time-series!
 [builds]
 (let [start-times (map (comp clj-time.coerce/to-long ::start build->datum) builds)
       durations (map (comp datum->duration build->datum) builds)]
  (incanter.core/view
   (incanter.charts/time-series-plot
    start-times
    durations))))

(defn filtered-builds-pass
 [builds]
 (filter
  (comp #{"passed"} :state)
  builds))

(defn do-it!
 "shows histogram and time series for durations for CI builds"
 [user repo]
 (let [builds (filtered-builds-pass (travis.builds/find user repo))]
  (taoensso.timbre/debug "user:" user)
  (taoensso.timbre/debug "repository:" repo)
  (builds->histogram! builds)
  (builds->time-series! builds)))
