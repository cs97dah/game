(ns game.sprites
  (:require [game.db :as db]
            [quil.core :as q]
            [game.gui :as gui]
            [taoensso.timbre :as log]))

(defn draw-player
  [{:keys [position size] :as player} tile-size]
  (let [{:keys [x y]} position
        width (quot tile-size 2)
        offset-x (quot tile-size 4)]
    (apply q/fill (get gui/colours :red))
    (q/rect (+ x offset-x) y width tile-size)))

(defn update-player-position
  [state player-id [x y :as update-vector]]
  (db/update-player-position state player-id x y)
  )

(defn update-state
  [state]
  (if-let [directions (seq (db/direction-player state 1))]
    (reduce (fn [state [player-id player]]
              (reduce (fn [state direction]
                        (let [update-vector (case direction
                                              :up [0 -1]
                                              :down [0 1]
                                              :left [-1 0]
                                              :right [1 0])]
                          (update-player-position state player-id update-vector)
                          )) state directions)
              ) state (db/sprites state :players))
    state))