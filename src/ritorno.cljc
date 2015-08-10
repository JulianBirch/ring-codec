(ns ritorno
  "Functions for encoding and decoding data."
  #?@(:clj [(:require [clojure.string :as str])
            (:import java.io.File
                     java.util.Map
                     [java.net URLEncoder URLDecoder]
                     org.apache.commons.codec.binary.Base64)]
      :cljs [(:require [clojure.string :as str]
                       [goog.crypt]
                       [goog.crypt.base64]
                       [goog.string])]))

(defn assoc-conj
  "Associate a key with a value in a map. If the key already
   exists in the map, a vector of values is associated with the key."
  [map key val]
  (assoc map key
    (if-let [cur (get map key)]
      (if (vector? cur)
        (conj cur val)
        [cur val])
      val)))

(defn- double-escape [^String x]
  (-> x
      (str/replace "\\" "\\\\")
      (str/replace "$" "\\$")))

; http://monsur.hossa.in/2012/07/20/utf-8-in-javascript.html
(defn utf-encode [^String unencoded & [^String encoding]]
  #? (:clj (.getBytes unencoded (or encoding "UTF-8"))
      :cljs (let [encoded (-> unencoded
                              js/escape
                              js/decodeURIComponent)]
                   (->> (range (count encoded))
                        (map #(.charCodeAt encoded %))))))

(defn percent-encode-byte [b]
  #? (:clj (format "%%%02X" b)
      :cljs (str "%" (.toString b 16))))

#? (:clj (do
           (def ^:private string-replace-bug?
             (= "x" (str/replace "x" #"." (fn [x] "$0"))))

           (defmacro fix-string-replace-bug [x]
             (if string-replace-bug?
               `(double-escape ~x)
               x))

           (defn- parse-bytes [encoded-bytes]
             (->> (re-seq #"%.." encoded-bytes)
                  (map #(subs % 1))
                  (map #(.byteValue (Integer/valueOf % 16)))
                  (byte-array)))

           (defn percent-decode
             "Decode every percent-encoded character in the given string
     using the specified encoding, or UTF-8 by default."
             [^String encoded & [^String encoding]]
             (str/replace encoded
                          #"(?:%..)+"
                          (fn [chars]
                            (-> ^bytes (parse-bytes chars)
                                (String. (or encoding "UTF-8"))
                                (fix-string-replace-bug)))))))

(defn percent-encode
  "Percent-encode every character in the given string using either
   the specified encoding, or UTF-8 by default."
  [^String unencoded & [^String encoding]]
  (->> (utf-encode unencoded encoding)
       (map percent-encode-byte)
       (str/join)))

(defn url-encode
  "Returns the url-encoded version of the given string, using
   either a specified encoding or UTF-8 by default."
  [unencoded & [encoding]]
  (str/replace unencoded
               #"[^A-Za-z0-9_~.+-]+"
               #(double-escape (percent-encode % encoding))))

(defn ^String url-decode
  "Returns the url-decoded version of the given string, using
   either a specified encoding or UTF-8 by default. If the encoding
   is invalid, nil is returned."
  [encoded & [encoding]]
  #? (:clj (percent-decode encoded encoding)
      :cljs (js/decodeURIComponent encoded)))

(defn base64-encode
  "Encode an array of bytes into a base64 encoded string."
  [unencoded]
  #? (:clj (String. (Base64/encodeBase64 unencoded))
      :cljs (goog.crypt.base64/encodeString unencoded)))

(defn base64-decode
  "Decode a base64 encoded string into an array of bytes."
  [^String encoded]
  #? (:clj (Base64/decodeBase64 (.getBytes encoded))
      :cljs (goog.crypt.base64/decodeString encoded)))

(defprotocol FormEncodeable
  (form-encode* [x encoding]))

(defn form-encode-str [^String unencoded ^String encoding]
  #?(:clj (URLEncoder/encode unencoded encoding)
     :cljs (-> unencoded
               js/encodeURIComponent
               (str/replace "%20" "+"))))

(declare form-encode)

(defn form-encode-map [params ^String encoding]
  (letfn [(encode [x] (form-encode x encoding))
          (encode-param [[k v]] (str (encode (name k)) "=" (encode v)))]
      (->> params
           (mapcat
            (fn [[k v]]
              (if (or (seq? v) (sequential? v) )
                (map #(encode-param [k %]) v)
                [(encode-param [k v])])))
           (str/join "&"))))

(defn form-encode
  "Encode the supplied value into www-form-urlencoded format, often
   used in URL query strings and POST request bodies, using the
   specified encoding.  If the encoding is not specified, it
   defaults to UTF-8"
  [x & [encoding]]
  (let [encoding (or encoding "UTF-8")]
    (cond (string? x) (form-encode-str x encoding)
          (map? x) (form-encode-map x encoding)
          :else (form-encode-str (str x) encoding))))

(defn form-decode-str
  "Decode the supplied www-form-urlencoded string using the
   specified encoding, or UTF-8 by default."
  [^String encoded & [encoding]]
  (try
    #? (:clj (URLDecoder/decode encoded (or encoding "UTF-8"))
        :cljs (-> encoded
                  (url-decode (or encoding "UTF-8"))
                  (str/replace "+" " ")))
    (catch #?(:clj Exception :cljs js/Object) _ nil)))

(defn form-decode
  "Decode the supplied www-form-urlencoded string using the
   specified encoding, or UTF-8 by default. If the encoded value is
   a string, a string is returned.  If the encoded value is a map
   of parameters, a map is returned."
  [^String encoded & [encoding]]
  (if-not
      #? (:clj (.contains encoded "=")
          :cljs (>= (.indexOf encoded "=") 0))
    (form-decode-str encoded encoding)
    (reduce
     (fn [m param]
       (if-let [[k v] (str/split param #"=" 2)]
         (assoc-conj m
                     (form-decode-str k encoding)
                     (form-decode-str v encoding))
         m))
     {}
     (str/split encoded #"&"))))
