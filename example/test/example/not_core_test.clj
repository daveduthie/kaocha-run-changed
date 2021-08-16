(ns example.not-core-test
  (:require [example.not-core :as sut]
            [clojure.test :as t :refer [deftest is]]))

(deftest one
  (is (= 1 (sut/g 2))))

(deftest two
  (is (= 2 (sut/g 4))))
