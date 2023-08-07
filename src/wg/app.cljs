(ns wg.app
  (:require [applied-science.js-interop :as j]))

(defn add-child [app child]
  (j/call-in app [:stage :addChild] child))

(defn get-width [app]
  (j/get-in app [:renderer :width]))

(defn get-height [app]
  (j/get-in app [:renderer :height]))

(defn clear! [app]
  (doseq [child (j/get app :children)]
    (.log js/console child )))
