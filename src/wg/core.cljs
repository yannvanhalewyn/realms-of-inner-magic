(ns wg.core
  (:require ["pixi.js" :as pixi]
            [applied-science.js-interop :as j]))

(defonce app-atom (atom nil))

(defn make-sprite [url]
  (j/call pixi/Sprite :from url))

(defn ^:dev/after-load render! []
  (let [app @app-atom
        bunny (make-sprite "https://pixijs.com/assets/bunny.png")]
    (j/call-in app [:stage :addChild] bunny)

    (j/call-in bunny [:anchor :set] 0.5)
    (j/assoc! bunny :x (/ (j/get-in app [:renderer :width]) 2))
    (j/assoc! bunny :y (/ (j/get-in app [:renderer :height]) 2))

    (j/call-in app [:ticker :add]
               (fn [dt]
                 (j/update! bunny :rotation #(+ % (* 0.01 dt)))))))

(defn main []
  (let [app (pixi/Application.)]
    (reset! app-atom app)
    (j/call-in js/document [:body :appendChild] (j/get app :view))
    (render!)))
