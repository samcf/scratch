(ns scratch.core
  (:require [io.pedestal.http :as http]
            [hiccup.core :as hiccup]))

(defonce server (atom nil))
(defonce config (atom {:env :dev}))

(defn index [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (hiccup/html [:html
                          [:head [:title "Scratch"]]
                          [:body [:div {:id "scratch"}]]])})

(defn service-routes []
  #{["/" :get index :route-name :index]})

(defn create-service []
  (let [port (System/getenv "PORT") env (:env @config)]
    {:env (if (= env :dev) :dev :prod)
     ::http/resource-path "/public"
     ::http/routes (service-routes)
     ::http/join? (not= env :dev)
     ::http/type :jetty
     ::http/host (if (= env :dev) "localhost" "0.0.0.0")
     ::http/port (if (string? port) (Integer/parseInt port 10) 8080)}))

(defn server-start []
  (when-not (nil? @server)
    (http/stop @server))

  (let [service (-> (create-service) (http/create-server) (http/start))]
    (reset! server service)))

(defn start [options]
  (reset! config options)
  (server-start))

(server-start)
