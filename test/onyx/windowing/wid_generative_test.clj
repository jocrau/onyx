(ns onyx.windowing.wid-generative-test
  (:require [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test :refer [deftest is]]
            [com.gfredericks.test.chuck :refer [times]]
            [com.gfredericks.test.chuck.clojure-test :refer [checking]]
            [onyx.windowing.window-id :refer [wids extents]]
            [onyx.api]))

(deftest fixed-windows
  (checking
   "one segment per fixed window"
   (times 500)
   [w-range-and-slide gen/s-pos-int
    value (gen/one-of [gen/pos-int
                       (gen/double* {:min       0
                                     :max       Long/MAX_VALUE
                                     :infinite? false
                                     :NaN?      false})])]
   (let [buckets (wids 0 w-range-and-slide w-range-and-slide :window-key {:window-key value})]
     (is (= 1 (count buckets))))))

(deftest sliding-windows
  (checking
   "a segment in a multiple sliding windows"
   (times 500)
   [w-slide gen/s-pos-int
    multiple gen/s-pos-int
    value (gen/one-of [gen/pos-int
                       (gen/double* {:min       0
                                     :max       Long/MAX_VALUE
                                     :infinite? false
                                     :NaN?      false})])]
   (let [w-range (* multiple w-slide)
         buckets (wids 0 w-range w-slide :window-key {:window-key value})]
     (is (= multiple (count buckets))))))

(deftest inverse-functions
  (checking
    "values produced by extents are matched by wids"
    (times 500)
    ;; Bound the window size to 10 to keep the each test iteration quick.
    [w-slide (gen/resize 10 gen/s-pos-int)
     multiple gen/s-pos-int
     w-id gen/pos-int]
    (let [w-range (* multiple w-slide)
          values (extents 0 w-range w-slide w-id)]
      (is (every?
            (fn [value]
              (some #{w-id}
                    (map long (wids 0 w-range w-slide :window-key {:window-key value}))))
                 values)))))
