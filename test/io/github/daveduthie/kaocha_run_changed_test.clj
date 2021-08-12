(ns io.github.daveduthie.kaocha-run-changed-test
  (:require [clojure.test :refer :all]
            [io.github.daveduthie.kaocha-run-changed :refer :all]))

(deftest a-test
  (testing "xyz"
    (is (= 1 1))))
