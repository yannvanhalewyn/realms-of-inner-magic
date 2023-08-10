(ns wg.app
  (:require [applied-science.js-interop :as j]
            ["pixi.js" :as pixi]))

(defn resize [app]
  (j/call-in app [:renderer :resize]
             (j/get js/window :innerWidth)
             (j/get js/window :innerHeight)))

(defn new [{:keys [on-resize]}]
  (let [app (pixi/Application.
             #js {:autoResize true
                  :resolution (j/get js/window :devicePixelRation)})]
    (j/call js/window :addEventListener "resize"
            #(do (resize app)
                 (when on-resize (on-resize))))
    (resize app)
    (j/call-in js/document [:body :appendChild] (j/get app :view))
    app))

(defn add-child [app child]
  (j/call-in app [:stage :addChild] child))

(defn get-width [app]
  (j/get-in app [:renderer :width]))

(defn get-height [app]
  (j/get-in app [:renderer :height]))

(defn resolution [app]
  [(get-width app) (get-height app)])

(defn last-time [app]
  (j/get-in app [:ticker :lastTime]))

(defn add-update-fn! [app update-fn]
  (j/call-in app [:ticker :add] update-fn))

(defn remove-update-fn! [app update-fn]
  (j/call-in app [:ticker :remove] update-fn))

(defn clear! [app]
  (j/call-in app [:stage :removeChildren]))
