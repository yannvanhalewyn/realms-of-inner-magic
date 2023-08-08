(ns wg.server.main
  (:require [com.biffweb :as biff]
            [clojure.tools.logging :as log]
            [malli.core :as malc]
            [malli.registry :as malr]
            [wg.server.api :as api]
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
  (biff/add-libs)
  (biff/eval-files! ctx))

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
                          (apply biff/safe-merge))})

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

(def components
  [biff/use-config
   use-secrets
   biff/use-xt
   biff/use-queues
   biff/use-tx-listener
   biff/use-jetty
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
