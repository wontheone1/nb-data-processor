(ns nb-mart.csv
  (:require [clojure.data.csv :as csv]
            [clojure.string :refer [blank?]]))

(defn read-csv-without-bom [file-path]
  "If Byte order mark (BOM) is the first char in the file,
  take the rest of the string except BOM"
  (let [file-content (slurp file-path)
        bom          "\uFEFF"]
    (if (.startsWith file-content bom)
      (csv/read-csv (.substring file-content 1) :separator \;)
      (csv/read-csv file-content :separator \;))))

;; To deal with Sabangnet download
(def freebies-matcher #".*사은품.*")
(def eng-and-num-matcher #"[a-zA-Z]+[0-9]+")
(defn string->model-name [string]
  "Returns model name that is of english and then numbers,
  if not found '(사은품)?만원이상 구매' is the model name"
  (or (re-find eng-and-num-matcher string)
      (re-matches freebies-matcher string)))
(defn row->model-name [vec-string]
  (some string->model-name vec-string))
(defn insert-model-names-from-csv-file [csv-file-path]
  "Returns a lazy sequence of vectors with model names as the first element"
  (let [sabang-net-data (read-csv-without-bom csv-file-path)]
    (for [a-row sabang-net-data]
      (if (blank? (a-row 0))
        (assoc a-row 0 (row->model-name a-row))
        a-row))))

;; To map models and partners
(defn read-model->partner [file-path]
  "Returns a map whose key is model names and value is partner names"
  (let [vectors-of-model->patner (read-csv-without-bom file-path)]
    (reduce #(into %1 {(%2 0) (%2 1)}) {} vectors-of-model->patner)))
