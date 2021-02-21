(ns scratch.server
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [io.pedestal.http :as http]
   [hiccup.core]))

(defonce server (atom nil))
(defonce config (atom {:env :dev}))

(def cli-opts
  [["-h" "--help"]
   ["-e" "--env" "Environment"
    :default :prod
    :parse-fn (fn [env] (if (= env "dev") :dev :prod))]])

(defn render-index []
  (hiccup.core/html
   [:html
    [:head
     [:title "play tic-tac-toe, i guess"]
     [:script {:src  "script/scratch.js" :type "text/javascript"}]]
    [:body [:div#scratch]]]))

(defn index [_]
  {:status 200 :body (render-index)})

(defn service-routes []
  #{["/" :get [http/html-body index] :route-name :index]})

(defn create-service []
  (let [port (System/getenv "PORT")
        env  (:env @config)]
    {:env                  (if (= env :dev) :dev :prod)
     ::http/resource-path  "/public"
     ::http/routes         (service-routes)
     ::http/join?          (not= env :dev)
     ::http/type           :jetty
     ::http/secure-headers {:content-security-policy-settings "default-src 'self'"}
     ::http/host           (if (= env :dev) "localhost" "0.0.0.0")
     ::http/port           (if (string? port) (Integer/parseInt port 10) 8080)}))

(defn server-start []
  (when-not (nil? @server)
    (http/stop @server))

  (let [service (-> (create-service) (http/create-server) (http/start))]
    (reset! server service)))

(defn -main [& args]
  (let [result (parse-opts args cli-opts)]
    (if (:errors result)
      (println (first (:errors result)))
      (if (:help (:options result))
        (println (str "clojure -M:start-server [options]" "\n" (:summary result)))
        (do
          (reset! config (:options result))
          (server-start))))))
