(ns wg.core
  (:require ["pixi.js" :as pixi]
            [applied-science.js-interop :as j]
            [wg.app :as app]
            [wg.sprite :as sprite]
            [wg.vec :as vec]
            [wg.world :as world]
            [devtools.core :as devtools]
            [wg.client.ws :as ws]))

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
  (swap! db assoc-in [:db/player :player/pos]
         (world/->world-coords @app-atom [(j/get e :x) (j/get e :y)])))

(defn add-background! [app]
  (let [obj (pixi/Graphics.)]
    (j/call obj :beginFill 0x8c9d3f)
    (j/call obj :drawRect 0 0 (app/get-width app) (app/get-height app))
    (on-click! obj on-click)
    (app/add-child app obj)))

(defn render! [app]
  (let [player (make-sprite "/img/1_IDLE_000.png")]
    (swap! db assoc :player-sprite player)
    (add-background! app)
    (app/add-child app player)

    (j/call-in player [:anchor :set] 0.5)

    (let [pos [0 0]]
      (swap! db assoc :db/player {:player/pos pos
                                  ;; m/s
                                  :player/speed 1.38})
      (sprite/set-pos! player (world/->pixel-coords app pos)))

    (let [update-fn (fn [dt]
                      (let [{:player/keys [speed]
                             target-pos :player/pos} (:db/player @db)
                            sprite-pos (sprite/get-pos player)
                            current-pos (world/->world-coords app sprite-pos)]

                        (when-not (= current-pos target-pos)
                          (let [diff (vec/- target-pos current-pos)
                                distance (vec/length diff)
                                dir (vec/normalize (vec/div diff distance))
                                step (* speed dt 0.001)
                                new-pos (if (> step distance)
                                          target-pos
                                          (vec/+ current-pos (vec/* dir step)))]
                            (sprite/set-pos!
                             player (world/->pixel-coords app new-pos))))))]

      (j/call-in app [:ticker :add] update-fn)
      #(do (.log js/console "Cleanup!")
           (j/call-in app [:ticker :remove] update-fn)))))

(defn socket-message-handler
  [{:keys [ch-recv send-fn state event id ?data] :as ws-client}]
  (when-not (= id :player/update-all)
    (.log js/console :received-msg event ws-client))
  (case id
    :chsk/handshake
    (when (true? (nth ?data 3))
      (ws/send! ws-client [:player/joined {:player/id (:uid @(:state ws-client))}]))
    :player/joined
    [] ;; Add a player to the app stage. I really need some state management here :/
    :player/update-all
    (swap! db assoc :backend/players ?data)
    (.log js/console :ws/unknown-event event)))

(defn ^:dev/after-load refresh! []
  (app/clear! @app-atom)
  (when-let [cleanup (:dev/cleanup @db)]
    (cleanup))
  (swap! db assoc :dev/cleanup (render! @app-atom)))

(defn main []
  (let [app (app/new {:on-resize refresh!})
        ws-client (ws/connect!)]
    (swap! db assoc :ws/client ws-client)
    (reset! app-atom app)
    (ws/start-listener! ws-client #(socket-message-handler %))
    (swap! db assoc :dev/cleanup (render! app))))
