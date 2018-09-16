(ns report.pr-loc
 (:require
  [clojure.spec.alpha :as spec]
  github.spec
  incanter.charts
  incanter.core))

(spec/def ::additions :github.spec/additions)
(spec/def ::deletions :github.spec/deletions)
(spec/def ::close :time.spec/datetime)

(spec/def ::datum
 (spec/keys
  :req
  [::additions
   ::deletions]))

(defn full-pr->datum
 [full-pr]
 {:pre [(spec/valid? :github.spec/pr--full full-pr)]
  :post [(or (spec/valid? ::datum %)
          (spec/explain ::datum %))]}
 {::additions (:additions full-pr)
  ::deletions (:deletions full-pr)
  ::close
  (or
   (some-> full-pr :closed_at github.core/iso8601->)
   (clj-time.core/now))})

(defn datum->loc-total
 [datum]
 (+ (::additions datum) (::deletions datum)))

(defn datum->loc-diff
 [datum]
 ; My point today is that, if we wish to count lines of code, we should not
 ; regard them as “lines produced” but as “lines spent”: the current
 ; conventional wisdom is so foolish as to book that count on the wrong side of
 ; the ledger.
 ; - Edsger W. Dijkstra
 (- (::additions datum) (::deletions datum)))

(defn view-histogram!
 [vs]
 (incanter.core/view
  (incanter.charts/histogram
   vs)))

(defn datums->view-time-series!
 [datums]
 {:pre [(spec/valid? (spec/coll-of ::datum) datums)]}
 (doseq [f [datum->loc-total datum->loc-diff]]
  (incanter.core/view
   (incanter.charts/time-series-plot
    (map (comp clj-time.coerce/to-long ::close) datums)
    (map f datums)))))

(defn do-it!
 ([user repo]
  (do-it! user repo {:state "closed" :base "develop"}))
 ([user repo params]
  (let [prs (github.prs/all-prs! user repo params)
        pr! (partial github.prs/pr! user repo)
        full-prs (pmap pr! (map :number prs))
        datums (map full-pr->datum full-prs)
        totals (map datum->loc-total datums)
        diffs (map datum->loc-diff datums)]
   (taoensso.timbre/debug "user:" user)
   (taoensso.timbre/debug "repository:" repo)
   (taoensso.timbre/debug "params:" params)
   (doall (map view-histogram! [totals diffs]))
   (datums->view-time-series! datums))))
