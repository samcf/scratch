(ns scratch.game)

(defrecord State [crosses circles])

(def victories
  [0x7 0x38 0x49 0x54 0x92 0x111 0x124 0x1c0])

(defn boards [state]
  [(:crosses state) (:circles state)])

(defn flag? [flag]
  (= (Integer/bitCount flag) 1))

(defn turn [state]
  (let [occupied (apply bit-or (boards state))]
    (if (even? (Integer/bitCount occupied)) :crosses :circles)))

(defn opponent [side]
  (case side :crosses :circles :circles :crosses :crosses))

(defn victory? [board]
  (or (some (fn [victory] (= (bit-and board victory) victory)) victories) false))

(defn legal [state]
  (-> (boards state) (apply bit-and) (bit-not) (bit-and 0x1ff)))

(defn legal? [state move]
  (assert (flag? move))
  (let [atk (get state (turn state))
        dfn (get state (opponent (turn state)))]
    (zero? (bit-and (bit-or atk move) dfn))))

(defn play [state move]
  (assert (flag? move))
  (let [atk (get state (turn state))]
    (assoc state (turn state) (bit-or atk move))))
