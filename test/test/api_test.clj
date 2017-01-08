(ns test.api-test
  (:require [clojure.string :refer [blank?]]
            [ring.mock.request :as mock]
            [midje.sweet :refer :all]
            [midje.config :refer [change-defaults]]
            [nb-mart.web :as web]
            [nb-mart.csv :as csv]))

(fact
  "Root path returns status 200"
  (web/app (mock/request :get "/")) => (contains {:status 200}))

(facts
  "csv name space related facts"
  (fact
    (csv/normalize-multi-model-name "LG8007A/LG8007P")
    => '("LG8007A" "LG8007P"))
  (fact
    (csv/normalize-multi-model-name "BYR9855/9865/9900/9899")
    => '("BYR9855" "BYR9865" "BYR9900" "BYR9899"))
  (fact
    "read ';' separated csv file and turns it into a map,
    check if it has expected data structure"
    (csv/read-model->partner "test/mtop.csv")
    => (every-checker #(= (% "(사은품)10만원이상") "(사은품)10만원이상구매")
                      #(= (% "12MK1098W") "가림텍스")))
  (fact
    "After inserting model names at the first column,
    they match with either english + number model scheme or contains 사은품,
    except the first"
    (rest (csv/insert-model-names-from-csv-file "test/sabang.csv"))
    => (has every? #(or (re-find csv/model-name-matcher (% 0))
                        (re-matches csv/freebies-matcher (% 0)))))
  (fact
    "After inserting partner names, there are 12 columns for each row"
    (csv/process-sabang-data "test/mtop.csv" "test/sabang.csv")
    => (has every? #(= (count %) 12))))
