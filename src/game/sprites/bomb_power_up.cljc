(ns game.sprites.bomb-power-up
  (:require [game.gui :as gui]
            [game.sprites.core :as sprites]
            [medley.core :as medley]
            [quil.core :as q]))

(defrecord BombPowerUp
  [position size coordinates]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (q/ellipse-mode :corner)
      (apply q/fill (gui/colour :purple))
      (q/ellipse x y (:x size) (:y size)))))

(defn create
  [{:keys [position size coordinates]}]
  (let [position (merge-with + position (medley/map-vals #(quot % 4) size))
        size (medley/map-vals #(quot % 2) size)]
    (map->BombPowerUp {:position position
                       :size size
                       :coordinates coordinates})))
