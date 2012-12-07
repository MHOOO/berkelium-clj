(ns leiningen.swig)

(defn swig [{:keys [berkelium-home]}]
  (assert (not (nil? berkelium-home)) "Please Specify :berkelium-home inside your project.clj.")
  (let [{:keys [out exit]} (clojure.java.shell/sh "./scripts/setup-berkelium.sh" berkelium-home)]
    (println out)
    (case exit
      0 true
      1 (do
          (throw (RuntimeException. "Failed to run script. Stopping."))))))
