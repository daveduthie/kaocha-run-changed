(ns example.core-test
  (:require [example.core :as sut]
            [example.another-not-core :as anc]
            [clojure.test :as t :refer [deftest is]]))

(deftest one
  (is (= 2 (sut/f 1))))

(deftest two
  (is (= 6 (sut/f (anc/h 2)))))
