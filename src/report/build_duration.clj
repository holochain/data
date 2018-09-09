(ns report.build-duration
 (:require
  travis.spec
  time.spec
  [clojure.spec.alpha :as spec]))

(spec/def ::duration :travis.spec/duration)
(spec/def ::started :time.spec/datetime)

(spec/def ::datum
 (spec/keys
  :req [::duration
        ::started]))

(defn build->datum
 [build]
 {:pre [(spec/valid? :travis.spec/build build)]
  :post [(spec/valid? ::datum %)]}
 {::duration (:duration build)
  ::started (-> build :started_at travis.core/iso8601->)})

(defn builds->histogram!
 [builds]
 (incanter.core/view
  (incanter.charts/histogram
   (map
    (comp ::duration build->datum)
    builds))))

(defn builds->time-series!
 [builds]
 (let [start-times (map (comp clj-time.coerce/to-long ::started build->datum) builds)
       durations (map (comp ::duration build->datum) builds)]
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
 (let [builds (filtered-builds-pass (travis.builds/find (str user "/" repo)))]
  (taoensso.timbre/debug "user:" user)
  (taoensso.timbre/debug "repository:" repo)
  (builds->histogram! builds)
  (builds->time-series! builds)))
