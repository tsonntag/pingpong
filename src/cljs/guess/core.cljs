(ns guess.core
    (:require
      [guess.pingpong :as pingpong]
      [guess.lib :refer [link-to]]
      [reagent.core :as reagent :refer [atom]]
      [reagent.session :as session]
      [secretary.core :as secretary :include-macros true]
      [goog.events :as events]
      [goog.history.EventType :as EventType]
      [cljsjs.react :as react])
    (:import goog.History))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome"]
   [:div (link-to "#/about" "about")]
   [:div (link-to "#/pingpong" "ping pong")]
  ])

(defn about-page []
  [:div [:h2 "About guess"]
   [:div (link-to "#/" "home")]])

(defn forms-page []
  [:div [:h2 "Forms"]
   [:input.form-control {:field :text :id :first-name}]
   [:div (link-to "#/" "home")]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/pingpong" []
  (session/put! :current-page #'pingpong/page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
