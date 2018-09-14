;; Copyright (c) 2012-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.mailer.core
  "Key library functionality: build and deliver email, render templates, et cetera."
  (:require [postal.message   :as msg]
            [hiccup.core :as hiccup]
            [postal.core :refer [send-message]]))

;;
;; Implementation
;;

(def ^{:doc "Accumulates mail messages delivered with the :test delivery mode."}
  deliveries (atom []))

(def ^{:dynamic true
       :doc "Delivery mode to use. One of :smtp, :test, :sendmail. With the :test
             delivery mode, real deliveries won't happen. Instead, they will be accumulated to the
             deliveries collection."} *delivery-mode* :smtp)
(def ^{:dynamic true
       :doc "Settings to use for email delivery (e.g. SMTP configuration)"} *delivery-settings*)

(def ^{:dynamic true
       :doc "Default email message parameters (useful for setting From and CC headers, for example)"} *message-defaults* {})

(def delivery-modes (atom {}))

(defn register-delivery-mode
  [mode-name f]
  (swap! delivery-modes assoc (keyword mode-name) f))


(declare build-email)
(defn deliver-in-test-mode
  [m template content-type & more-data]
  (swap! deliveries conj (apply build-email m template content-type more-data)))

(defn deliver-with-smtp
  [m template content-type & more-data]
  (send-message *delivery-settings* (apply build-email m template content-type more-data)))

(defn deliver-with-sendmail
  [m template content-type & more-data]
  (send-message {} (apply build-email m template content-type more-data)))

(definline check-not-nil! [v ^String m]
  `(when (nil? ~v)
     (throw (IllegalArgumentException. ~m))))

;;
;; API
;;

(defn delivery-mode!
  "Sets default delivery mode by altering root binding of *delivery-mode*"
  [mode]
  (alter-var-root (var *delivery-mode*) (constantly mode)))

(defn defaults!
  [m]
  (alter-var-root (var *message-defaults*) (constantly m)))

(defn settings!
  [m]
  (alter-var-root (var *delivery-settings*) (constantly m)))

(defmacro with-delivery-mode
  [mode & body]
  `(binding [*delivery-mode* ~mode]
     ~@body))

(defmacro with-defaults
  [m & body]
  `(binding [*message-defaults* ~m]
     ~@body))

(defmacro with-settings
  [m & body]
  `(binding [*delivery-settings* ~m]
     ~@body))


(defn- mime-type-str
  [content-type]
  (str (namespace content-type) "/" (name content-type)))

(defprotocol ContentType
  (get-content-type [value]))

(extend-protocol ContentType
  clojure.lang.Keyword
    (get-content-type
      [k]
      (mime-type-str k))
  java.lang.String
    (get-content-type
      [s]
      s))

(defn deep-merge-into
  "Recursively merge maps applying into when there's a non-map. based on conjure.contrib/map_utils deep-merge-with"
  [& maps]
  (apply
    (fn m [& maps]
      (if (every? map? maps)
        (apply merge-with m maps)
        (apply into maps)))
    maps))

(defn get-content-with-type
  "Helper which renders contents and assigns content-type."
  [template content-type]
  {:content (if (= content-type :text/plain)
              (if (string? template)
                template
                (hiccup/html template))
              (hiccup/html template))
   :type (get-content-type content-type)})

(defn build-email
  "Builds up a mail message (returned as an immutable map). Body is rendered from a given hiccup template."
  [m template content-type & more-data]
  (let [contents (map (partial apply get-content-with-type )
                      (partition-all 2 (concat [template content-type] more-data)))]
    (deep-merge-into *message-defaults* m
                     {:body (if (empty? more-data)
                              contents
                              (cons :alternative contents))})))


(defn deliver-email
  "Delivers a mail message using delivery mode specified by the *delivery-mode* var. Body is rendered from a given template."
  ([m template]
   (deliver-email m template (if (string? template) :text/plain :text/html)))
  ([m template content-type & more-data]
     (io!
      (if-let [f (get @delivery-modes *delivery-mode*)]
        (apply f m template content-type more-data)
        (throw (IllegalArgumentException. (format  "%s delivery mode implementation is not registered. Possibly you misspelled %s?" *delivery-mode* *delivery-mode*)))))))


(defn reset-deliveries!
  "Resets test mode deliveries. Typically this is performed before and after each test."
  ([]
     (reset! deliveries []))
  ([f]
     (reset-deliveries!)
     (f)
     (reset-deliveries!)))


;; register core delivery methods
(register-delivery-mode :test     deliver-in-test-mode)
(register-delivery-mode :smtp     deliver-with-smtp)
(register-delivery-mode :sendmail deliver-with-sendmail)
