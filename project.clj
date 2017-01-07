(defproject clojure-getting-started "1.0.0-SNAPSHOT"
  :description "Demo Clojure web app"
  :url "http://clojure-getting-started.herokuapp.com"
  :license {:name "Eclipse Public License v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring "1.5.0"]
                 [environ "1.0.0"]
                 [ring/ring-defaults "0.2.1"]
                 [org.clojure/data.csv "0.1.3"]]
  :ring {:handler nb-mart.web/app}
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]
            [lein-ring "0.9.7"]]
  :hooks [environ.leiningen.hooks]
  ;; All generated files will be placed in :target-path. In order to avoid
  ;; cross-profile contamination (for instance, uberjar classes interfering
  ;; with development), it's recommended to include %s in in your custom
  ;; :target-path, which will splice in names of the currently active profiles.
  :target-path "target/%s/"
  :uberjar-name "clojure-getting-started-standalone.jar"
  :profiles {:production {:env {:production true}}
             :dev        {:dependencies [[ring/ring-mock "0.3.0"]
                                         [midje "1.8.3" :exclusions [org.clojure/clojure]]]
                          :plugins      [[lein-midje "3.2.1"]]
                          :repl-options {:init-ns user
                                         :init    (do (println "here we are in" *ns*)
                                                      (use 'nb-mart.web)
                                                      (use 'nb-mart.csv))}
                          :source-paths ["dev"]
                          }})
