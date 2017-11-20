(ns learn-datomic.helpers.config
  (:require [environ.core :refer [env]]))

(def datomic-uri
  (env :datomic-uri "datomic:free://localhost:4334/films"))
