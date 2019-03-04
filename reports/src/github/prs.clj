(ns github.prs
  (:require
   github.core
   github.data))

(defn all-prs!
 ([user repo]
  (all-prs! user repo nil))
 ([user repo params]
  (github.core/api!
   (github.data/token)
   ["repos" user repo "pulls"]
   (merge
    {:base "develop"
     :per_page 100}
    params))))

(defn pr!
 [user repo n]
 (first
  (github.core/api!
   (github.data/token)
   ["repos" user repo "pulls" n])))

(defn with-labels
 "Merges in the labels for a given PR or collection of PRs"
 [user repo pr]
 (if (sequential? pr)
  (pmap #(with-labels user repo %) pr)
  (merge
   pr
   {:labels
    (github.core/api!
     (github.data/token)
     ["repos" user repo "issues" (:number pr) "labels"])})))

(defn with-head-state
 "Merges in the CI states for a given PR or collection of PRs"
 [user repo pr]
 (if (sequential? pr)
  (pmap #(with-head-state user repo %) pr)
  (merge
   pr
   {:head_state
    (github.core/api!
     (github.data/token)
     ["repos" user repo "commits" (-> pr :head :sha) "status"])})))
