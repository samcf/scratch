(ns scratch.render
  (:require [rum.core :as rum]
            [scratch.game :as game :refer [State]]))

(enable-console-print!)

(def squares [0x100 0x80 0x40 0x20 0x10 0x8 0x4 0x2 0x1])

(defn handler [f & args]
  (fn [event] (apply f args)))

(defn occupying [state square]
  (let [{:keys [crosses circles]} state]
    (cond
      (= square (bit-and square crosses)) :crosses
      (= square (bit-and square circles)) :circles
      :else :neither)))

(rum/defc board [state on-move]
  [:div.board
   (for [square squares]
     (let [side (occupying state square)]
       [:div {:key square :on-click (handler on-move square)}
        (case side
          :crosses "X"
          :circles "O"
          " ")]))])

(rum/defcs app <
  (rum/local (new State 0x0 0x0) ::game)
  [state]
  (let [game (deref (::game state))
        status (game/status game)]
    [:div.app
     [:div.header
      (case (:status status)
        :victory (str (:victor status) " wins!")
        :scratch "Cat's game!"
        :playing (str "It's " (:turn status) "'s turn next."))]
     [:div
      (board game (fn [square]
                    (when (and (= (:status status) :playing)
                               (game/legal? game square))
                      (reset! (::game state) (game/play game square)))))]]))

(defn main []
  (let [element (.querySelector js/document "#scratch")]
    (-> (app) (rum/mount element))))

(main)
