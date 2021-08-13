(ns user
  (:require [kaocha.repl :as kr]
            [clojure.edn :as edn]))

(comment

  (require '[clojure.tools.deps.alpha.repl :refer [add-libs]])
  (add-libs (:deps (edn/read-string (slurp "deps.edn"))))

  (ns-interns (the-ns 'kaocha.repl))
  (kr/run)

  ,)
