(defproject clojure-getting-started "1.0.0-SNAPSHOT"
  :description "Demo Clojure web app"
  :url "http://clojure-getting-started.herokuapp.com"
  :license {:name "Eclipse Public License v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [environ "1.0.0"]
                 [ring/ring-defaults "0.2.1"]]
  :ring {:handler clojure-getting-started.web/app}
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]
            [lein-ring "0.9.7"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "clojure-getting-started-standalone.jar"
  :profiles {:production {:env {:production true}}
             :dev        {:dependencies [[ring/ring-mock "0.3.0"]
                                         [midje "1.8.3" :exclusions [org.clojure/clojure]]]
                          :plugins      [[lein-midje "3.2.1"]]}})
