(ns scratch.render
  (:require [rum.core :as rum]
            [scratch.board :as board]
            [scratch.slurp :include-macros true :refer [slurp]]
            [clojure.core.async :refer [go <! timeout]]))

(enable-console-print!)

(defn handler [f & args]
  (fn [_] (apply f args)))

(def initial-state
  {:fealty :crosses
   :board  (board/create)})

(defmulti event-handler (fn [_ args] (first args)))

(defmethod event-handler :play [[state update!] [_ square]]
  (let [{:keys [board fealty]} state
        [legal? status]        (board/check board fealty square)]
    (if (and (not legal?) (#{:victory :scratch} status))
      (update! initial-state)
      (when legal?
        (let [next (board/play board square)]
          (update! (assoc state :board next))
          (when (= status :playing)
            (go
              (<! (timeout (+ (rand-int 600) 200)))
              (let [best       (board/best next)
                    [legal? _] (board/check next (board/opponent fealty) best)]
                (when legal?
                  (update! (assoc state :board (board/play next best))))))))))))

(rum/defcontext Scratch)

(rum/defc provider [children]
  (let [[state set-state] (rum/use-state initial-state)]
    (let [dispatch (fn [& args] (event-handler [state set-state] args))]
      (rum/bind-context [Scratch [state dispatch]] children))))

(rum/defc status [side]
  (rum/with-context [[state _] Scratch]
    (let [fealty (:fealty state)
          board  (:board state)
          next   (board/turn board)
          prev   (board/opponent next)
          status (board/state board)]
      (rum/fragment
       [:div
        [:strong (str
                  (case side :crosses "Crosses" :circles "Circles")
                  (if (= side fealty) " (You)" ""))]]
       [:div
        [:em (case status
               :playing (if (= side next) "Playing" "Waiting")
               :victory (if (= side prev) "Victor!" "Defeated!")
               :scratch "Scratch!")]]))))

(rum/defc header []
  (rum/with-context [[state _] Scratch]
    (let [{:keys [board fealty]} state
          turn                   (board/turn board)]
      [:div.statuses
       [:div.status (status :crosses)]
       [:div.status (status :circles)]])))

(rum/defc board []
  (rum/with-context [[state dispatch] Scratch]
    (let [{:keys [board]} state]
      (when (not (nil? board))
        [:div.board
         (for [square board/squares]
           [:div {:key square :on-click (handler dispatch :play square)}
            (case (board/occupying board square)
              :crosses "X"
              :circles "O"
              "")])]))))

(rum/defc root []
  (provider
   (rum/with-context [[state] Scratch]
     [:div.layout
      [:div.column
       [:div.header (header)]
       [:div.game (board)]]
      [:div.column
       [:h2 "Source Code"]
       [:pre (slurp)]]])))

(defn main []
  (let [element (.querySelector js/document "#root")]
    (-> (root) (rum/mount element))))

(main)
