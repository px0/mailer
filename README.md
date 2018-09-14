# What is Hiccup Mailer

Hiccup mailer is my little fork of
[Mailer](https://github.com/clojurewerkz/mailer). It differs from Mailer in not
requiring you to provide template resource files, but instead letting you
provide an email string that you can construct however you want, or optionally
provide hiccup data that will automatically be converted into HTML.


## Documentation

Mailer uses Postal mail message attribute maps. Key functions are:

 * `px0.mailer.core/build-email`
 * `px0.mailer.core/deliver-email`
 * `px0.mailer.core/render`
 * `px0.mailer.core/delivery-mode!`
 * `px0.mailer.core/with-delivery-mode`
 * `px0.mailer.core/with-settings`

``` clojure
(ns my-app
  (:require [px0.mailer.core :refer [delivery-mode! with-settings with-defaults with-settings build-email deliver-email]]))

;; set default delivery mode (:smtp, :sendmail or :test)
(delivery-mode! :test)

;; build a message (can be used in unit tests or for various forms of delayed delivery)
(build-email {:from "Joe The Robot", :to ["ops@megacorp.internal" "oncall@megacorp.internal"] :subject "OMG everything is down!"}
(format "Holy cow, %s, %s is down!!!!" "Joe" "host3.megacorp.internal") :text/plain)

;;build a message using an HTML template, specify parameter mime type :text/html
(build-email {:from "Joe The Robot", :to ["ops@megacorp.internal" "oncall@megacorp.internal"] :subject "OMG everything is down!"}
  (hiccup/html [:html [:h1 host-that-is-down " is down!!!" ] [:span.text-emergency user " do something!!!!"]] :text/html)

;; build a message using alternative message body, specify alternative plain-text body in addition to main HTML body of the message
(build-email {:from "Joe The Robot", :to ["ops@megacorp.internal" "oncall@megacorp.internal"] :subject "Hello!"}
              [:html [:h1 "Hello World"]]:text/html
              "Hello World" :text/plain)

;; deliver mail, uses *delivery-mode* value to determine how exactly perform the delivery, defaults to :text/plain for strings, and :text/html for everything else
(deliver-email {:from "Joe The Robot", :to ["ops@megacorp.internal" "oncall@megacorp.internal"] :subject "OMG everything is down!"}
  [:h1 "OMG somebody do something!!!"])

;; deliver mail, specify html content type
(deliver-email {:from "Joe The Robot", :to ["ops@megacorp.internal" "oncall@megacorp.internal"] :subject "OMG everything is down!"}
  "<h1> I got rendered by some other means, please send help! </h1>"  :text/html)

;; deliver mail using alternative message body, specify alternative plain-text body in addition to main HTML body of the message
(deliver-email {:from "Joe The Robot", :to ["ops@megacorp.internal" "oncall@megacorp.internal"] :subject "Hello!"}
               [:h1 "Everybody Panic!"] :text/html
               "Panic in plain text!" :text/plain)

;; alter message defaults, for example, From header
(with-defaults { :from "Joe The Robot <robot@megacorp.internal>" :subject "[Do Not Reply] Warning! Achtung! Внимание!" }
  (send-warnings))

;; alter delivery mode (effective for current thread only):
(with-delivery-mode :smtp
  (do-something))

;; alter SMTP settings (effective for current thread only, only makes sense for :smtp delivery mode):
(with-settings { :host "smtp.megacorp.internal" }
  (with-delivery-mode :smtp
    (do-something-that-delivers-email-over-smtp)))

```


## Supported Clojure versions

Mailer requires Clojure 1.8+.


## Development

Mailer uses [Leiningen 2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make sure you have it installed and then run tests
using

    lein test

## License

Copyright © 2012-2017 Michael S. Klishin, Alex Petrov, and the ClojureWerkz team.

Distributed under the Eclipse Public License, the same as Clojure.
