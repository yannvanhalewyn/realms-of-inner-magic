(ns wg.world
  (:require [wg.vec :as vec]
            [wg.app :as app]))

;; how many pixels is
(def SCALE 4)
(def PPM 1000)

(defn meters->pixels [m]
  (vec/* m (* PPM SCALE)))

(defn pixels->meters [px]
  (vec/div px (* PPM SCALE)))

(defn ->pixel-coords [app meters]
  (-> (meters->pixels meters)
      (vec/+ (vec/div (app/resolution app) 2))))

(defn ->world-coords [app pixels]
  ;; 0 0 is w/2 h/2
  ;; 1 0 is w/2 + 1m in pixels
  ;; 800pixels is 0 in the world
  (-> (vec/- pixels (vec/div (app/resolution app) 2))
      (pixels->meters)))
