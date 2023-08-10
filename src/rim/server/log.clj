(ns rim.server.log
  (:require [rim.util.ansi :as ansi])
  (:import java.text.DateFormat java.util.Date))

(defn- format-date []
  (.format (DateFormat/getTimeInstance) (Date.)))

(defn- logger [type]
  (fn [& [component & args]]
    (apply println (ansi/gray (format-date)) type
           (ansi/bold (ansi/green component)) args)))

(def error (logger (ansi/red "[error]")))
(def warn (logger (ansi/yellow "[warning]")))
(def debug (logger (ansi/yellow "[debug]")))
(def info (logger (ansi/gray "[info]")))
