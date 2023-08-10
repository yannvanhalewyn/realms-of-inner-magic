(ns wg.core
  (:require ["pixi.js" :as pixi]
            [applied-science.js-interop :as j]
            [wg.app :as app]
            [wg.sprite :as sprite]
            [wg.vec :as vec]
            [wg.world :as world]
            [devtools.core :as devtools]
            [wg.client.ws :as ws]
            [sc.api]))

(devtools/set-pref!
 :cljs-land-style
 (str "filter:invert(1);" (:cljs-land-style (devtools/get-prefs))))
(devtools/install! [:custom-formatters :sanity-hints])

(defonce app-atom (atom nil))
(defonce db
  (let [player-id (random-uuid)]
    (atom {:db/player-id player-id
           :db/players {player-id {:player/id player-id
                                   :player/pos [0 0]}}
           :app/sprites {}})))

(defn on-click! [sprite f]
  (j/call sprite :on "pointerdown" f)
  (j/assoc! sprite :eventMode "static"))

(defn on-click [e]
  (let [player-id (:db/player-id @db)]
    (swap! db assoc-in [:db/players player-id :player/pos]
           (world/->world-coords @app-atom [(j/get e :x) (j/get e :y)]))
    (.log js/console [(j/get e :x) (j/get e :y)] (vals (:db/players @db)))))

(defn add-background! [app]
  (let [obj (pixi/Graphics.)]
    (j/call obj :beginFill 0x8c9d3f)
    (j/call obj :drawRect 0 0 (app/get-width app) (app/get-height app))
    (on-click! obj on-click)
    (app/add-child app obj)))

(defn add-player! [db app player]
  (.log js/console "ADD PLAYER" player)
  ;; TODO implement asset loader
  (let [sprite (sprite/make "/img/1_IDLE_000.png")
        pos (:player/pos player)]
    (sprite/set-anchor! sprite 0.5)
    (sprite/set-pos! sprite (world/->pixel-coords app pos))
    (swap! db assoc-in [:app/sprites (:player/id player)] sprite)
    (app/add-child app sprite)
    sprite))

(defn make-update-fn [app]
  (fn [dt]
    (let [{:keys [:db/players :db/player-id :db/backend-players :app/sprites]} @db]
      (doseq [{:player/keys [speed id] :as client-player} (vals players)]
        (let [backend-player (get backend-players id)
              target-pos (:player/pos client-player)
              sprite (get sprites id)
              current-pos (world/->world-coords app (sprite/get-pos sprite))]
          (when-not (= current-pos target-pos)
            (let [diff (vec/- target-pos current-pos)
                  distance (vec/length diff)
                  dir (vec/normalize (vec/div diff distance))
                  step (* 1.38 dt 0.001)

                  new-pos (if (> step distance)
                            target-pos
                            (vec/+ current-pos (vec/* dir step)))]
              (sprite/set-pos!
               sprite (world/->pixel-coords app new-pos)))))))))

(defn mount! [app]
  (add-background! app)

  (doseq [[id player] (:db/players @db)]
    (add-player! db app player))

  (let [update-fn (make-update-fn app)]
    (app/add-update-fn! app update-fn)
    #(app/remove-update-fn! app update-fn)))

(defn socket-message-handler
  [{:keys [ch-recv send-fn state event id ?data] :as ws-client}]
  (when-not (= id :player/update-all)
    (.log js/console :received-msg event ws-client))
  (case id
    :chsk/handshake
    (when (true? (nth ?data 3))
      (ws/send! ws-client [:player/joined {:player/id (:uid @(:state ws-client))}]))
    :player/joined
    (do (add-player! db @app-atom ?data)
        (swap! db assoc-in [:backend/players (:player/id ?data)] ?data))
    :player/update-all
    (swap! db assoc :db/backend-players ?data)
    (.log js/console :ws/unknown-event event)))

(defn ^:dev/after-load refresh! []
  (app/clear! @app-atom)
  (when-let [cleanup (:dev/cleanup @db)]
    (cleanup))
  (swap! db assoc :dev/cleanup (mount! @app-atom)))

(defn main []
  (let [app (app/new {:on-resize refresh!})
        ws-client (ws/connect! (:db/player-id @db))]
    (swap! db assoc :ws/client ws-client)
    (reset! app-atom app)
    (ws/start-listener! ws-client #(socket-message-handler %))
    (swap! db assoc :dev/cleanup (mount! app))))

(comment
  (dissoc @db :ws/client)
  (:app/sprites @db)


  )
