(ns consulate.handler
  (:require [compojure.core :refer [GET defroutes routes context]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [api-defaults site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-params wrap-json-body]]
            [ring.util.response :refer [response]]
            [selmer.parser :refer [render-file]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]))

(defn get-leader [request]
  (response {:body "10.1.10.12:8300"} ))

(defn get-peers [request]
  (response {:body ["10.1.10.12:8300",
                    "10.1.10.11:8300",
                    "10.1.10.10:8300"]}))

(defn get-key [request]
  (response {:body [
                    {
                     :CreateIndex 100,
                     :ModifyIndex 200,
                     :LockIndex 200,
                     :Key "zip",
                     :Flags 0,
                     :Value "dGVzdA==",
                     :Session "adf4238a-882b-9ddc-4a9d-5b6758e4159e"
                     }
                    ]}))
;;...... ?recurse , ?keys , ?raw
;; put key, body
;; delete key

;; catalog
;; register
;; deregister
;; datacenters
;; nodes
;; services
;; service/<service>
;; nodes/<node>

;; events
;; fire/<name>
;; list

;; health...


(defroutes api-routes
  (context "/api" []
    (context "/v1" []
      (context "/status" []
        (GET "/leader" [] get-leader)
        (GET "/peers" [] get-peers))
      (context "/kv" []
        (GET "/:key" [] get-key)))))

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
