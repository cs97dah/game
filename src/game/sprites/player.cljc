(ns game.sprites.player
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]))

(defrecord Player
  [position size colour]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (apply q/fill colour)
      (q/rect x y (:x size) (:y size)))))

(defn create
  [player-id position tile-size]
  (let [width (quot tile-size 2)
        height (* 3 (quot tile-size 4))
        position (-> position
                     (update :x + (quot width 2))
                     (update :y + (- tile-size height)))]
    (map->Player {:position position
                  :size {:x width
                         :y height}
                  :colour (gui/colour (case player-id
                                        0 :red))})))

(defn try-move
  [player directions])