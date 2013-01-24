(defproject de.karolski/berkelium-clj "0.1.0-SNAPSHOT"
  :description "Wrapper around the berkelium project (Chromium based offscreen renderer for Web Pages)."
  :url "https://github.com/MHOOO/berkelium-clj.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"] 
                 [org.clojure/tools.logging "0.2.3"]
                 [slingshot "0.10.3"]]
  :java-source-paths ["src/main/java"]
  :source-paths ["src/main/clojure"]
  :resource-paths ["src/main/resource"]
  :prep-tasks ["swig" "javac" "compile"]
  :berkelium-home "../../sources/berkelium.git"
  :jar-exclusions [#".clj$" #".java$"]
  :aot [de.karolski.berkelium-clj.core]
  :main de.karolski.berkelium-clj.core
)
