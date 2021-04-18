(ns game.sprites.bomb-power-up
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]
            [taoensso.timbre :as log]
            [game.db :as db]
            [medley.core :as medley]))

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
