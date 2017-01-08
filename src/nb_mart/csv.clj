(ns nb-mart.csv
  (:require [clojure.data.csv :as csv]
            [clojure.string :refer [blank?]]
            [clojure.java.io :as io])
  (:import (java.io File)))

(defn read-csv-without-bom [file-path]
  "If Byte order mark (BOM) is the first char in the file,
  take the rest of the string except BOM"
  (let [file-content (slurp file-path)
        bom          "\uFEFF"]
    (if (.startsWith file-content bom)
      (csv/read-csv (.substring file-content 1) :separator \;)
      (csv/read-csv file-content :separator \;))))
(defn create-temp-file!
  ([prefix suffix]
   (let [file (doto (File/createTempFile prefix suffix)
                .deleteOnExit)]
     ; TODO: log file path
     (.getCanonicalPath file)))
  ([content]
   (let [file (doto (File/createTempFile "input-" ".csv")
                .deleteOnExit)]
     (io/copy content file)
     (.getCanonicalPath file))))
(defn write-processed-data-to-file! [processed-data]
  (let [temp-file-path (create-temp-file! "output-" ".csv")
        bom            "\uFEFF"]
    (with-open [out-file (io/writer temp-file-path)]
      (.write out-file bom)
      (csv/write-csv out-file processed-data :separator \;)
      (io/file temp-file-path))))

;; To map models and partners
(defn read-model->partner [file-path]
  "Returns a map whose key is model names and value is partner names"
  (let [vectors-of-model->patner (read-csv-without-bom file-path)]
    (reduce #(into %1 {(%2 0) (%2 1)}) {} vectors-of-model->patner)))

;; To deal with Sabangnet download
(def freebies-matcher #".*사은품.*")
(def eng-and-num-matcher #"[a-zA-Z]+[0-9]+")
(defn string->model-name [string]
  "Returns model name that is of english and then numbers,
  if not found '(사은품)?만원이상' is the model name.
  The split is there to cutoff the part after space(' gumae'"
  (or (re-find eng-and-num-matcher string)
      (if-let [match-string (re-matches freebies-matcher string)]
        (-> match-string
            (.split " ")
            first))))
(defn row->model-name [vec-string]
  (some string->model-name vec-string))
(defn insert-model-names-from-csv-file [csv-file-path]
  "Returns a lazy sequence of vectors with model names as the first element"
  (let [sabang-net-data (read-csv-without-bom csv-file-path)]
    (for [a-row sabang-net-data]
      (if (blank? (a-row 0))
        (assoc a-row 0 (row->model-name a-row))
        a-row))))
(defn insert-partner-names [model->partner sabang-net-data]
  "Decide partner names based on model names,
  insert the partner names in the first column"
  (for [a-row sabang-net-data]
    (let [model-name (a-row 0)
          partner-name (model->partner model-name)]
      (concat [partner-name] a-row))))
(defn process-sabang-data [model-partner-file-path sabang-data-file-path]
  "Process data from sabangnet, write to a file, return the file path"
  (let [model->partner (read-model->partner model-partner-file-path)
        sabang-data    (insert-model-names-from-csv-file sabang-data-file-path)]
    (insert-partner-names model->partner sabang-data)))
(defn generate-processed-csv! [model-partner-file-path sabang-data-file-path]
  (-> (process-sabang-data model-partner-file-path sabang-data-file-path)
      (write-processed-data-to-file!)))

;; To deal with file uploads
(defn file-uploads-then-return-result! [model-file sabang-file]
  (let [model-file-path  (create-temp-file! model-file)
        sabang-file-path (create-temp-file! sabang-file)]
    (generate-processed-csv! model-file-path sabang-file-path)))
