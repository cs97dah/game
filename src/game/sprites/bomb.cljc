(ns game.sprites.bomb
  (:require [game.sprites.core :as sprites]
            [game.sprites.explosion :as explosion]
            [game.gui :as gui]
            [quil.core :as q]
            [taoensso.timbre :as log]
            [game.db :as db]))

(def bomb-explode-millis 4000)

(defrecord Bomb
  [position size bomb-explodes-at bomb-strength]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (q/ellipse-mode :corner)
      (apply q/fill (gui/colour :black))
      (q/ellipse x y (:x size) (:y size)))))

(defn create
  [state {:keys [bomb-strength] :as player} position-and-size]
  (map->Bomb (assoc position-and-size
                    :bomb-explodes-at (db/game-time-plus-millis state bomb-explode-millis)
                    :bomb-strength bomb-strength)))

(defn explode-bomb
  [state bomb]
  (let [explosions (explosion/create state bomb)]
    (-> state
        (db/dissoc-bomb bomb)
        (db/assoc-explosions explosions))))
