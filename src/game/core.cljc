(ns game.core
  (:require [game.db :as db]
            [game.map.core :as map]
            [game.sprites.core :as sprites]
            [game.sprites.player :as player]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(defn init-state []
  (let [state (db/init-state map/board-size map/tile-size map/move-pixels-per-second)
        {:keys [background-image walls bricks bomb-power-ups players speed-power-ups]} (map/initial-state state)]
    (-> state
        (db/assoc-background-image background-image)
        (db/assoc-walls walls)
        (db/assoc-bricks bricks)
        (db/assoc-bomb-power-ups bomb-power-ups)
        (db/assoc-speed-power-ups speed-power-ups)
        (db/assoc-players players))))

(defn setup []
  (q/frame-rate 30)
  (db/assoc-game-state {} :menu))

(defn render
  [sprites]
  (doseq [sprite sprites]
    (sprites/render sprite)))

(defn draw-game
  [state]
  (q/set-image 0 0 (db/background-image state))
  (let [bricks (db/bricks state)
        players (db/players state)
        bombs (db/bombs state)
        bomb-power-ups (db/bomb-power-ups state)
        speed-power-ups (db/speed-power-ups state)
        explosions (db/explosions state)]
    (render bomb-power-ups)
    (render speed-power-ups)
    (render bricks)
    (render bombs)
    (render (vals players))
    (render explosions)))

(defn draw-menu []
  (q/background 255)
  (q/fill 0)
  (q/text "Computer game. Press any key to start." 10 30))

(defn draw-result [state]
  (q/fill 0)
  (let [winner (db/winner state)]
    (q/text (str "Game over. " winner " Press any key to return to the menu.") 10 30)))

(defn draw-state
  [state]
  (case (db/game-state state)
    :running
    (draw-game state)

    :menu
    (draw-menu)

    :game-over
    (do
      (draw-game state)
      (draw-result state))))

(defn key-pressed
  [state key-details]
  (case (db/game-state state)
    :running
    (if-let [key (-> key-details :key player/relevant-keys)]
      (db/assoc-key-pressed state key)
      state)

    :menu
    (-> (init-state)
        (db/assoc-game-state :running))

    :game-over
    (db/assoc-game-state {} :menu)))

(defn key-released
  [state key-details]
  (db/dissoc-key-pressed state (:key key-details)))

; this function is called in index.html
(defn ^:export run-sketch []
  (q/defsketch game
               :host "game"
               :size [(:x map/board-size) (:y map/board-size)]
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
               :key-released key-released))

; uncomment this line to reset the sketch:
; (run-sketch)
