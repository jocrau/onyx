(ns onyx.windowing.wid-generative-test
  (:require [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test :refer [deftest is]]
            [com.gfredericks.test.chuck :refer [times]]
            [com.gfredericks.test.chuck.clojure-test :refer [checking]]
            [onyx.windowing.window-id :refer [wids extent extent-lower-bound extent-upper-bound]]
            [onyx.api]))

(deftest fixed-windows
  (checking
    "one segment per fixed window"
    (times 500)
    [w-range-and-slide (gen/one-of [gen/s-pos-int
                                    (gen/double* {:min       1
                                                  :max       1000
                                                  :infinite? false
                                                  :NaN?      false})])
    value (gen/one-of [gen/pos-int
                       (gen/double* {:min       0
                                     :max       Long/MAX_VALUE
                                     :infinite? false
                                     :NaN?      false})])]
    (let [buckets (wids 0 w-range-and-slide w-range-and-slide :window-key {:window-key value})]
      (is (= 1 (count buckets))))))

(deftest inverse-functions
  (checking
    "boundaries of the extent are matched by wids"
    (times 500)
    ;; Bound the window size to 10 to keep the each test iteration quick.
    [w-slide (gen/one-of [(gen/choose 1 10)
                          (gen/double* {:min       1
                                        :max       10
                                        :infinite? false
                                        :NaN?      false})])
     w-range (gen/one-of [(gen/choose (inc w-slide) 100)
                          (gen/double* {:min       (inc w-slide)
                                        :max       100
                                        :infinite? false
                                        :NaN?      false})])
     w-id gen/pos-int]
    (let [lower-bound (extent-lower-bound 0 w-range w-slide w-id)
          upper-bound (extent-upper-bound 0 w-slide w-id)]
      (is (some #{w-id} (wids 0 w-range w-slide :window-key {:window-key lower-bound})))
      (is (not (some #{w-id} (wids 0 w-range w-slide :window-key {:window-key upper-bound})))))))
