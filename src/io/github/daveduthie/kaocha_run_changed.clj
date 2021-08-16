(ns io.github.daveduthie.kaocha-run-changed
  "This kaocha plugin uses clojure.tools.namespace to filter tests.

  It only re-runs tests which depend on files changed since the last run.

  An exception is when [[kaocha.watch]] limits tests to those which failed the
  last test run, in which case this filter does nothing."
  (:require
   [clojure.tools.logging :as log]
   [clojure.tools.namespace.dir :as ctn.dir]
   [clojure.tools.namespace.track :as ctn.track]
   [kaocha.hierarchy :as hierarchy]
   [kaocha.plugin :as plugin :refer [defplugin]]
   [kaocha.testable :as testable]
   [kaocha.result :as result]
   [kaocha.watch :as watch]))

(def ^:private *tracker (atom (ctn.track/tracker)))

(defn- changed-namespaces!
  [dirs]
  (log/trace :scanningDirs dirs)
  (let [updated (ctn.dir/scan-dirs @*tracker dirs)]
    (reset! *tracker (dissoc updated ::ctn.track/load ::ctn.track/unload))
    (log/trace :updated updated)
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

(defn- collect-dirs
  [{:keys [kaocha/tests]}]
  (->> tests
       (map (juxt :kaocha/source-paths :kaocha/test-paths))
       flatten
       set
       not-empty))

(comment
  (require '[aero.core :as aero])
  (collect-dirs (aero/read-config "example/tests.edn")))

(defn- failed-tests? [test-plan]
  (seq (::watch/focus test-plan)))

(defn- filter-test-plan
  [{::keys [dirs], :as test-plan}]
  #_(log/trace :testPlan (with-out-str (pprint/pprint test-plan)))
  (if (failed-tests? test-plan)
    test-plan
    (filter-testable (set (changed-namespaces! dirs)) test-plan)))

;; -----------------------------------------------------------------------------
;; The plugin

(defplugin io.github.daveduthie/kaocha-run-changed
  "Only run changed namespaces"

  (config [config]
    (update config ::dirs #(or % ; explicitly provided
                               (collect-dirs config) ; inferred from test suites
                               ["src" "test"]))) ; default

  (pre-run [test-plan]
    (filter-test-plan test-plan)))
