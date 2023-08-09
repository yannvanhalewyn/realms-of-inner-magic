(ns wg.client.ws
  (:require
   [applied-science.js-interop :as j]
   [clojure.core.async :as async]
   [taoensso.sente :as sente]))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client!
       "/ws" ; Note the same path as before
       nil
       {:type :auto ; e/o #{:auto :ajax :ws}
       })]

  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )
