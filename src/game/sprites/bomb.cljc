(ns game.sprites.bomb
  (:require [game.db :as db]
            [game.gui :as gui]
            [game.sprites.core :as sprites]
            [game.sprites.explosion :as explosion]
            [quil.core :as q]))

(def bomb-explode-millis 4000)

(defrecord Bomb
  [position size bomb-explodes-at bomb-strength coordinates]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (q/ellipse-mode :corner)
      (apply q/fill (gui/colour :black))
      (q/ellipse x y (:x size) (:y size)))))

(defn create
  [state {:keys [bomb-strength] :as player} {:keys [position size] :as position-and-size}]
  (map->Bomb (assoc position-and-size
                    :bomb-explodes-at (db/game-time-plus-millis state bomb-explode-millis)
                    :bomb-strength bomb-strength
                    :coordinates (-> position
                                     (update :x quot (:x size))
                                     (update :y quot (:y size))))))

(defn explode-bomb
  [state bomb]
  (let [explosions (explosion/create state bomb)]
    (-> state
        (db/dissoc-bomb bomb)
        (db/assoc-explosions explosions))))
