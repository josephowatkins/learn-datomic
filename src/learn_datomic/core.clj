(ns learn-datomic.core
  (:require [datomic.api :as d]
            [learn-datomic.helpers.config :as config]))


(def conn (d/connect config/datomic-uri))

(def first-movies [{:film/title "The Goonies"
                    :film/genre "action/adventure"
                    :film/release-year 1985}
                   {:film/title "Commando"
                    :film/genre "action/adventure"
                    :film/release-year 1985}
                   {:film/title "Repo Man"
                    :film/genre "punk dystopia"
                    :film/release-year 1984}])

(def db (d/db conn))

(def all-films-q '[:find ?e :where [?e :film/title]])
(def all-titles-q '[:find ?film-title :where [_ :film/title ?film-title]])
(def titles-from-1985 '[:find ?title
                        :where [?e :film/title ?title]
                               [?e :film/release-year 1985]])

(def all-data-from-1985 '[:find ?e ?title ?year ?genre
                          :where [?e :film/title ?title]
                                 [?e :film/release-year ?year]
                                 [?e :film/genre ?genre]
                                 [?e :film/release-year 1985]])

(def commando-id (ffirst (d/q '[:find ?e :where [?e :film/title "Commando"]] db)))

(def commando-update [{:db/id commando-id :film/genre "future guvenor"}])

(def hdb (d/history db))
