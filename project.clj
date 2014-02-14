(defproject clj-pail-tap "0.1.0-SNAPSHOT"
  :description "Library for making taps easier with Pail."
  :url "http://github.com/EricGebhart/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"

  :source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.backtype/dfs-datastores "1.3.4"]
                 [potemkin "0.3.4"]
                 [clj-pail "0.1.3"]]

  :profiles {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0-master-SNAPSHOT"]]}

             :dev {:dependencies [[midje "1.5.1"]]
                   :plugins [[lein-midje "3.0.1"]]
                   :source-paths ["src/test"]
                   :aot [clj-pail-tap.fakes.structure]
                   }}


  :deploy-repositories [["releases" {:url "https://clojars.org/repo" :username :gpg :password :gpg}]
                        ["snapshots" {:url "https://clojars.org/repo" :username :gpg :password :gpg}]])
