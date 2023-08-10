(ns wg.server.game
  (:require
   [rim.util.async :as util.async]
   [wg.server.ws :as ws]
   [rim.server.log :as log]))

;; Broadcast player data every ms
(def BROADCAST_INTERVAL 100)

(defonce world (atom {:players {}}))

(defn handle-client-msg [{:keys [event id ?data] :as ws}]
  (log/info :game/client-msg event)
  (case id
    :chsk/uidport-close
    (swap! world update :players dissoc ?data)
    :player/joined
    (let [player (assoc ?data
                   :player/pos [0 0]
                   :player/speed 1.38)]
      (swap! world assoc-in [:players (:player/id player)] player)
      (ws/broadcast-others! ws [:player/joined player]))
    (log/info :game/client-msg (str "Unhandled message from client" id))))

(defn broadcast! [ws-server]
  (ws/broadcast! ws-server [:player/update-all (:players @world)]))

(defn use-broadcaster
  "Depends on ws/use-listener in order to get the ::ws/server from the context"
  [ctx]
  (assert (::ws/server ctx) "No ws-server in biff context")
  (let [stop (util.async/interval BROADCAST_INTERVAL
                                  #(broadcast! (::ws/server ctx)))]
    (update ctx :biff/stop conj (fn []
                                  (stop)
                                  (reset! world {:players {}})))))
