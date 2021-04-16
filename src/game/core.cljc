(ns game.core
  (:require [quil.core :as q :include-macros true]
            [game.gui :as gui]
            [quil.middleware :as m]
            [medley.core :as medley]
            [taoensso.timbre :as log]
            [clojure.string :as string]
            [game.map.core :as map]
            [game.db :as db]
            [game.sprites.core :as sprites-core]            ;; TODO: rename to sprites when possible
            [game.sprites :as sprites]))

(defn setup []
  (q/frame-rate 30)
  (let [state (db/init-state 500 32 )
        {:keys [background-image sprites]} (map/generate state)]
    (-> state
        (db/assoc-background-image background-image)
        (db/assoc-sprites sprites)
        )))

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
  (let [{:keys [tile-size]} (db/gui-info state)]
    (doseq [brick (db/sprites state :bricks)]
      (sprites-core/render brick)
      )
  #_  (doseq [player (vals (db/sprites state :players))]
      (sprites/draw-player player tile-size)))
  #_(doseq [p (vals players)]
      (player p))

  )

(defn direction
  [key-details]
  ;(log/info "direction>" key-details)
  (case (:key key-details)
    :ArrowUp :up
    :ArrowDown :down
    :ArrowLeft :left
    :ArrowRight :right
    nil))

(defn key-pressed
  [state key-details]
  ;(log/info "key-pressed" key-details)
  (let [direction (direction key-details)]
    (cond-> state
      direction
      (db/assoc-player-direction 1 direction)))


  )

(defn key-released
  [state key-details]
  ;(log/info "key-released" key-details)
  (let [direction (direction key-details)]
    (cond-> state
      direction
      (db/dissoc-player-direction 1 direction))))

; this function is called in index.html
(defn ^:export run-sketch []
  (q/defsketch game
               :host "game"
               :size [500 500]
               ; setup function called only once, during sketch initialization.
               :setup setup
               ; update-state is called on each iteration before draw-state.
               :update sprites/update-state
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
