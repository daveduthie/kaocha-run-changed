(ns io.github.daveduthie.kaocha-run-changed
  (:require
   [clojure.tools.namespace.dir :as ctn.dir]
   [clojure.tools.namespace.track :as ctn.track]
   [kaocha.plugin :as plugin :refer [defplugin]]
   [kaocha.hierarchy :as hierarchy]
   [kaocha.testable :as testable]))

(def ^:private *tracker (atom (ctn.track/tracker)))

(defn- changed-namespaces! [dirs]
  (let [updated (ctn.dir/scan-dirs @*tracker dirs)]
    (reset! *tracker {::ctn.dir/time (System/currentTimeMillis)})
    (::ctn.track/load updated)))

(defn- filter-testable
  [changed-namespaces testable]
  (let [recurse (fn recurse []
                  (update testable
                          :kaocha.test-plan/tests
                          (partial map #(filter-testable changed-namespaces %))))]
    (cond (hierarchy/group? testable) ; a set of tests in one ns
          (if (contains? changed-namespaces (:kaocha.ns/name testable))
            testable
            (assoc testable ::testable/skip true))
          (:kaocha.test-plan/tests testable) (recurse)
          :else testable)))

(defn filter-test-plan
  [{::keys [dirs], :as test-plan}]
  (let [changed (set (changed-namespaces! dirs))]
    (filter-testable changed test-plan)))

;; -----------------------------------------------------------------------------
;; The plugin

(defplugin io.github.daveduthie/kaocha-run-changed
  "Only run changed namespaces"

  (pre-run [test-plan]
    (filter-test-plan test-plan)))
