(ns wg.sprite
  (:require [applied-science.js-interop :as j]))

(defn get-pos [sprite]
  [(j/get sprite :x) (j/get sprite :y)])
