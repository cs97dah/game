(ns game.sprites.bomb
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]
            [taoensso.timbre :as log]

            [game.db :as db]))

(defrecord Bomb
  [position size bomb-explodes-at]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (q/ellipse-mode :corner)
      (apply q/fill (gui/colour :black))
      (q/ellipse x y (:x size) (:y size)))))

(defn create
  [state position-and-size]
  (map->Bomb (assoc position-and-size
                    :bomb-explodes-at (db/game-time-plus-millis state 5000))))
