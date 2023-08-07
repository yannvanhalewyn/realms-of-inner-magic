(ns wg.sprite
  (:require [applied-science.js-interop :as j]))

(defn get-pos [sprite]
  [(j/get sprite :x) (j/get sprite :y)])

(defn set-pos! [sprite [x y]]
  (j/assoc! sprite :x x)
  (j/assoc! sprite :y y))
