(ns dev
  (:require
   [sc.api]
   [clojure.core.async :as async]
   [wg.server.main :as main]
   [wg.server.ws :as ws]
   [clojure.string :as str]
   [rim.server.log :as log]
   [clojure.tools.namespace.repl :as tools.ns.repl]
   [com.biffweb :as biff]
   [com.biffweb.impl.xtdb :as biff.xt]
   [malli.core :as malli]
   [xtdb.api :as xt]))

(set! *warn-on-reflection* true)

(def start #'main/-main)

(def system main/system)

(defn refresh []
  (doseq [f (:biff/stop @system)]
    (log/info :dev/stopping (str f))
    (f))
  (tools.ns.repl/refresh :after `start))

(defn reset []
  (doseq [f (:biff/stop @system)]
    (log/info :dev/stopping (str f))
    (f))
  (start))

(defn get-ctx []
  (biff/assoc-db @system))

(defn get-node []
  (:biff.xtdb/node @system))

(defn get-db []
  (:biff/db (get-ctx)))

(defn q! [& args]
  (apply biff/q (get-db) args))

(defn submit-tx [tx]
  (biff/submit-tx (get-ctx) tx))

(defn with-tx [tx]
  (xt/with-tx (get-db) tx))

(defn biff-tx->xt [tx]
  (biff.xt/biff-tx->xt (get-ctx) tx))

(defn lookup [k v]
  (biff/lookup (get-db) k v))

(defn clear-db! []
  (xt/submit-tx
   (get-node)
   (for [id (com.biffweb/q (get-db) '{:find ?e :where [[?e :xt/id ?a]]})]
     [::xt/delete id])))

(defn valid? [doc-type doc]
  (malli/validate doc-type doc @(:biff/malli-opts @system)))

(defn explain [doc-type doc]
  (malli/explain doc-type doc @(:biff/malli-opts @system)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; WS / Async

(defn ws-server []
  (::ws/server @system))

(defn ws-broadcast! [msg]
  (ws/broadcast! (ws-server) msg))

(defn ws-send! [server user-id msg]
  ((:send-fn (ws-server)) #uuid "26849503-3209-4f07-bff9-b8756c97abf6" [:message/topic "hello!"])
  )

(comment
  (ws-broadcast! [:my/topic "how you doin"])
  (ws-send! #uuid "216bd1c6-2a2c-4242-84a5-82683e106abc" [:my/topic "how you doin"])
  (:connected-uids (ws-server))

  )
