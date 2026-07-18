(ns ossekai.test-runner
  (:require [clojure.test :as test]
            [ossekai.methods.agent-test]
            [ossekai.methods.charter-gates-test]))
(defn -main [& _]
  (let [r (test/run-tests 'ossekai.methods.agent-test 'ossekai.methods.charter-gates-test)]
    (when-not (zero? (+ (:fail r) (:error r))) (throw (ex-info "ossekai tests failed" r)))))
