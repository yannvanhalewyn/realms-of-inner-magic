(ns rim.client.assets
  (:refer-clojure :exclude [get])
  (:require
   [applied-science.js-interop :as j]
   ["pixi.js$Assets" :as Assets]))

(defn add!
  "Adds an assets to the manifest, ready to be loaded."
  [name url]
  (.log js/console :assets/adding name url)
  (j/call Assets :add name url))

(defn add-multi! [name->paths]
  (doseq [[name path] name->paths]
    (add! name path)))

(defn load!
  "Loads the asset and returns a promise that resolves when the assets are
  loaded. Resolves immediately if the asset is already loaded. The promise
  contains all the laoded assets as textures."
  [names]
  (j/call Assets :load (clj->js names)))

(defn get [{::keys [assets]} name]
  (j/get assets name))
