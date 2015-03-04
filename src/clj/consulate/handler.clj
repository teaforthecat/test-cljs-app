(ns consulate.handler
  (:require [compojure.core :refer [GET defroutes routes context]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [api-defaults site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params wrap-json-body]]
            [ring.util.response :refer [response]]
            [selmer.parser :refer [render-file]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]))

(defn say-hi [request]
  (response {:body "hello world"}))

(defroutes api-routes
  (context "/api" []
    (GET "/say" [] say-hi)))

(defroutes site-routes
  (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
  (resources "/")
  (not-found "Not Found"))

(def app-handler
  (routes
   (-> api-routes
       wrap-json-params
       wrap-json-body
       wrap-json-response
       (wrap-defaults api-defaults))
   (wrap-defaults site-routes site-defaults)))

(def app
  (let [handler app-handler]
    (if (env :dev?) (wrap-exceptions handler) handler)))
