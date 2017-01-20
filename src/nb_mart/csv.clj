(ns nb-mart.csv
  (:require [clojure.data.csv :as csv]
            [clojure.string :refer [blank? starts-with? split]]
            [clojure.java.io :as io]
            [nb-mart.matcher :as nm])
  (:import (java.io File)))

;; Util functions
(defn vec-insert
  "insert elem in coll at pos"
  [coll pos elem]
  (concat (subvec coll 0 pos) [elem] (subvec coll pos)))
(defn read-csv-without-bom
  "If Byte order mark (BOM) is the first char in the file,
  take the rest of the string except BOM"
  ([file-path]
    (read-csv-without-bom file-path \;))
  ([file-path separator]
    (let [file-content (slurp file-path)
          bom          "\uFEFF"]
      (if (.startsWith file-content bom)
        (csv/read-csv (.substring file-content 1) :separator separator)
        (csv/read-csv file-content :separator separator)))))
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
(defn write-processed-data-to-file!
  ([processed-data]
    write-processed-data-to-file! processed-data \;)
  ([processed-data separator]
    (let [temp-file-path (create-temp-file! "output-" ".csv")
          bom "\uFEFF"]
      (println (format "Tempfile created at \n%s" temp-file-path))
      (with-open [out-file (io/writer temp-file-path)]
        (.write out-file bom)
        (csv/write-csv out-file processed-data :separator separator)
        (io/file temp-file-path)))))

;; To map models and partners
(defn read-model->partner [model-file-path]
  "Returns a map whose key is model names and value is partner names"
  (let [vectors-of-model->patner (read-csv-without-bom model-file-path)]
    (reduce #(into %1 {(%2 0) (%2 1)}) {} vectors-of-model->patner)))

;; To standardize unstandardized names in wholesale order file(도매전화주문)
(defn normalize-multi-model-name [multi-model-name]
  "E.g. 'BYR9855/9865/9900/9899' => ['BYR9855' 'BYR9865' 'BYR9900' 'BYR9899']
  'LG8007A/LG8007P' => ['LG8007A' 'LG8007P']"
  (let [names-array       (split multi-model-name #"/")
        first-model-name  (first names-array)
        first-eng-part    (first (split first-model-name #"\d+"))
        names-from-second (rest names-array)]
    (cons first-model-name
          (for [name names-from-second]
            (if (re-find #"^[\d]+" name)
              (str first-eng-part name)
              name)))))
(defn read-standard-model-names [model-file-path]
  "Returns a map whose key is model names and value is partner names"
  (let [vectors-of-model->patner (read-csv-without-bom model-file-path)]
    (->> (map #(normalize-multi-model-name (% 0)) vectors-of-model->patner)
         (apply concat)
         (sort-by count >))))
(defn lookup-standard-name [standard-names non-standard-name]
  (some #(if (.startsWith non-standard-name %) %) standard-names))
(defn insert-standardized-model-names [model-file-path whole-sale-order-file-path separator]
  (let [whole-sale-data (read-csv-without-bom whole-sale-order-file-path separator)]
    (for [a-row whole-sale-data]
      (let [standard-names    (read-standard-model-names model-file-path)
            standard-name     (or (lookup-standard-name standard-names (a-row 2))
                                  (lookup-standard-name standard-names (a-row 1)))]
        (if (blank? standard-name)
          (vec-insert a-row 1 "")
          (vec-insert a-row 1 standard-name))))))

;; To deal with Sabangnet download

(defn- remove-dash-when-eng-dash-eng?num [string]
  (when string
    (if (re-find #"[a-zA-Z]+-[0-9]+(\/[a-zA-Z]*-?[0-9]+)*" string)
      (clojure.string/replace string "-" "")
      string)))
(defn- remove-space-bar [string]
  (when string
    (clojure.string/replace string " " "")))

(defn string->model-name [string]
  "Returns model name that matches the model pattern,
  if not found '(사은품)?만원이상' is the model name.
  The split is there to cutoff the part after space('구매' part)"
  (or (re-find nm/chumy-underscore-model-name-matcher string)
      (if-let [match-string (re-matches nm/freebies-matcher string)]
        (-> match-string
            (split #" ")
            first))
      (-> (re-find nm/byc-eng-num-slash-only-matcher string)
          first)
      (-> (re-find nm/eng-num-dash-matcher string)
          first
          remove-dash-when-eng-dash-eng?num)
      (-> (re-find nm/eng-num-space-matcher string)
          first
          remove-space-bar)
      (re-find nm/all-caps-model-name-matcher string)
      (re-find nm/hangeul-matcher string)))
(defn insert-model-names-from-csv-file [sabang-file-path separator]
  "Returns a lazy sequence of vectors with model names as the first element"
  (let [sabang-net-data (read-csv-without-bom sabang-file-path separator)]
    (for [a-row sabang-net-data]
      (if (blank? (a-row 0))
        (assoc a-row 0 (string->model-name (str (a-row 1) " " (a-row 3))))
        a-row))))
(defn insert-partner-names [model->partner sabang-net-data]
  "Decide partner names based on model names,
  insert the partner names in the first column"
  (for [a-row sabang-net-data]
    (let [model-name (a-row 0)
          partner-name (model->partner model-name)]
      (concat [partner-name] a-row))))
(defn process-sabang-data [model-partner-file-path sabang-data-file-path separator]
  "Process data from sabangnet, write to a file, return the file path"
  (let [model->partner (read-model->partner model-partner-file-path)
        sabang-data    (insert-model-names-from-csv-file sabang-data-file-path separator)]
    (insert-partner-names model->partner sabang-data)))
(defn generate-processed-csv! [model-partner-file-path data-file-path data-type separator]
  (-> (case data-type
        :sabangnet (process-sabang-data model-partner-file-path data-file-path separator)
        :whole-sale (insert-standardized-model-names model-partner-file-path data-file-path separator))
      (write-processed-data-to-file! separator)))

;; To deal with file uploads
(defn file-uploads-then-return-result! [model-file data-csv-file data-type separator]
  (let [model-file-path  (create-temp-file! model-file)
        data-csv-file-path (create-temp-file! data-csv-file)]
    (generate-processed-csv! model-file-path data-csv-file-path data-type (first (char-array separator)))))
