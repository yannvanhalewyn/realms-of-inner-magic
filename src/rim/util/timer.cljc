(ns rim.util.timer
  (:require [medley.core :as m]
            [sc.api]))

(defn get-time
  "Returns the passed time since the GL context launched in seconds"
  []
  #?(:clj nil
     :cljs (.getTime (js/Date.))))

(defn start
  ([]
   (start (get-time)))
  ([curr-time]
   (atom {::prev-time (- curr-time 1)
          ::curr-time curr-time})))

(defn elapsed [timer]
  (let [{::keys [prev-time curr-time]} @timer]
    (- curr-time prev-time)))

(defn fps [timer]
  (/ 1 (elapsed timer)))

(defn- update-throttles
  [throttles curr-time]
  (m/map-vals
   (fn [{::keys [threshold last-hit] :as throttle}]
     ;; (println (- curr-time last-hit))
     (if (>= (- curr-time last-hit) threshold)
       (assoc throttle ::last-hit curr-time)
       throttle))
   throttles))

(defn add-throttle!
  "Adds a throttle marker to the timer. This is useful if you want to use the
  timer to execute something once for every time period.

  See `throttled?` below."
  [timer k threshold]
  (swap! timer assoc-in [::throttles k]
         {::threshold threshold
          ::last-hit (::curr-time @timer)}))

(defn throttled?
  "Checks if the current frame is the one where the time got throttled. Must be
  set up with `add-throttle!` before use.

  Can be used to have a quick dev throttle without providing a throttle. It will
  then pick a random configured throttle."
  ([timer]
   (throttled? timer (key (first (::throttles @timer)))))
  ([timer k]
   (let [{::keys [throttles curr-time]} @timer]
     ;; (println curr-time (::last-hit (get throttles k)))
     (= (::last-hit (get throttles k)) curr-time))))

(defn tick
  ([timer]
   (tick timer (get-time)))
  ([timer curr-time]
   ;; (.log js/console :tick curr-time)
   (swap! timer
          (fn [state]
            (assoc state
              ::prev-time (::curr-time state)
              ::curr-time curr-time
              ::throttles (update-throttles (::throttles state) curr-time))))))
