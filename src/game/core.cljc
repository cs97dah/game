(ns game.core
  (:require [game.db :as db]
            [game.map.core :as map]
            [game.sprites.core :as sprites]
            [game.sprites.player :as player]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [taoensso.timbre :as log]))

;; Tile size must be a multiple of 4
(def tile-size {:x 64 :y 64})
(def board-size (-> tile-size
                    (update :x * map/tiles-wide)
                    (update :y * map/tiles-high)))

(defn setup []
  (q/frame-rate 30)
  (let [state (db/init-state board-size tile-size)
        {:keys [background-image walls bricks bomb-power-ups players]} (map/initial-state state)]
    (-> state
        (db/assoc-background-image background-image)
        (db/assoc-walls walls)
        (db/assoc-bricks bricks)
        (db/assoc-bomb-power-ups bomb-power-ups)
        (db/assoc-players players))))

(defn render
  [sprites]
  (doseq [sprite sprites]
    (sprites/render sprite)))

(defn draw-state [state]
  (q/set-image 0 0 (db/background-image state))
  (let [bricks (db/bricks state)
        players (vals (db/players state))
        bombs (db/bombs state)
        bomb-power-ups (db/bomb-power-ups state)
        explosions (db/explosions state)]
    (render bomb-power-ups)
    (render bricks)
    (render bombs)
    (render players)
    (render explosions)))

(defn key-pressed
  [state key-details]
  (let [key (-> key-details :key player/relevant-keys)]
    (cond-> state
      key
      (db/assoc-key-pressed  key))))

(defn key-released
  [state key-details]
  (db/dissoc-key-pressed state (:key key-details)))

; this function is called in index.html
(defn ^:export run-sketch []
  (q/defsketch game
               :host "game"
               :size [(:x board-size) (:y board-size)]
               ; setup function called only once, during sketch initialization.
               :setup setup
               ; update-state is called on each iteration before draw-state.
               :update map/update-state
               :draw draw-state
               ; This sketch uses functional-mode middleware.
               ; Check quil wiki for more info about middlewares and particularly
               ; fun-mode.
               :middleware [m/fun-mode]

               :key-pressed key-pressed
               :key-released key-released

               ))

; uncomment this line to reset the sketch:
; (run-sketch)
