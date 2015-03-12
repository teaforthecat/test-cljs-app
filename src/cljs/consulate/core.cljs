(ns consulate.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [reagent-forms.core :refer [bind-fields]]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [ajax.core :refer [GET POST]]
              [cljsjs.react :as react])
    (:import goog.History))

;; -------------------------
;; Views
(defn handler [response]
  (.log js/console (str response)))
(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(def hostname-re (re-pattern "([a-z]{1,})-([a-z][a-z-]*[a-z]+)([0-9]+)?-?([0-9]+)?[-.]([a-z]{2,6})([.-][a-z]{2,6})?(?:.gdi)?"))

(defrecord Server [hostname environment hostgroup number cluster location ] )

(defn new-server [hostname]
  (handler hostname)
  (apply ->Server (re-matches hostname-re hostname)))

(def test-list-of-servers ["qc-evoweb1.ep.gdi"] )

(defn post-hostname [hostname]
  (ajax.core.POST "/api/v1/hostname"
        {:params {:hostname hostname}
         :handler handler
         :error-handler error-handler}))

(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

(defn radio [label name value]
  [:div.radio
   [:label
    [:input {:field :radio :name name :value value}]
    label]])

(defn input [label type id]
  (row label [:input.form-control {:field type :id id}]))

(def hostname-fields
  [:div
   (row "hostname"
        [:input.form-control {:field :text :id :entered_hostname}])
   (row "hostname"
        [:input.form-control {:field :text :id :hostname}])
   (row "environment"
        [:input.form-control {:field :text :id :environment}])
   (row "hostgroup"
        [:input.form-control {:field :text :id :hostgroup}])
   (row "number"
        [:input.form-control {:field :text :id :number}])
   (row "cluster"
        [:input.form-control {:field :text :id :cluster}])
   (row "location"
        [:input.form-control {:field :text :id :location}])])

(def current-hostname (atom {}))

(def form-template
  [:div
   ;; (input "hostname" :text :hostname.hostname)
   [:label (str @current-hostname)]
;   [:input.form-control {:field :text :id :hostname.hostname} ]
   [:div.row
    [:div.col-md-2]
    [:div.col-md-5
     [:div.alert.alert-danger
      {:field :alert :id :errors.hostname}]]]])


(defn handle-hostname-change-handler [value]
  (let [results (or (new-server value) {})]
    (assoc results :entered_hostname value)))

(defn handle-hostname-change [[id] value]
  (if (= id :entered_hostname)
    (swap! current-hostname #(handle-hostname-change-handler value))))

(defn form [doc & body]
  [:div.row
   body
   ])

(defn home-page []
  (let [doc (atom {:hostname "test-evoweb1.ep.gdi"})]
    [:div [:h2 "Welcome to consulate"]
     [:div [:a {:href "#/about"} "go to about page"]]
     ;; form-template
     [form doc
      [bind-fields hostname-fields doc handle-hostname-change]]]))

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
