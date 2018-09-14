(defproject px0/hiccup-mailer "1.4.0"
  :description "An ActionMailer-inspired mailer library. Combines Postal, Hiccup, some conventions and support for multiple delivery modes."
  :url "https://github.com/px0/mailer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure             "1.9.0"]
                 [com.draines/postal              "1.11.3"]
                 [hiccup "1.0.5"]
                 ]
  :test-selectors {:focus          :focus
                   :all            (constantly true)}
  :source-paths ["src/clojure"]
  :profiles {:dev {:resource-paths ["test/resources"]}}
  :repositories {"clojure-releases" "http://build.clojure.org/releases"
                 "sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail :update :always}}
                 "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}}
  :global-vars {*warn-on-reflection* true})
