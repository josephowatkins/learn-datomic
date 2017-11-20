(defproject learn-datomic "SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [com.datomic/datomic-free "0.9.5561.62"]
                 [ragtime "0.7.2"]
                 [environ "1.1.0"]]

  :plugins [[lein-environ "1.1.0"]])
