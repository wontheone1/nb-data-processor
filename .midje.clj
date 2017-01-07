(ns midje
    (:require
      [midje.config :refer [change-defaults]]))

(change-defaults :print-level :print-facts)
