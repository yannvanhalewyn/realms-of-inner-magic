(ns wg.vec
  (:refer-clojure :exclude [+ - * / div]))

(def +' clojure.core/+)
(def -' clojure.core/-)
(def *' clojure.core/*)
;; /' is an invalid symbol ¯\_(ツ)_/¯
(def div clojure.core//)

(defn length [[x y]]
  (Math/sqrt (+' (*' x x) (*' y y))))

(defn + [[x1 y1] [x2 y2]]
  [(+' x1 x2) (+' y1 y2)])

(defn - [[x1 y1] [x2 y2]]
  [(-' x1 x2) (-' y1 y2)])

(defn * [[x y] scale]
  [(*' x scale) (*' y scale)])

(defn / [[x1 y1] [x2 y2]]
  [(/ x1 x2) (/ y1 y2)])

(defn div [[x y] scale]
  [(div x scale) (div y scale)])

(defn normalize [v]
  (div v (length v)))
