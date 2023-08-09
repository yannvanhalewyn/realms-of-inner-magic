(ns wg.server.api
  (:require
   [wg.server.static :as static]
   [wg.server.ws :as ws]
   [ring.util.anti-forgery]
   [clojure.core.async :as async]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

(defn index [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (static/index)})

(def plugin
  {:routes [["/" {:get index}]]
   :api-routes [["/ws" {:get ws/http-get
                        :post ws/http-post}]]})
