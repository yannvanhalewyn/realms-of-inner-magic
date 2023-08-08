(ns wg.server.api
  (:require
   [com.biffweb :as biff]))

(defn- subscribe [{:keys [biff/db params]}]
  {:status 200
   :body {:message "Subscribe"}})

(defn- publish [{:keys [params] :as ctx}]
  {:status 200
   :body {:message "Publish"}})

(defn ws [{:keys [params]}]
  {:status 200
   :body {:message "ws"}})

(def plugin
  {:api-routes [["/ws" {:post ws}]
                ["/api/pub" {:post publish}]
                ["/api/sub" {:post subscribe}]]})
