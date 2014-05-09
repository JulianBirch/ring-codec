(defproject net.colourcoding/ritorno "1.0.0"
  :description "Library for encoding and decoding data"
  :url "https://github.com/JulianBirch/ritorno"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [commons-codec "1.6"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [com.cemerick/clojurescript.test "0.3.0"]]
  :plugins [[codox "0.7.0"]
            [com.keminglabs/cljx "0.3.2"]
            [lein-cljsbuild "1.0.3"]
            [com.cemerick/clojurescript.test "0.3.0"]]
  :codox {:src-dir-uri "http://github.com/JulianBirch/ritorno"
          :src-linenum-anchor-prefix "L"}
  :hooks [cljx.hooks
          leiningen.cljsbuild]
  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]
  :profiles
  {:1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}}
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "src"
                   :rules :clj}

                  {:source-paths ["src/cljx"]
                   :output-path "target2/classes"
                   :rules :cljs}

                  {:source-paths ["test/cljx"]
                   :output-path "test"
                   :rules :clj}

                  {:source-paths ["test/cljx"]
                   :output-path "target2/test-classes"
                   :rules :cljs}]}
  :cljsbuild
  {:builds
   {:dev  {:source-paths ["target2/classes"]
           :compiler {:output-to "target/main.js"
                      :output-dir "target"
                      :source-map "target/main.js.map"
                      :optimizations :whitespace
                      :pretty-print true}}
    :test {:source-paths ["target2/classes" "target2/test-classes"]
           :incremental? true
           :compiler {:output-to "target-test/unit-test.js"
                      :output-dir "target-test"
                      :source-map "target-test/unit-test.js.map"
                      :optimizations :whitespace
                      :pretty-print true}}}
   :test-commands {"unit-tests"
                   ["phantomjs" :runner "target-test/unit-test.js"]}})
