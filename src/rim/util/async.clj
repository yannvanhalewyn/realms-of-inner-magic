(ns rim.util.async
  (:require [clojure.core.async :as async]))

(defn interval [ms callback]
  (let [kill-ch (async/chan)]
    (async/go-loop []
      (let [[_ c] (async/alts! [(async/timeout ms) kill-ch])]
        (callback)
        (when-not (= c kill-ch)
          (recur))))
    #(async/close! kill-ch)))

(comment
  (def job (set-interval #(println "JOB") 1000))
  (async/close! job)

  (def kill (interval 15 #(println "ping")))

  (kill)


  )
