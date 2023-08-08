(ns wg.broker
  (:require [clojure.core.async :as async]))

(defn make-publisher []
  (let [ch (async/chan 1)]
    {::ch ch ::pub (async/pub ch ::tag)}))

(defn close-publisher! [publisher]
  (async/close! (::ch publisher)))

(defn subscribe!
  ([publisher topic]
   (let [ch (async/chan 1)]
     (async/sub (::pub publisher) topic ch)
     ch))
  ([publisher topic cb]
   (let [ch (subscribe! publisher topic)]
     (async/go-loop []
       (when-let [value (async/<! ch)]
         (cb value)
         (recur)))
     ch)))

(defn unsubscribe! [subscription]
  (async/close! subscription))

(defn publish! [publisher tag msg]
  (async/>!! (::ch publisher) {::tag tag ::msg msg}))

(comment
  (def publisher (make-publisher))


  (def subscription (subscribe! publisher :player-activity
                                (fn [msg]
                                  (println "Other subscriber" msg))))

  (publish! publisher :player-activity [10 10])

  (unsubscribe! subscription)

  (close-publisher! publisher)

  )
