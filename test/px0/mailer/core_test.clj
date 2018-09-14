(ns px0.mailer.core-test
  (:use clojure.test
        px0.mailer.core))

(delivery-mode! :test)

(use-fixtures :each reset-deliveries!)

;;
;; Unknown delivery modes
;;

(deftest test-attempt-to-use-unknown-delivery-mode
  (is (thrown? IllegalArgumentException
               (with-delivery-mode :magic-fairies
                 (deliver-email {:from "joe@giove.local" :to "Tom <tom@giove.local>"}
                                [:span "Hello Tom!"])))))



;;
;; Building
;;

(deftest test-building-messages
  (let [d        { :cc ["baz@bar.dom" "Quux <quux@bar.dom>"] }
        expected-hdr {:from "fee@bar.dom"
                      :to "Foo Bar <foo@bar.dom>"
                      :cc ["baz@bar.dom" "Quux <quux@bar.dom>"]
                      :subject "Hello"}
        expected-content "Hello, Joe!"
        expected-type "text/plain"]
    (with-delivery-mode :test
      (with-defaults d
        (let [email (build-email {:from    "fee@bar.dom"
                                  :to      "Foo Bar <foo@bar.dom>"
                                  :subject "Hello"}
                                 (str "Hello, " "Joe" "!")
                                 :text/plain)
              content (:content (first (:body email)))
              type (:type (first (:body email)))]
          (doseq [[k v] expected-hdr]
            (is (= v (k email))))
          (is (= content expected-content))
          (is (= type expected-type)))))))



(deftest test-content-type-keyword
  (let [email (build-email {} [:div "Hello, world!"] :text/html)
        type (:type (first (:body email)))]
      (is (= type "text/html"))))

(deftest test-content-type-string
  (let [email (build-email {} [:div "Hello, world!"] "text/html")
        type (:type (first (:body email)))]
    (is (= type "text/html"))))


;;
;; Test Delivery
;;

(deftest test-test-delivery-mode
  (is (= 0 (count @deliveries)))
  (with-delivery-mode :test
    (deliver-email {:from "joe@giove.local" :to "Tom <tom@giove.local>"}
                   [:div "Hello, world!"] )
    (deliver-email {:from "Tom <tom@giove.local>" :to "joe@giove.local"}
                   "Hello, world!" ))
  (is (= 2 (count @deliveries))))

(deftest test-reset-deliveries
  (is (= 0 (count @deliveries)))
  (with-delivery-mode :test
    (deliver-email {:from "joe@giove.local" :to "Tom <tom@giove.local>"}
                   "Hello, world!" )
    (deliver-email {:from "Tom <tom@giove.local>" :to "joe@giove.local"}
                   [:div "Hello, world!"] ))
  (is (= 2 (count @deliveries)))
  (reset-deliveries!)
  (is (= 0 (count @deliveries))))


;;
;; SMTP Delivery
;;

;; TBD


;;
;; Sendmail Delivery
;;

;; TBD

;;
;; Merging
;;

(deftest test-merged-maps
  (is (= {:body [:alternative
                 {:type "text/plain" :content "test content"}
                 {:type "text/html" :content "<p>content</p>"}]
          :subject "subj"
          :to "person@anywhere.com"}
         (deep-merge-into {:body [:alternative {:content "test content" :type "text/plain"}]
                           :subject "subj"
                           :to "person@anywhere.com"}
                          {:body [{:content "<p>content</p>" :type "text/html"}]}))))
