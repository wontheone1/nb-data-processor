(ns nb-mart.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.multipart-params :refer :all]
            [ring.middleware.params :refer :all]
            [environ.core :refer [env]]
            [clojure.data.csv :as csv]
            [nb-mart.csv :as nb-csv]))

(defroutes app-routes
           (GET "/" []
             (slurp (io/resource "index.html")))
           (POST "/data" request
             {:status 200
              :headers {"Content-Type" "application/octet-stream"
                        "Content-Disposition" "attachment;filename=\\\"result.txt\\\""}}
              :body (nb-csv/file-uploads-then-return-result!
                      (get-in request [:params :model-file :tempfile])
                      (get-in request [:params :sabang-file :tempfile])))
           (ANY "*" []
             (route/not-found (slurp (io/resource "404.html")))))

(def app
  (-> app-routes
      wrap-params
      wrap-multipart-params))

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
