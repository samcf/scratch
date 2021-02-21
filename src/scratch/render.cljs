(ns scratch.render)

(defn main []
  (println "Hello world"))

(defonce _
  (do (enable-console-print!)
      (.addEventListener js/window "DOMContentLoaded" main)))
