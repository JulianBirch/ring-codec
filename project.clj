(defproject net.colourcoding/ritorno "1.0.1"
  :description "Library for encoding and decoding data"
  :url "https://github.com/JulianBirch/ritorno"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [commons-codec "1.10"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [com.cemerick/clojurescript.test "0.3.3"
                  :scope "test"]]
  :plugins [[codox "0.7.0"]
            [lein-cljsbuild "1.0.6"]
            [com.cemerick/clojurescript.test "0.3.3"]]
  :codox {:src-dir-uri "http://github.com/JulianBirch/ritorno"
          :src-linenum-anchor-prefix "L"}
  :hooks [leiningen.cljsbuild]
  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]
  :cljsbuild
  {:builds
   {:dev  {:source-paths ["src"]
           :compiler {:output-to "target/main.js"
                      :output-dir "target"
                      :source-map "target/main.js.map"
                      :optimizations :whitespace
                      :pretty-print true}}
    :test {:source-paths ["src" "test"]
           :incremental? true
           :compiler {:output-to "target-test/unit-test.js"
                      :output-dir "target-test"
                      :source-map "target-test/unit-test.js.map"
                      :optimizations :advanced
                      :pretty-print true}}}
    :test-commands {"unit-tests"
                    ["xvfb-run" "-a" "slimerjs" :runner
                     "window.literal_js_was_evaluated=true"
                     "target-test/unit-test.js"]}})
