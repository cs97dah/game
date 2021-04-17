(ns game.sprites.wall
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]
            [taoensso.timbre :as log]
            [game.db :as db]))

(defrecord Wall
  [position size coordinates]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (apply q/fill (get gui/colours :grey))
      (q/rect x y (:x size) (:y size)))))

(defn create
  [position tile-size]
  (map->Wall {:position position
              :size {:x tile-size
                     :y tile-size}
              :coordinates (-> position
                                 (update :x quot tile-size)
                                 (update :y quot tile-size))}))
