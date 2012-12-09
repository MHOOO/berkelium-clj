(defproject de.karolski.berkelium-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure.contrib/core "1.3.0-alpha4"]
                 [org.clojure.contrib/logging "1.3.0-alpha4"]]
  :java-source-paths ["src/main/java"]
  :source-paths ["src/main/clojure"]
  :prep-tasks ["swig" "javac" "compile"]
  :native-path "./native"
  :berkelium-home "../../sources/berkelium.git"
  :main de.karolski.berkelium-clj.core
)
