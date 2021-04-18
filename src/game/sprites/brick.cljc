(ns game.sprites.brick
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]
            [game.db :as db]))

(defrecord Brick
  [position size coordinates]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (apply q/fill (get gui/colours :brown))
      (q/rect x y (:x size) (:y size)))))

(defn create
  [position tile-size]
  (map->Brick {:position position
               :size tile-size
               :coordinates (sprites/coordinates position tile-size)}))
(defn remove-if-hit
  [state brick explosions]
  (cond-> state
    (sprites/sprite-intersects brick explosions)
    (db/dissoc-brick brick)
    )
  )