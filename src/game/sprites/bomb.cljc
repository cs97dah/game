(ns game.sprites.bomb
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]
            [taoensso.timbre :as log]))

(defrecord Bomb
  [position size]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (q/ellipse-mode :corner)
      (apply q/fill (gui/colour :black))
      (q/ellipse x y (:x size) (:y size)))))

(defn create
  [position-and-size]
  ;; TODO: Start timer to explode bomb
  (log/info "Creating bomb:" position-and-size)
  (map->Bomb position-and-size))
