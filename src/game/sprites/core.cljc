(ns game.sprites.core
  (:require [game.db :as db]
            [taoensso.timbre :as log]))

(defprotocol Sprite
  (render [sprite]))

(defn update-position
  [player [x y :as proposed-move]]
  (log/info "update-position" player)
  (log/info "update-position 2" (-> player
                                    (update-in [:position :x] + x)
                                    (update-in [:position :y] + y)))
  (-> player
      (update-in [:position :x] + x)
      (update-in [:position :y] + y)))

(defn is-above?
  "True if a is above b"
  [sprite-a sprite-b]
  (let [bottom-of-a (+ (get-in sprite-a [:position :y]) (get-in sprite-a [:size :y]))
        top-of-b (get-in sprite-b [:position :y])]
    (< bottom-of-a top-of-b)))

(defn is-left?
  "True is a is to the left of b"
  [sprite-a sprite-b]
  (let [right-of-a (+ (get-in sprite-a [:position :x]) (get-in sprite-a [:size :x]))
        left-of-b (get-in sprite-b [:position :x])]
    (< right-of-a left-of-b)
    )
  )

(defn sprites-intersect?
  [sprite-a sprite-b]
  (let [sprites-intersect? (not (or (is-above? sprite-a sprite-b)
                                    (is-above? sprite-b sprite-a)
                                    (is-left? sprite-a sprite-b)
                                    (is-left? sprite-b sprite-a)))]
   #_ (do                                                     ;when sprites-intersect?
      (log/info "sprites-intersect")
      (log/info "a" sprite-a)
      (log/info "b" sprite-b)
      )
    sprites-intersect?
    ))

(defn can-move?
  [state player proposed-move]
  ; (log/info "bricks:" (into [] (get-in state db/path-bricks)))
  (let [solid-objects (concat (get-in state db/path-bricks)
                              (get-in state db/path-walls)
                              )
        proposed-player (update-position player proposed-move)
        result (not (reduce (fn [_ solid-object]
                              (if (sprites-intersect? proposed-player solid-object)
                                (reduced true)
                                false)) nil solid-objects))]
    result))
