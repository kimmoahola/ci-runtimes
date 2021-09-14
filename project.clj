(defproject ci-runtimes "0.1.0"
  :description "List Github actions runtimes"
  :url "https://github.com/kimmoahola/ci-runtimes"
  :license {:name "MIT"
            :url "https://mit-license.org/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-http "3.12.1"]
                 [cheshire "5.10.0"]]
  :main ^:skip-aot ci-runtimes.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
