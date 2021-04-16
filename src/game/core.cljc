(ns game.core
  (:require [quil.core :as q :include-macros true]
            [game.gui :as gui]
            [quil.middleware :as m]
            [medley.core :as medley]
            [taoensso.timbre :as log]
            [clojure.string :as string]
            [game.map.core :as map]
            [game.db :as db]
            [game.sprites :as sprites]))

(defn setup []
  (q/frame-rate 30)
  (let [state (db/init-state)
        {:keys [background-image sprites]} (map/generate state)]
    (-> state
        (db/assoc-background-image background-image)
        (db/assoc-sprites sprites)
        )))


(defn update-state
  [state]
  state
  )

(defn square
  [{:keys [size position colour] :as params}]


  (let [{:keys [x y]} position
        {height :y width :x} size]
    (apply q/fill (get gui/colours colour))
    (q/rect x y width height))
  )

(defn player [{:keys [size position colour] :as player}]
  (square player)
  )

(defn draw-state [state]
  (q/set-image 0 0 (db/background-image state))
  ;(log/info "draw-state" (:sprites state))
  (let [{:keys [tile-size]} (db/gui-info state)]
    ; (log/info "DRAWING BRICKS:" (db/sprites state :bricks))
    (doseq [brick (db/sprites state :bricks)]
      (sprites/draw-brick brick tile-size)))
  #_(doseq [player (vals (db/sprites state :players))]
    (sprites/draw-player player))
  #_(doseq [p (vals players)]
      (player p))

  )

(defn key-pressed
  [& args]
  (log/info "key-pressed" args)
  (first args)
  )

(defn key-released
  [& args]
  (log/info "key-released" key)
  args)

; this function is called in index.html
(defn ^:export run-sketch []
  (q/defsketch game
               :host "game"
               :size [500 500]
               ; setup function called only once, during sketch initialization.
               :setup setup
               ; update-state is called on each iteration before draw-state.
               :update update-state
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
