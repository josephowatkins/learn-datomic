(ns migrate
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [learn-datomic.helpers.config :as config]
            [ragtime.protocols :as protocols]
            [ragtime.repl :as repl]
            [datomic.api :as d])
  (:import (java.util Date)
           (java.io File)))


(defn- conn [uri]
  (d/connect uri))

(defn- has-attribute? [db name]
  (-> (d/entity db name)
      :db.install/_attribute
      boolean))

(def ^:private migration-schema [{:db/ident :migration/id
                                  :db/valueType :db.type/string
                                  :db/cardinality :db.cardinality/one}
                                 {:db/ident :migration/created-at
                                  :db/valueType :db.type/instant
                                  :db/cardinality :db.cardinality/one}])

(defn- ensure-migration-schema [conn]
  (when-not (has-attribute? (d/db conn) :migration/id)
    @(d/transact conn migration-schema)))

(defrecord DatomicDatabase [uri]
  protocols/DataStore
  (add-migration-id [_ id]
    (let [conn (d/connect uri)]
      (ensure-migration-schema conn)
      @(d/transact conn [{:migration/id id
                          :migration/created-at (java.util.Date.)}])))

  (remove-migration-id [_ id]
    (let [conn (d/connect uri)]
      (ensure-migration-schema conn)
      (when-let [migration (d/q '[:find ?e .
                                  :in $ ?id
                                  :where
                                  [?e :migration/id ?id]]
                                (d/db conn)
                                id)]
        @(d/transact conn [[:db.fn/retractEntity migration]]))))

  (applied-migration-ids [_] ;; should be sorted by created-at ?
    (let [conn (d/connect uri)]
      (ensure-migration-schema conn)
      (->> (d/db conn)
           (d/q '[:find ?id
                  :where
                  [_ :migration/id ?id]])
           (map first)))))

(defrecord DatomicMigration [id up down]
  protocols/Migration
  (id [_] id)

  (run-up! [_ db]
    (let [conn (-> db :uri d/connect)]
      @(d/transact conn up)))

  (run-down! [_  db]
    (let [conn (-> db :uri d/connect)]
      @(d/transact conn down))))

(defn- datomic-migration [id up down]
  (->DatomicMigration id up down))



(defn- file-extension [file]
  (re-find #"\.[^.]*$" (str file)))

(let [pattern (re-pattern (str "([^\\" File/separator "]*)\\" File/separator "?$"))]
  (defn- basename [file]
    (second (re-find pattern (str file)))))

(defn- remove-extension [file]
  (second (re-matches #"(.*)\.[^.]*" (str file))))

(defn- load-files [files]
  (for [file files
        :let [id (-> file basename remove-extension)
              {:keys [up down]} (-> file slurp edn/read-string)]]
    (datomic-migration id up down)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public API

(defn datomic-database [uri]
  (->DatomicDatabase uri))

(defn load-resources [path]
  (->> (io/resource path)
          io/file
          file-seq
          (filter (memfn ^File isFile))
          (filter #(= ".edn" (file-extension %)))
          load-files
          (sort-by :id)))

;; example config
(def config {:datastore (datomic-database config/datomic-uri)
             :migrations (load-resources "migrations")})
