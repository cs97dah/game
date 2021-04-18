(ns game.sprites.wall
  (:require [game.gui :as gui]
            [game.sprites.core :as sprites]
            [quil.core :as q]))

(defrecord Wall
  [position size coordinates]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (apply q/fill (get gui/colours :grey))
      (q/no-stroke)
      (q/rect x y (:x size) (:y size)))))

(defn create
  [position tile-size]
  (map->Wall {:position position
              :size tile-size
              :coordinates (sprites/coordinates position tile-size)}))
