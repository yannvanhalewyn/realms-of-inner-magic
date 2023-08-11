(ns wg.server.game
  (:require
   [rim.util.async :as util.async]
   [wg.server.ws :as ws]
   [rim.server.log :as log]))

;; Broadcast player data every ms
(def BROADCAST_INTERVAL 100)

(def world (atom {::players {}}))

(defn player-joined [db player]
  (assoc-in db [::players (:player/id player)]
            (assoc player
              :player/pos [0 0]
              :player/speed 1.38)))

(defn player-left [db player-id]
  (update db ::players dissoc player-id))

(defn player-move-intent [db player-id target-pos]
  ;; Recalculate pos every frame? I may need a game loop on server.
  ;; For now the player teleports and the client animates, which is fine until
  ;; we need collision.
  (assoc-in db [::players player-id :player/pos] target-pos))

(defn handle-client-msg [{:keys [event id ?data uid] :as ws}]
  (log/info :game/client-msg event)
  (case id
    :chsk/uidport-close (swap! world player-left ?data)
    :player/joined      (swap! world player-joined ?data)
    :player/move-intent (swap! world player-move-intent uid ?data)
    (log/info :game/client-msg (str "Unhandled message from client" id))))

(defn broadcast! [ws-server]
  (ws/broadcast! ws-server [:player/update-all (::players @world)]))

(defn use-broadcaster
  "Depends on ws/use-listener in order to get the ::ws/server from the context"
  [ctx]
  (assert (::ws/server ctx) "No ws-server in biff context")
  (let [stop (util.async/interval BROADCAST_INTERVAL
                                  #(broadcast! (::ws/server ctx)))]
    (update ctx :biff/stop conj (fn []
                                  (stop)
                                  (reset! world {::players {}})))))
