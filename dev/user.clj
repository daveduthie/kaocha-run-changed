(ns user
  (:require [clojure.edn :as edn]
            [clojure.tools.deps.alpha.repl :refer [add-libs]]))

(defn load-all-deps []
  (let [depsEdn (edn/read-string (slurp "deps.edn"))
        allDeps (apply merge
                       (:deps depsEdn)
                       (map :extra-deps (vals (:aliases depsEdn))))]
    {:deps allDeps
     :result (add-libs allDeps)}))

(comment
  (load-all-deps)
  ,)
