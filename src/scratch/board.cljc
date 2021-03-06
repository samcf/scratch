(ns scratch.board)

(defrecord Boards [crosses circles])

(defn weight
  "Returns the hamming weight of the given number."
  [number]
  (loop [value number count 0]
    (if (> value 0)
      (recur (bit-and value (- value 1)) (inc count))
      count)))

(def victories
  "The set of all possible board masks that are considered victory states."
  #{0x7 0x38 0x49 0x54 0x92 0x111 0x124 0x1c0})

(def squares
  "The set of all board masks with a hamming weight of one. Useful to iterate
   through all the squares of the board."
  #{0x100 0x80 0x40 0x20 0x10 0x8 0x4 0x2 0x1})

(defn boards
  "Returns both board masks within the given board as a vector."
  [board]
  [(:crosses board) (:circles board)])

(defn legal
  "Returns a board mask of all possible legal positions."
  [board]
  (->> (boards board) (apply bit-or) (bit-not) (bit-and 0x1FF)))

(defn turn
  "Returns the side that must go next (:crosses or :circles)."
  [board]
  (if (even? (weight (apply bit-or (boards board)))) :crosses :circles))

(defn opponent
  "Returns the side that opposes the given side."
  [side]
  (case side :crosses :circles :circles :crosses :crosses))

(defn occupying
  "Returns the side that occupies the given square within the given board or
   :neither if none."
  [board square]
  (let [{:keys [crosses circles]} board]
    (cond
      (= square (bit-and square crosses)) :crosses
      (= square (bit-and square circles)) :circles
      :else :neither)))

(defn flag?
  "Return true if the given mask has a hamming weight of one.
   This predicate function lets us assert that an argument is a mask that
   represents a single play."
  [flag]
  (= (weight flag) 1))

(defn victory?
  "Returns true if the given board mask is in any victory state."
  [mask]
  ((comp boolean some) #(= (bit-and mask %) %) victories))

(defn scratch?
  "Returns true if the given board is in a scratch state."
  [board]
  (->> (boards board) (apply bit-or) (= 0x1FF)))

(defn legal?
  "Returns true if the given move is legal regardless of whose turn it is."
  [board square]
  (assert (flag? square))
  (let [atk (get board (turn board))
        dfn (get board (opponent (turn board)))]
    (zero? (bit-and (bit-or atk square) dfn))))

(defn play
  "Returns a new board with the next turn's mask updated with the given move."
  [board square]
  (assert (flag? square))
  (assert (legal? board square))
  (let [side (turn board)]
    (assoc board side (bit-or (side board) square))))

(defn state
  "Returns :victory, :scratch, or :playing depending on the current state of
   the board."
  [board]
  (let [prev (opponent (turn board))]
    (cond
      (victory? (prev board)) :victory
      (scratch? board)        :scratch
      :else                   :playing)))

(defn check
  "Returns a tuple of [legal? state] for the move given by the board, side, and
   square. When [true state], state is the state that the board will be in
   should that play be performed. When [false, state], state is the state of
   the board as it was passed in."
  [board side square]
  (let [mode (state board)]
    (cond
      (not (= mode :playing))     [false mode]
      (not (= side (turn board))) [false :offside]
      (legal? board square)       [true (state (play board square))])))

(defn best
  "Returns the best, most optimal move given a board."
  [board]
  (let [candidates (filter #(= % (bit-and % (legal board))) squares)]
    (when (not (empty? candidates))
      (rand-nth candidates))))

(defn create "Returns a new, unplayed board." []
  (new Boards 0x0 0x0))
