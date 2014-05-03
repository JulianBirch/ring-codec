(defproject ring/ring-codec "1.0.0"
  :description "Library for encoding and decoding data"
  :url "https://github.com/ring-clojure/ring-codec"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [commons-codec "1.6"]]
  :plugins [[codox "0.6.4"]
            [com.keminglabs/cljx "0.3.2"]
            [org.clojure/clojurescript "0.0-2156"]
            [lein-cljsbuild "1.0.1"]]
  :codox {:src-dir-uri "http://github.com/ring-clojure/ring-codec/blob/1.0.0"
          :src-linenum-anchor-prefix "L"}
  :hooks [cljx.hooks]
  :jar-exclusions [#"\.cljx|\.swp|\.swo|\.DS_Store"]
  :profiles
  {:1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
   :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}}
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}

                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}

                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :clj}

                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :cljs}]})
