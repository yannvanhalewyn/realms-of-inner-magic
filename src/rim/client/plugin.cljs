(ns rim.client.plugin
  (:require
   [promesa.core :as p]
   [rim.client.assets :as assets]))

;; This namespace is probably an app/init namespace
(defn init [plugins]
  (let [all-assets (reduce merge (map ::assets plugins))]
    (assets/add-multi! all-assets)
    (p/let [loaded-assets (assets/load! (keys all-assets))]
      {::assets/assets loaded-assets})))
