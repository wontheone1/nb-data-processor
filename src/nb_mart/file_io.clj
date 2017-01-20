(ns nb-mart.file-io
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:import (java.io File)))

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

