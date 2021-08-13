(ns example.core
  (:require [clojure.tools.logging :as log]))

(defn f [i]
  (log/info :i i)
  (* i 2))
