(ns wg.server.api
  (:require
   [wg.server.static :as static]
   [ring.util.anti-forgery]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

(defn- subscribe [{:keys [biff/db params]}]
  {:status 200
   :body {:message "Subscribe"}})

(defn- publish [{:keys [params] :as ctx}]
  {:status 200
   :body {:message "Publish"}})

(defn ws [{:keys [params]}]
  {:status 200
   :body {:message "ws"}})

(defn index [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (static/index)})

(def chsk-server (sente/make-channel-socket-server! (get-sch-adapter) {:csrf-token-fn nil}))

;; (def ch-chsk        (:ch-recv chsk-server))        ; ChannelSocket's receive channel
;; (def chsk-send!     (:send-fn chsk-server))        ; ChannelSocket's send API fn
;; (def connected-uids (:connected-uids chsk-server)) ; Watchable, read-only atom

(def plugin
  {:routes [["/" {:get index}]]
   :api-routes [["/ws" {:get (fn [req]
                               (println "IN REQ" (get-in req  [:headers "x-csrf-token"]))
                               (let [res ((:ajax-get-or-ws-handshake-fn chsk-server) req)]

                                 (sc.api/spy :req)
                                 res))
                        :post (:ajax-post-fn chsk-server)}]
                ["/api/pub" {:post publish}]
                ["/api/sub" {:post subscribe}]]})
