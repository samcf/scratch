(ns scratch.slurp
  (:refer-clojure :exclude [slurp]))

(defmacro slurp []
  (clojure.core/slurp "src/scratch/board.cljc"))
