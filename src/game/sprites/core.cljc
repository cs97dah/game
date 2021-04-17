(ns game.sprites.core
  (:require [game.db :as db]
            [taoensso.timbre :as log]))

(defprotocol Sprite
  (render [sprite]))

(defn update-position
  [player [x y :as proposed-move]]
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
  "True if a is to the left of b"
  [sprite-a sprite-b]
  (let [right-of-a (+ (get-in sprite-a [:position :x]) (get-in sprite-a [:size :x]))
        left-of-b (get-in sprite-b [:position :x])]
    (< right-of-a left-of-b)))

(defn sprites-intersect?
  [sprite-a sprite-b]
  (let [sprites-intersect? (not (or (is-above? sprite-a sprite-b)
                                    (is-above? sprite-b sprite-a)
                                    (is-left? sprite-a sprite-b)
                                    (is-left? sprite-b sprite-a)))]
    sprites-intersect?))

(defn sprite-intersects?
  [sprite sprites]
  (reduce (fn [_ solid-object]
            (if (sprites-intersect? sprite solid-object)
              (reduced solid-object)
              false)) nil sprites))

(defn can-move?
  [state player proposed-move]
  (let [bomb-player-is-on (sprite-intersects? player (db/bombs state))
        solid-objects (concat (db/bricks state)
                              (db/walls state)
                              (disj (db/bombs state) bomb-player-is-on))
        proposed-player (update-position player proposed-move)]
    (not (sprite-intersects? proposed-player solid-objects))))
