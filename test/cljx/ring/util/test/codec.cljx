(ns ring.util.test.codec
  #+clj (:use [clojure.test]
              [ring.util.codec])
  #+clj (:import java.util.Arrays)
  (:require #+clj [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :as t]
            [clojure.string :as str]
            [ring.util.codec
             :refer [percent-encode #+clj percent-decode url-encode
                     url-decode base64-encode base64-decode
                     form-encode form-decode form-decode-str]])
  #+cljs (:require-macros [cemerick.cljs.test :refer
                           (is are deftest with-test
                               run-tests testing)]))

(defn to-upper [^String x]
  #+clj (.toUpperCase x)
  #+cljs (.toUpperCase x))

(defn =%-reduce [[result %count] [x y]]
  (let [new-count (if (= x "%")
                    2
                    (max 0 (dec %count)))
        char= (if (pos? %count)
                (= (to-upper x) (to-upper y))
                (= x y))]
    [(and result char=) new-count]))

(defn to-strings [x y]
  [(str x) (str y)])

(defn =% [x y]
  (and (= (count x) (count y))
       (->> (map to-strings x y)
            (reduce =%-reduce [true 0])
            first)))

(defn =url [x y]
  (=% (str/replace x "+" "%2b") (str/replace y "+" "%2b")))

(deftest test-percent-encode
  (is (=% (percent-encode " ") "%20"))
  (is (=% (percent-encode "+") "%2B"))
  (is (=% (percent-encode "foo") "%66%6F%6F")))

#+clj (deftest test-percent-decode
  (is (=% (percent-decode "%20") " "))
  (is (=% (percent-decode "foo%20bar") "foo bar"))
  (is (=% (percent-decode "foo%FE%FF%00%2Fbar" "UTF-16") "foo/bar"))
  (is (=% (percent-decode "%24") "$")))

(deftest test-url-encode
  (is (=% (url-encode "foo/bar") "foo%2Fbar"))
  #+clj (is (=% (url-encode "foo/bar" "UTF-16") "foo%FE%FF%00%2Fbar"))
  (is (=% (url-encode "foo+bar") "foo+bar"))
  (is (=% (url-encode "foo bar") "foo%20bar")))

(deftest test-url-decode
  (is (=% (url-decode "foo%2Fbar") "foo/bar" ))
  #+clj (is (=% (url-decode "foo%FE%FF%00%2Fbar" "UTF-16") "foo/bar"))
  #+clj (is (=% (url-decode "%") "%")))

#+clj (deftest test-base64-encoding
  (let [str-bytes (.getBytes "foo?/+" "UTF-8")]
    (is (Arrays/equals str-bytes (base64-decode (base64-encode str-bytes))))))

(deftest test-form-encode
  (testing "strings"
    (are [x y] (=url (form-encode x) y)
      "foo bar" "foo+bar"
      "foo+bar" "foo%2Bbar"
      "foo/bar" "foo%2Fbar")
    #+clj (is (=% (form-encode "foo/bar" "UTF-16") "foo%FE%FF%00%2Fbar")))
  (testing "maps"
    (are [x y] (= (form-encode x) y)
      {"a" "b"} "a=b"
      {:a "b"}  "a=b"
      {"a" 1}   "a=1"
      {"a" "b" "c" "d"} "a=b&c=d"
      {"a" "b c"}       "a=b+c")
    #+clj (is (= (form-encode {"a" "foo/bar"} "UTF-16") "a=foo%FE%FF%00%2Fbar"))))

(deftest test-form-decode-str
  (is (= (form-decode-str "foo=bar+baz") "foo=bar baz"))
  (is (nil? (form-decode-str "%D"))))

(deftest test-form-decode
  (are [x y] (= (form-decode x) y)
       "foo"     "foo"
       "a=b"     {"a" "b"}
       "a=b&c=d" {"a" "b" "c" "d"}
       "foo+bar" "foo bar"
       "a=b+c"   {"a" "b c"}
       "a=b%2Fc" {"a" "b/c"})
  #+clj (is (= (form-decode "a=foo%FE%FF%00%2Fbar" "UTF-16")
               {"a" "foo/bar"})))
