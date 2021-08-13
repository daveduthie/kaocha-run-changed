(ns build
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]))

(defn git-describe-tag
  []
  (-> (b/process
       {:command-args ["git" "describe" "--tags" "--always" "--dirty=-dirty"],
        :out :capture})
      :out str/trim))

(def lib 'io.github.daveduthie/kaocha-run-changed)
(def version (git-describe-tag))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def src-dirs ["src"])
(def copy-srcs src-dirs)

(defn clean [params]
  (b/delete {:path "target"})
  params)

(defn jar [params]
  (b/write-pom {:class-dir class-dir
                :lib       lib
                :version   version
                :basis     basis
                :src-dirs  src-dirs})
  (b/copy-dir {:src-dirs   copy-srcs
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file  jar-file})
  params)

(defn write-pom [params]
  (b/write-pom {:basis     basis
                :class-dir class-dir
                :lib       lib
                :version   version})
  (b/copy-file {:src    (format "%s/META-INF/maven/%s/pom.xml" class-dir lib)
                :target "pom.xml"})
  params)

(defn install [params]
  (b/install {:basis     basis
              :lib       lib
              :version   version
              :jar-file  jar-file
              :class-dir class-dir})
  params)

(comment
  (-> (clean {})
      jar
      write-pom
      install))
