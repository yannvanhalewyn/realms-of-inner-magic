(ns wg.server.game
  (:require
   [rim.util.async :as util.async]
   [wg.server.ws :as ws]
   [rim.server.log :as log]
   [medley.core :as m]))

(defonce world (atom {:players {}}))

(defn handle-client-msg [msg]
  (log/info :game/client-msg (:event msg))
  (case (:id msg)
    :chsk/uidport-close
    (swap! world m/dissoc-in [:players (:?data msg)])
    :player/joined
    (let [player (:?data msg)]
      (swap! world assoc-in [:players (:player/id player)] player))
    (log/info :game/client-msg (str "Unhandled message from client" (:id msg)))))

(defn broadcast! [ws-server]
  (ws/broadcast! ws-server [:player/update-all (:players @world)]))

(defn use-broadcaster
  "Depends on ws/use-listener in order to get the ::ws/server from the context"
  [ctx]
  (assert (::ws/server ctx) "No ws-server in biff context")
  (let [stop (util.async/interval 1000 #(broadcast! (::ws/server ctx)))]
    (update ctx :biff/stop conj stop)))
