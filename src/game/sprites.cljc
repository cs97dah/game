(ns game.sprites
  (:require [game.db :as db]
            [quil.core :as q]
            [game.gui :as gui]
            [taoensso.timbre :as log]))

(def sprites
  {:wall (fn [state]
           (let [{:keys [tile-size]} (db/gui-info state)]
             {:shorthand :w
              ;  :size tile-sizex
              :type :wall}))
   :spawn-player (fn [state]
                   (let [{:keys [tile-size]} (db/gui-info state)]
                     {:shorthand :s
                      :size tile-size
                      :type :spawn-player}))
   :player (fn [state]
             (let [{:keys [tile-size]} (db/gui-info state)
                   {:keys [x y]} tile-size]
               {:shorthand :p
                :size tile-size #_{:x (quot x 2) :y (* 3 (quot y 4))}
                :type :player}))
   ;; Empty squares are empty but can be filled with bricks
   :empty-square (fn [state]
                   (let [{:keys [tile-size]} (db/gui-info state)]
                     {:shorthand nil
                      :size tile-size
                      :type :empty-square}))
   ;; Free squares are empty and must be left that way
   :free-square (fn [state]
                  (let [{:keys [tile-size]} (db/gui-info state)]
                    {:shorthand :f
                     :size tile-size
                     :type :free-square}))
   :brick (fn [state]
            (let [{:keys [tile-size]} (db/gui-info state)]
              {:shorthand :b
               :size tile-size
               :type :brick}))})

(defn sprite
  [state type]
  ((get sprites type) state))

(defn draw-brick
  [{:keys [position] :as brick} tile-size]
  (let [{:keys [x y]} position]
    (apply q/fill (get gui/colours :brown))
    (q/rect x y tile-size tile-size)))

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
    (do
      ;(log/info "directions" directions)
      (reduce (fn [state [player-id player]]
                ;         (log/info "reduce 1" player-id player)
                (reduce (fn [state direction]
                          (let [update-vector (case direction
                                                :up [0 -1]
                                                :down [0 1]
                                                :left [-1 0]
                                                :right [1 0])]
                            (update-player-position state player-id update-vector)
                            )) state directions)
                #_#_(log/info "update-state" [player-id player])
                state
                ) state (db/sprites state :players)))
    state))