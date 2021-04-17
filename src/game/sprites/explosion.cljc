(ns game.sprites.explosion
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]
            [game.db :as db]
            [taoensso.timbre :as log]))

(def explosion-active-millis 2000)

(defrecord Explosion
  [position size extinguishes-at]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (apply q/fill (gui/colour :orange))
      (q/rect x y (:x size) (:y size)))))

(defn conj-some
  [coll & args]
  (reduce #(cond-> %1
             %2
             (conj %2)) coll args))

(def change-coordinates {:up {:x 0 :y -1}
                         :down {:x 0 :y 1}
                         :left {:x -1 :y 0}
                         :right {:x 1 :y 0}})

(defn check-strength
  [state coordinates bomb-strength direction]
  (let [strength (loop [strength 0
                        bomb-strength (dec bomb-strength)
                        coordinates coordinates]
                   (let [coordinates-to-check (merge-with + coordinates (change-coordinates direction))]
                     (cond
                       (db/wall-at? state coordinates-to-check)
                       strength

                       (or (db/brick-at? state coordinates-to-check)
                           (zero? bomb-strength))
                       (inc strength)

                       :otherwise
                       (recur (inc strength) (dec bomb-strength) coordinates-to-check))))]
    (when (pos? strength)
      strength)))

(defn create
  [state {:keys [position size bomb-strength coordinates] :as bomb}]
  (let [fire-up (check-strength state coordinates bomb-strength :up)
        fire-down (check-strength state coordinates bomb-strength :down)
        fire-left (check-strength state coordinates bomb-strength :left)
        fire-right (check-strength state coordinates bomb-strength :right)
        _ (log/info "fire-up fire-down fire-lef fire-right" fire-up fire-down fire-left fire-right)
        extinguishes-at (db/game-time-plus-millis state explosion-active-millis)
        up-down-position (when (or fire-down fire-up)
                           {:x (+ (:x position) (quot (:x size) 4))
                            :y (cond-> (:y position)
                                 fire-up
                                 (- (+ (* fire-up (:y size)) fire-up)))})
        up-down-size (when up-down-position
                       {:x (quot (:x size) 2)
                        :y (let [length (+ 1 (or fire-up 0) (or fire-down 0))]
                             (+ length (* length (:y size))))})
        up-down-explosion (when up-down-position
                            (map->Explosion {:position up-down-position
                                             :size up-down-size
                                             :extinguishes-at extinguishes-at}))

        left-right-position (when (or fire-right fire-left)
                              {:y (+ (:y position) (quot (:y size) 4))
                               :x (cond-> (:x position)
                                    fire-left
                                    (- (+ (* fire-left (:x size)) fire-left)))})
        left-right-size (when left-right-position
                          {:y (quot (:y size) 2)
                           :x (let [length (+ 1 (or fire-left 0) (or fire-right 0))]
                                (+ length (* length (:x size))))})
        left-right-explosion (when left-right-position
                               (map->Explosion {:position left-right-position
                                                :size left-right-size
                                                :extinguishes-at extinguishes-at}))]
    (conj-some [] up-down-explosion left-right-explosion)))