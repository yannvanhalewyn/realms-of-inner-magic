(ns wg.core
  (:require ["pixi.js" :as pixi]
            [applied-science.js-interop :as j]
            [wg.app :as app]))

(defonce app-atom (atom nil))

(defn make-sprite [url]
  (j/call pixi/Sprite :from url))

(defn on-click! [sprite f]
  (j/call sprite :on "pointerdown" f)
  (j/assoc! sprite :eventMode "static"))

(defn add-background! [app]
  (let [obj (pixi/Graphics.)]
    (j/call obj :beginFill 0x8c9d3f)
    (j/call obj :drawRect 0 0 (app/get-width app) (app/get-height app))
    (on-click! obj #(js/alert "HI!"))
    (app/add-child app obj)))

(defn render! [app]
  (let [bunny (make-sprite "https://pixijs.com/assets/bunny.png")]
    (add-background! app)
    (app/add-child app bunny)

    (j/call-in bunny [:anchor :set] 0.5)
    (j/assoc! bunny :x (/ (app/get-width app) 2))
    (j/assoc! bunny :y (/ (app/get-height app) 2))

    (j/call-in app [:ticker :add]
               (fn [dt]
                 (j/update! bunny :rotation #(+ % (* 0.01 dt)))))))

(defn ^:dev/after-load refresh! []
  (app/clear! @app-atom)
  (render! @app-atom))

(defn main []
  (let [app (pixi/Application.)]
    (reset! app-atom app)
    (j/call-in js/document [:body :appendChild] (j/get app :view))
    (render! app)))
