(ns game.sprites.core
  (:require [game.db :as db]
            [medley.core :as medley]))

(defprotocol Sprite
  (render [sprite]))

(defn is-above?
  "True if a is above b"
  [sprite-a sprite-b]
  (let [bottom-of-a (+ (get-in sprite-a [:position :y]) (get-in sprite-a [:size :y]))
        top-of-b (get-in sprite-b [:position :y])]
    (<= bottom-of-a top-of-b)))

(defn is-left?
  "True if a is to the left of b"
  [sprite-a sprite-b]
  (let [right-of-a (+ (get-in sprite-a [:position :x]) (get-in sprite-a [:size :x]))
        left-of-b (get-in sprite-b [:position :x])]
    (<= right-of-a left-of-b)))

(defn sprites-intersect?
  [sprite-a sprite-b]
  (not (or (is-above? sprite-a sprite-b)
           (is-above? sprite-b sprite-a)
           (is-left? sprite-a sprite-b)
           (is-left? sprite-b sprite-a))))

(defn sprite-intersects
  "Returns the first sprite from the sequence of sprites that intersects sprite
  (or nil if none intersect)"
  [sprite sprites]
  (medley/find-first #(sprites-intersect? sprite %) sprites))

(defn coordinates
  [position tile-size]
  (-> position
      (update :x quot (:x tile-size))
      (update :y quot (:y tile-size))))

(defn position-of-coordinates
  [state coordinates]
  (let [{:keys [tile-size]} (db/gui-info state)]
    (merge-with * tile-size coordinates)))
