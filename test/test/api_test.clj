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
  "Util functions test"
  (fact
    (csv/vec-insert ["03/31-11" "CDAABFAH03AS85" "첨이스윗가든미디팬티2매 [85]" "5" "6 873" "34 364" "3 436" "37 800" "주식회사제이앤와이스타일"]
                    1
                    "")
    => ["03/31-11" "" "CDAABFAH03AS85" "첨이스윗가든미디팬티2매 [85]" "5" "6 873" "34 364" "3 436" "37 800" "주식회사제이앤와이스타일"]))

(facts
  (let [standard-names (csv/read-standard-model-names "test/mtop.csv")]
    (fact
      (csv/lookup-standard-name standard-names "JHWPQ512AS100")
      => "JHWPQ512")
    (fact
      (csv/lookup-standard-name standard-names "첨이스윗가든미디팬티2매 [85]")
      => nil)))

(facts
  "csv name space related facts"
  (fact
    (csv/normalize-multi-model-name "LG8007A/LG8007P")
    => '("LG8007A" "LG8007P"))
  (fact
    (csv/normalize-multi-model-name "BYR9855/9865/9900/9899")
    => '("BYR9855" "BYR9865" "BYR9900" "BYR9899"))
  (fact
    (csv/insert-standardized-model-names "test/mtop.csv" "test/wholesale.csv")
    => (contains ['("03/02-1" "BYL7739" "BYL7739CL100" "BYL7739센스레이스22호 [100]"
                     "1" "11 318" "11 318" "1 132" "12 450" "이두레")]))
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
