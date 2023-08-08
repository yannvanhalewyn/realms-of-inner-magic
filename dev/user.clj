(ns user
  (:require [clojure.tools.namespace.repl :as ns]))

(defn- switch-to-dev []
  (in-ns 'dev)
  :ok)

(defn dev
  "Loads all source files and switches to the 'dev' namespace."
  []
  (set! *print-namespace-maps* false)
  (let [ret (ns/refresh :after `switch-to-dev)]
    (if (instance? Throwable ret)
      (throw ret)
      ret)))
