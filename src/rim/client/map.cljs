(ns rim.client.map
  (:require
   [rim.client.plugin :as plugin]))

(def plugin
  {::plugin/assets {"map.json" "./map.json"
                    "terrain.png" "./img/terrain.png"}})
