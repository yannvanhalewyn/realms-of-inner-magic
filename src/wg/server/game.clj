(ns wg.server.game
  (:require
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
