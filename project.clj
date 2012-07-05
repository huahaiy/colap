(defproject colap "0.0.1-SNAPSHOT"
  :description "COLAP"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [clj-logging-config "1.9.7"]
                 [log4j "1.2.16"]
                 [slingshot "0.10.2"]
                 [org.apache.commons/commons-lang3 "3.0.1"]
                 [org.clojure/data.json "0.1.2"]
                 [com.googlecode.javaewah/JavaEWAH "0.5.1"]]
  :dev-dependencies [[vimclojure/server "2.3.1"] 
                     [org.clojars.paul/clj-hector "0.2.1"]
                     [clj-stacktrace "0.2.4"]]
  :warn-on-reflection true
  :aot [colap.bytebuffer])
