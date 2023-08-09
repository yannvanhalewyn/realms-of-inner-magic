(ns wg.server.main
  (:require [com.biffweb :as biff]
            [com.biffweb.impl.middleware :as biff.middleware]
            [clojure.tools.logging :as log]
            [malli.core :as malc]
            [malli.registry :as malr]
            [wg.server.api :as api]
            [wg.server.ws :as ws]
            [org.httpkit.server :as http-server]
            [clojure.java.io :as io]))

(def plugins
  [(biff/authentication-plugin {})
   api/plugin])

(def routes [["" {:middleware [biff/wrap-site-defaults]}
              (keep :routes plugins)]
             ["" {:middleware [biff/wrap-api-defaults]}
              (keep :api-routes plugins)]])

(def handler (-> (biff/reitit-handler {:routes routes})
                 (biff/wrap-base-defaults)))

(defn on-save [ctx]
  (biff/add-libs))

(def malli-opts
  {:registry (malr/composite-registry
              malc/default-registry
              (apply biff/safe-merge
                     (keep :schema plugins)))})

(defn send-email [ctx opts]
  (println "Email:" opts))

(def initial-system
  {:biff/plugins #'plugins
   :biff/send-email #'send-email
   :biff/handler #'handler
   :biff/malli-opts #'malli-opts
   :biff.beholder/on-save #'on-save
   :biff.xtdb/tx-fns (->> (conj (keep :tx-fns plugins) biff/tx-fns)
                          (apply biff/safe-merge))
   :chsk/server {:csrf-token-fn nil
                 :user-id-fn (fn [_req] (random-uuid))}
   :chsk/handler (fn [event]
                   (println "CHSK EVENT" (keys event)))})

(defonce system (atom {}))

(def get-secret
  (let [file (io/file "secrets.edn")
        secrets (delay (when (.exists file) (read-string (slurp file))))]
    (fn [ctx k]
      (get @secrets (get ctx k)))))

(defn use-secrets [ctx]
  (when-not (every? #(get-secret ctx %) [:biff.middleware/cookie-secret :biff/jwt-secret])
    (binding [*out* *err*]
      (println "Secrets are missing, add a secrets.edn")))
  (assoc ctx :biff/secret #(get-secret ctx %)))

(defn use-http-kit [{:biff/keys [host port handler]
                     :or {host "localhost" port 8080}
                     :as ctx}]
  (let [server (http-server/run-server handler {:host host :port port})]
    (log/info "Server running on" (str "http://" host ":" port))
    (update ctx :biff/stop conj server)))

(def components
  [biff/use-config
   use-secrets
   biff/use-xt
   biff/use-queues
   biff/use-tx-listener
   ;; Must be before use-wrap-ctx for handlers to have access to ws-server
   ws/use-listener
   biff.middleware/use-wrap-ctx
   use-http-kit
   biff/use-chime
   biff/use-beholder])

(defn -main [& args]
  (let [new-system (reduce (fn [system component]
                             (log/info "starting:" (str component))
                             (component system))
                           initial-system
                           components)]
    (reset! system new-system)
    (log/info "Go to" (:biff/base-url new-system))))
