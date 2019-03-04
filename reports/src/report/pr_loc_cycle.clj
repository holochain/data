(ns report.pr-loc-cycle
 (:require
  report.pr-cycle-time
  report.pr-loc
  incanter.charts
  incanter.core))

(defn view-scatter!
 [xs ys]
 (incanter.core/view
  (incanter.charts/scatter-plot
   xs ys)))

(defn top-n-by
 [n by xs]
 (take 10 (reverse (sort-by by xs))))

(def top-10-by (partial top-n-by 10))

(defn do-it!
 ([user repo]
  (do-it! user repo {:state "closed" :base "develop"}))
 ([user repo params]
  (let [prs (github.prs/all-prs! user repo params)
        pr! (partial github.prs/pr! user repo)
        full-prs (pmap pr! (map :number prs))
        loc:datums (map report.pr-loc/full-pr->datum full-prs)
        loc:totals (map ::total loc:datums)
        loc:diffs (map ::diff loc:datums)
        cycle:datums (map report.pr-cycle-time/pr->datum full-prs)
        cycle:cycle-times (map :report.pr-cycle-time/cycle-time cycle:datums)]
   (taoensso.timbre/debug "user:" user)
   (taoensso.timbre/debug "repository:" repo)
   (taoensso.timbre/debug "params:" params)
   (doseq [vs [loc:totals loc:diffs]]
    (view-scatter! cycle:cycle-times vs))
   (doseq [[by xs] [[:report.pr-cycle-time/cycle-time
                     (map
                      #(dissoc % :report.pr-cycle-time/close :report.pr-cycle-time/open)
                      cycle:datums)]
                    [:report.pr-loc/total
                     (map
                      #(dissoc % :report.pr-loc/close :report.pr-loc/additions :report.pr-loc/deletions)
                      loc:datums)]]]
    (taoensso.timbre/debug "Top 10 PRs" by)
    (clojure.pprint/print-table
     (top-10-by by xs))))))
