(ns dev
  (:require
   [sc.api]
   [wg.server.main :as main]
   [wg.server.ws :as ws]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [clojure.tools.namespace.repl :as tools.ns.repl]
   [com.biffweb :as biff]
   [com.biffweb.impl.xtdb :as biff.xt]
   [malli.core :as malli]
   [xtdb.api :as xt]))

(def start #'main/-main)

(def system main/system)

(defn refresh []
  (doseq [f (:biff/stop @system)]
    (log/info "stopping:" (str f))
    (f))
  (tools.ns.repl/refresh :after `start))

(defn reset []
  (doseq [f (:biff/stop @system)]
    (log/info "stopping:" (str f))
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

(defn ws-server []
  (::ws/server @system))

(defn ws-broadcast! [msg]
  (ws/broadcast! (ws-server) msg))
