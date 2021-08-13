(ns io.github.daveduthie.kaocha-run-changed
  (:require
   [clojure.pprint :as pprint]
   [clojure.tools.logging :as log]
   [clojure.tools.namespace.dir :as ctn.dir]
   [clojure.tools.namespace.track :as ctn.track]
   [kaocha.hierarchy :as hierarchy]
   [kaocha.plugin :as plugin :refer [defplugin]]
   [kaocha.testable :as testable]))

(def ^:private *tracker (atom (ctn.track/tracker)))

(defn- changed-namespaces!
  [dirs]
  (let [updated (ctn.dir/scan-dirs @*tracker dirs)]
    (reset! *tracker (dissoc updated ::ctn.track/load ::ctn.track/unload))
    (log/trace :updated (with-out-str (pprint/pprint updated)))
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
  (or (->> tests
           (map (juxt :kaocha/source-paths :kaocha/test-paths))
           flatten
           set)
      ["src" "test"]))

(comment
  (require '[aero.core :as aero])
  (collect-dirs (aero/read-config "tests.edn")))

(defn- filter-test-plan
  [{::keys [dirs], :as test-plan}]
  (let [changed (set (changed-namespaces! dirs))]
    #_(log/debug :testPlan (with-out-str (pprint/pprint test-plan)))
    (log/debug :scanningDirs dirs)
    (log/debug :changed changed)
    (filter-testable changed test-plan)))

;; -----------------------------------------------------------------------------
;; The plugin

(defplugin io.github.daveduthie/kaocha-run-changed
  "Only run changed namespaces"

  (config [config]
    (update config ::dirs #(or % (collect-dirs config))))

  (pre-run [test-plan]
    (filter-test-plan test-plan)))
