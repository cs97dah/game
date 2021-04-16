(ns game.sprites.brick
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]))

(defrecord Brick
  [position size]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (apply q/fill (get gui/colours :brown))
      (q/rect x y (:x size) (:y size)))))

(defn create
  [position tile-size]
  (map->Brick {:position position
               :size {:x tile-size
                      :y tile-size}}))
