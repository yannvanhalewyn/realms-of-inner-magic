(ns wg.server.api
  (:require
   [wg.server.static :as static]))

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

(def plugin
  {:routes [["/" {:get index}]]
   :api-routes [["/ws" {:post ws}]
                ["/api/pub" {:post publish}]
                ["/api/sub" {:post subscribe}]]})
