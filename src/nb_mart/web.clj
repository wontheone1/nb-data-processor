(ns nb-mart.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]
            [clojure.data.csv :as csv]))

(defroutes app-routes
           (GET "/" []
             (slurp (io/resource "index.html")))
           (ANY "*" []
             (route/not-found (slurp (io/resource "404.html")))))

(def app
  (wrap-defaults app-routes site-defaults))

(def reloadable-app
  (try (wrap-reload app)
       (catch Exception e
         (println e)
         {:status 500
          :body   (slurp (io/resource "500.html"))})))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'reloadable-app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
