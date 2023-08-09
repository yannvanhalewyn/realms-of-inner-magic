(ns wg.server.static
  "Utility functions for inserting anti-forgery tokens into HTML forms."
  (:require [hiccup.core :as h]
            [hiccup.page :as hp]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(defn- html-head []
  [:head
   [:title "Realms of Inner Magic"]
   [:meta {:charset "UTF-8"}]
   [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:style {:type "text/css" :media "screen"}
    "
     body {
         margin:0;
         padding:0;
         overflow:hidden;
     }

     canvas {
         display:block;
     }"]])

(defn index
  "Create a hidden field with the session anti-forgery token as its value.
  This ensures that the form it's inside won't be stopped by the anti-forgery
  middleware."
  []
  (hp/html5
   {:lang "en"}
   (html-head)
   [:body
    [:div {:id "app"}]
    [:noscript "You need to enable JavaScript to run this app."]
    [:script {:src "/js/main.js"}]]))
