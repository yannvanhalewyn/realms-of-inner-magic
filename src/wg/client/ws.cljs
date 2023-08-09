(ns wg.client.ws
  (:require
   [applied-science.js-interop :as j]
   [clojure.core.async :as async]
   [taoensso.sente :as sente]))

(defn connect! []
  (sente/make-channel-socket-client! "/ws" nil {:type :auto}))

(defn send! [{:keys [send-fn] :as ws-client} event]
  (send-fn event 8000
           #(if (sente/cb-success? %)
              (.log js/console "ws-send:success" %)
              (.log js/console "ws-send:failure" %))))

(defn start-listener! [{:keys [ch-recv]} handler]
  (async/go-loop []
    (when-let [msg (async/<! ch-recv)]
      (handler (async/<! ch-recv))
      (recur))))

(comment
  (send! (:ws/client @wg.core/db) [:player/joined {}])

  )
