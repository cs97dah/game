(ns game.sprites.player
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]))

(defrecord Player
  [position size]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (apply q/fill (get gui/colours :brown))
      (q/rect x y size size))))

(defn player
  [position tile-size]
  (map->Player {:position position
                :size {:x (quot tile-size 2)
                       :y (* 3 (quot tile-size 4))}}))
