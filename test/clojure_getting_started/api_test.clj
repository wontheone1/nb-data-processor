(ns clojure-getting-started.api-test
  (:require [ring.mock.request :as mock]
            [midje.sweet :refer :all]
            [clojure-getting-started.web :refer :all]))

(fact
  "Root path returns status 200"
  (app (mock/request :get "/")) => (contains {:status 200}))