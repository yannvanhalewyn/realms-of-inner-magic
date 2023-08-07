(ns wg.ticker
  "https://pixijs.download/release/docs/PIXI.Ticker.html"
  (:require
   ["pixi.js" :as pixi]
   [applied-science.js-interop :as j]))

(defn last-time []
  (j/get-in pixi/Ticker [:shared :lastTime]))

(defn start! []
  (j/call-in pixi/Ticker [:shared :start]))

(defn tick! [t]
  (j/call-in pixi/Ticker [:shared :update] t))
