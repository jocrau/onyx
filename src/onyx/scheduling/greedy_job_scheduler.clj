(ns onyx.scheduling.greedy-job-scheduler
  (:require [onyx.scheduling.common-job-scheduler :as cjs]
            [onyx.scheduling.common-task-scheduler :as cts]
            [onyx.log.commands.common :as common]))

(defn job-coverable? [replica job]
  (let [min-req (apply + (vals (get-in replica [:min-required-peers job])))]
    (>= (count (get-in replica [:peers])) min-req)))

(defmethod cjs/job-offer-n-peers :onyx.job-scheduler/greedy
  [replica]
  (if (seq (:jobs replica))
    (let [[active & passive] (:jobs replica)]
      (merge {active (count (:peers replica))} (zipmap passive (repeat 0))))
    {}))

(defmethod cjs/claim-spare-peers :onyx.job-scheduler/greedy
  [replica jobs n]
  ;; This is a trivial case. A Greedy job scheduler has already offered
  ;; all the peers to the first available job. If there are extra peers,
  ;; they would simply be offered back to the same job, which would refuse
  ;; them. Return the same job claims since nothing will change.
  ;;
  jobs)

(defmethod cjs/sort-job-priority :onyx.job-scheduler/greedy
  [replica jobs]
  ;; We only care about the first job in a Greedy scheduler.
  (if-let [x (first jobs)]
    (vector x)
    []))
