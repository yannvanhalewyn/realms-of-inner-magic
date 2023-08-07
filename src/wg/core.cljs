(ns wg.core
  (:require ["pixi.js" :as pixi]
            [applied-science.js-interop :as j]
            [wg.app :as app]
            [wg.sprite :as sprite]
            [wg.vec :as vec]
            [devtools.core :as devtools]))

(devtools/set-pref!
 :cljs-land-style
 (str "filter:invert(1);" (:cljs-land-style (devtools/get-prefs))))
(devtools/install! [:custom-formatters :sanity-hints])

(defonce app-atom (atom nil))
(defonce db (atom {}))

(defn make-sprite [url]
  (j/call pixi/Sprite :from url))

(defn on-click! [sprite f]
  (j/call sprite :on "pointerdown" f)
  (j/assoc! sprite :eventMode "static"))

(defn on-click [e]
  (let [{:keys [player-sprite]} @db]
    (swap! db assoc-in [:db/player :player/pos] [(j/get e :x) (j/get e :y)])))

(defn add-background! [app]
  (let [obj (pixi/Graphics.)]
    (j/call obj :beginFill 0x8c9d3f)
    (j/call obj :drawRect 0 0 (app/get-width app) (app/get-height app))
    (on-click! obj on-click)
    (app/add-child app obj)))

(defn render! [app]
  (let [bunny (make-sprite "https://pixijs.com/assets/bunny.png")]
    (swap! db assoc :player-sprite bunny)
    (add-background! app)
    (app/add-child app bunny)

    (j/call-in bunny [:anchor :set] 0.5)

    (let [x (/ (app/get-width app) 2)
          y (/ (app/get-height app) 2)]
      (swap! db assoc :db/player {:player/pos [x y]
                                  ;; pixels per second
                                  :player/speed 10})
      (j/assoc! bunny :x x)
      (j/assoc! bunny :y y))

    (j/call-in app [:ticker :add]
               (fn [dt]
                 (let [{:player/keys [pos speed]} (:db/player @db)
                       sprite-pos (sprite/get-pos bunny)]
                   (when-not (= sprite-pos pos)
                     ;; Move a frame in the direction from sprite-pos to player-pos
                     (let [dir (vec/normalize (vec/- pos sprite-pos))
                           translate (vec/* dir (* speed dt))
                           [new-x new-y] (vec/+ sprite-pos translate)]
                       ;; (.log js/console "dir:" new-x "translate:" translate "newpos:" [new-x new-y])
                       (j/assoc! bunny :x new-x)
                       (j/assoc! bunny :y new-y))))
                 (j/update! bunny :rotation #(+ % (* 0.01 dt)))))))

(defn ^:dev/after-load refresh! []
  (app/clear! @app-atom)
  (render! @app-atom))

(defn main []
  (let [app (app/new {:on-resize refresh!})]
    (reset! app-atom app)
    (render! app)))
