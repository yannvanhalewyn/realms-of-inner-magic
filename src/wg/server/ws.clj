(ns wg.server.ws
  (:require
   [clojure.core.async :as async]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

(defn make-server! [opts]
  (sente/make-channel-socket-server! (get-sch-adapter) opts))

(defn close-server! [ws-server]
  (async/close! (:ch-recv ws-server)))

(defn send! [{:keys [send-fn]} uid event]
  (send-fn uid event))

(defn broadcast! [{:keys [connected-uids send-fn]} msg]
  (doseq [uid (:any @connected-uids)]
    (send-fn uid msg)))

(defn start-listener! [{:keys [ch-recv]} handler]
  (async/go-loop []
    ;; Needs recur
    (when-let [msg (async/<! ch-recv)]
      (handler msg)
      (recur))))

(defn use-listener [{:keys [:chsk/server :chsk/handler] :as ctx}]
  (let [server (make-server! server)
        listener (start-listener! server handler)]
    (-> ctx
        (assoc ::server server)
        ;; Maybe close the ch-recv instead
        (update :biff/stop conj #(async/close! listener))
        (update :biff/stop conj #(close-server! server)))))

(defn http-get [{::keys [server] :as req}]
  ((:ajax-get-or-ws-handshake-fn server) req))

(defn http-post [{::keys [server] :as req}]
  ((:ajax-post-fn server) req))
