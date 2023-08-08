(ns wg.sprite
  (:require
   [applied-science.js-interop :as j]))

(defn resolution [sprite]
  [(j/get sprite :width) (j/get sprite :height)])

(defn set-scale! [sprite [x y]]
  (j/assoc! sprite :scale #js {:x x :y y}))

(defn get-pos [sprite]
  [(j/get sprite :x) (j/get sprite :y)])

(defn set-pos! [sprite [x y]]
  (j/assoc! sprite :x x)
  (j/assoc! sprite :y y))
