(ns consulate.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react])
    (:import goog.History))

;; -------------------------
;; Views

(def hostname-re (re-pattern "([a-z]{1,})-([a-z][a-z-]*[a-z]+)([0-9]+)?-?([0-9]+)?[-.]([a-z]{2,6})([.-][a-z]{2,6})?(?:.gdi)?"))

(defrecord Server [hostname environment hostgroup number cluster location ] )

(defn new-server [hostname]
  (apply ->Server (re-matches hostname-re hostname)))

(def test-list-of-servers ["qc-evoweb1.ep.gdi"] )

(defn home-page []
  [:div [:h2 "Welcome to consulate"]
   [:div [:a {:href "#/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About consulate"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

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
(defn init! []
  (hook-browser-navigation!)
  (reagent/render-component [current-page] (.getElementById js/document "app")))
