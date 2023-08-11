(ns wg.core
  (:require ["pixi.js" :as pixi]
            [applied-science.js-interop :as j]
            [wg.app :as app]
            [wg.sprite :as sprite]
            [wg.vec :as vec]
            [wg.world :as world]
            [devtools.core :as devtools]
            [wg.client.ws :as ws]
            [rim.util.timer :as timer]
            [clojure.set :as set]
            [medley.core :as m]
            [sc.api]))

(devtools/set-pref!
 :cljs-land-style
 (str "filter:invert(1);" (:cljs-land-style (devtools/get-prefs))))
(devtools/install! [:custom-formatters :sanity-hints])

(defonce db
  (let [player-id (random-uuid)]
    (atom {:db/player-id player-id
           :app/sprites {}})))

(defn on-click! [sprite f]
  (j/call sprite :on "pointerdown" f)
  (j/assoc! sprite :eventMode "static"))

(defn on-click [e]
  (let [world-coords (world/->world-coords (:app/app @db) [(j/get e :x) (j/get e :y)])]
    (ws/send! (:ws/client @db) [:player/move-intent world-coords])))

(defn add-background! [app]
  (let [obj (pixi/Graphics.)]
    (j/call obj :beginFill 0x8c9d3f)
    (j/call obj :drawRect 0 0 (app/get-width app) (app/get-height app))
    (on-click! obj on-click)
    (app/add-child app obj)))

(defn add-player! [db app player]
  ;; TODO implement asset loader
  (let [sprite (sprite/make "/img/1_IDLE_000.png")
        pos (:player/pos player)]
    (.log js/console :adding-player player)
    (sprite/set-anchor! sprite 0.5)
    (sprite/set-pos! sprite (world/->pixel-coords app pos))
    (swap! db assoc-in [:app/sprites (:player/id player)] sprite)
    (app/add-child app sprite)
    sprite))

(defn reconcile-playerbase! [db]
  (let [{:keys [:db/player-id :db/players :app/sprites]} @db
        backend-ids (set (keys players))
        client-ids (set (keys sprites))]
    ;; Mounted new players
    (doseq [player-id (set/difference
                       backend-ids
                       client-ids)]
      (add-player! db (:app/app @db) (get players player-id)))

    ;; Unmount exited players
    (doseq [player-id (set/difference
                       client-ids
                       (conj backend-ids player-id))]
      (.log js/console :removing-player player-id)
      (app/remove-child (:app/app @db) (get sprites player-id))
      (swap! db update :app/sprites dissoc player-id))))

(defn make-update-fn [app timer]
  (fn [dt]
    (let [{:keys [:db/players :app/sprites]} @db]
      (when (timer/throttled? timer ::reconcile-playerbase)
        (reconcile-playerbase! db))

      (doseq [{:player/keys [speed id]
               target-pos :player/pos} (vals players)]
        (let [sprite (get sprites id)
              current-pos (world/->world-coords app (sprite/get-pos sprite))]
          (when sprite
            (when-not (= current-pos target-pos)
              (let [diff (vec/- target-pos current-pos)
                    distance (vec/length diff)
                    dir (vec/normalize (vec/div diff distance))
                    step (* 0.0038 dt)

                    new-pos (if (> step distance)
                              target-pos
                              (vec/+ current-pos (vec/* dir step)))]
                (sprite/set-pos!
                 sprite (world/->pixel-coords app new-pos)))))))

      (timer/tick timer (app/last-time app)))))

(defn mount! [app]
  (add-background! app)
  (add-player! db app {:player/id (:db/player-id @db)})

  (let [timer (timer/start 0)
        update-fn (make-update-fn app timer)]
    (timer/add-throttle! timer ::reconcile-playerbase 1000)
    (app/add-update-fn! app update-fn)
    #(app/remove-update-fn! app update-fn)))

(defn socket-message-handler
  [{:keys [event id ?data] :as ws-client}]
  (when-not (= id :player/update-all)
    (.log js/console :received-msg id ?data))
  (case id
    :chsk/handshake
    (when (true? (nth ?data 3))
      (ws/send! ws-client [:player/joined {:player/id (:uid @(:state ws-client))}]))
    :player/update-all
    ;; TODO make sente transit (but transform for now)
    (swap! db assoc :db/players (->> (m/map-keys uuid ?data)
                                             (m/map-vals #(update % :player/id uuid))))
    (.log js/console :ws/unknown-event event)))

(defn ^:dev/after-load refresh! []
  (app/clear! (:app/app @db))
  (when-let [cleanup (:dev/cleanup @db)]
    (cleanup))
  (swap! db assoc :dev/cleanup (mount! (:app/app @db))))

(defn main []
  (let [app (app/new {:on-resize refresh!})
        ws-client (ws/connect! (:db/player-id @db))]
    (swap! db assoc
           :ws/client ws-client
           :app/app app)
    (ws/start-listener! ws-client #(socket-message-handler %))
    (swap! db assoc :dev/cleanup (mount! app))))

(comment
  (dissoc @db :ws/client)
  (:app/sprites @db)
  (m/map-keys uuid
              {"71a7ac6a-762e-4192-a084-d0fe6a7f609f"
               {:player/id "71a7ac6a-762e-4192-a084-d0fe6a7f609f",
                :player/pos [1 2]
                :player/speed 1.38}})


  )
