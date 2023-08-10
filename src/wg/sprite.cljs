(ns wg.sprite
  (:require
   ["pixi.js" :as pixi]
   [applied-science.js-interop :as j]))

(defn make [url]
  (j/call pixi/Sprite :from url))

(defn resolution [sprite]
  [(j/get sprite :width) (j/get sprite :height)])

(defn set-scale! [sprite [x y]]
  (j/assoc! sprite :scale #js {:x x :y y}))

(defn set-anchor! [sprite anchor]
  (j/call-in sprite [:anchor :set] anchor))

(defn get-pos [sprite]
  [(j/get sprite :x) (j/get sprite :y)])

(defn set-pos! [sprite [x y]]
  (j/assoc! sprite :x x)
  (j/assoc! sprite :y y))
