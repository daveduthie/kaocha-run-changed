(ns example.not-core-test
  (:require [example.not-core :as sut]
            [clojure.test :as t :refer [deftest is]]))

(deftest g-test
  (is (= 1 (sut/g 2))))
