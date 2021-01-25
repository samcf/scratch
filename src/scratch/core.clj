(ns scratch.core
  (:require [io.pedestal.http :as http]
            [hiccup.core :as hiccup]))

(defn index [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (hiccup/html
          [:html
           [:head
            [:title "Scratch"]]
           [:body
            [:div {:id "scratch"}]]])})

(def service
  {:env :prod
   ::http/join? false
   ::http/routes #{["/" :get index :route-name :index]}
   ::http/resource-path "/public"
   ::http/type :jetty
   ::http/host "0.0.0.0"
   ::http/port (let [port (or (System/getenv "PORT") "8080")]
                 (Integer/parseInt port 10))})

(defonce server (atom nil))

(defn server-start []
  (let [srvc (assoc service ::http/join? false :env :dev)
        srvr (http/create-server srvc)
        strt (http/start srvr)]
    (reset! server strt)))

(defn server-stop []
  (when (some? @server)
    (http/stop @server)))

(defn server-restart []
  (server-stop)
  (server-start))

(defn -main []
  (server-start))
