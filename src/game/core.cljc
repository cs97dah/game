(ns game.core
  (:require [quil.core :as q :include-macros true]
            [game.gui :as gui]
            [quil.middleware :as m]
            [medley.core :as medley]
            [taoensso.timbre :as log]
            [clojure.string :as string]
            [game.map.core :as map]
            [game.db :as db]
            [game.sprites.player :as player]
            [game.sprites.core :as sprites-core]            ;; TODO: rename to sprites when possible
            ))

(defn setup []
  (q/frame-rate 30)
  (let [state (db/init-state {:x 990 :y 858} 64)
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
    (sprites-core/render sprite)))

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
  ;(log/info "Height:" (q/height))
  (q/defsketch game
               :host "game"
               :size [990 858]
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
