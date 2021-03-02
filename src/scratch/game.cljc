(ns scratch.game)

(defrecord State [crosses circles])

(defn ^{:private true} weight [number]
  (loop [value number count 0]
    (if (> value 0)
      (recur (bit-and value (- value 1)) (inc count))
      count)))

(def ^{:private true} victories
  [0x7 0x38 0x49 0x54 0x92 0x111 0x124 0x1c0])

(defn ^{:private true} boards [state]
  [(:crosses state) (:circles state)])

(defn ^{:private true} flag? [flag]
  (= (weight flag) 1))

(defn ^{:private true} turn [state]
  (let [occupied (apply bit-or (boards state))]
    (if (even? (weight occupied)) :crosses :circles)))

(defn ^{:private true} opponent [side]
  (case side :crosses :circles :circles :crosses :crosses))

(defn ^{:private true} victory? [board]
  (or (some (fn [victory] (= (bit-and board victory) victory)) victories) false))

(defn ^{:private true} scratch? [state]
  (->> (boards state) (apply bit-or) (= 0x1FF)))

(defn ^{:private true} legal [state]
  (->> (boards state) (apply bit-and) (bit-not) (bit-and 0x1FF)))

(defn legal? [state move]
  (assert (flag? move))
  (let [atk (get state (turn state))
        dfn (get state (opponent (turn state)))]
    (zero? (bit-and (bit-or atk move) dfn))))

(defn play [state move]
  (assert (flag? move))
  (let [atk (get state (turn state))]
    (assoc state (turn state) (bit-or atk move))))

(defn status [state]
  (let [next (turn state) prev (opponent next)]
    (cond
      (victory? (prev state)) {:status :victory :victor prev}
      (scratch? state) {:status :scratch}
      :else {:status :playing :turn next})))
