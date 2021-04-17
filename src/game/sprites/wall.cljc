(ns game.sprites.wall
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]
            [taoensso.timbre :as log]))

(defrecord Wall
  [position size]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (apply q/fill (get gui/colours :grey))
      (q/rect x y (:x size) (:y size)))))

(defn create
  [position tile-size]
  (map->Wall {:position position
              :size {:x tile-size
                     :y tile-size}}))
