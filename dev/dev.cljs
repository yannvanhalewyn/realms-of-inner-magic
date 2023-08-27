(ns dev
  (:require [promesa.core :as p]
            [wg.core :as core]))

(defn log-promise [promise]
  (p/then promise #(.log js/console %)))

(def db #'core/db)

(defn get! [k]
  (get @core/db k))

(defn -main []
  (core/-main))
