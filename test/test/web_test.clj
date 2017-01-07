(ns test.web-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [nb-mart.web :refer :all]))

(deftest first-test
  (is (= (:status (app (mock/request :get "/foo/bar")))
         404)
      "/foo/bar returns not found response"))
