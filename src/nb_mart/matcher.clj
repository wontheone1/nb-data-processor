(ns nb-mart.matcher)

(def chumy-underscore-model-name-matcher #"[A-Z0-9]+_[0-9]+")
(def byc-eng-num-slash-only-matcher #"BY[a-zA-Z]*[0-9]+(\/[0-9]+)*")
(def eng-num-dash-matcher #"[a-zA-Z0-9]*[a-zA-Z]{2,}-?[0-9]+[a-zA-Z0-9-]+(\/[a-zA-Z0-9]*[a-zA-Z]*-?[0-9]+[a-zA-Z0-9-]+)*")
(def eng-num-space-matcher #"[a-zA-Z]+ ?[0-9]+[a-zA-Z0-9]*(\/[a-zA-Z]* ?[0-9]+[a-zA-Z0-9]*)*")
(def freebies-matcher #".*\(사은품\).*")
(def all-caps-model-name-matcher #"[A-Z]{4,}")
(def hangeul-matcher #"[가-힣]{2,} ?-?_?[0-9]+[a-zA-Z]*")