(ns consulate.server
  (:require [consulate.handler :refer [app]]
            [org.httpkit.server :refer [run-server]]
            ;[ring.adapter.jetty :refer [run-jetty]]
            )
  (:gen-class))

 ;; (defn -main [& args]
 ;;   (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
 ;;     (run-jetty app {:port port :join? false})))

(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (run-server app {:port port})))
