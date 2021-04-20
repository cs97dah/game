(ns game.map.core
  (:require [clojure.string :as string]
            [game.db :as db]
            [game.gui :as gui]
            [game.sprites.bomb :as bomb]
            [game.sprites.bomb-power-up :as bomb-power-up]
            [game.sprites.speed-power-up :as speed-power-up]
            [game.sprites.brick :as brick]
            [game.sprites.player :as player]
            [game.sprites.wall :as wall]
            [quil.core :as q]
            [taoensso.timbre :as log]))

(def basic-map
  "Key:
    w = wall
    p = spawn locations of players 1 -4
    f = free space"
  (->> #_["ww"
          "ww"]
    ["wwwwwwwwwwwwwww"
     "wpf         ffw"
     "wfw w w w w wfw"
     "w             w"
     "w w w w w w w w"
     "w             w"
     "w w w w w w w w"
     "w             w"
     "w w w w w w w w"
     "w             w"
     "wfw w w w w wfw"
     "wff         ffw"
     "wwwwwwwwwwwwwww"]
    (map seq)
    (map (partial map (comp keyword #(when-not (string/blank? %) %) str)))))

(def tiles-wide (count (first basic-map)))
(def tiles-high (count basic-map))

;; Tile size must be a multiple of 4
(def tile-size {:x 64 :y 64})
(def board-size (-> tile-size
                    (update :x * tiles-wide)
                    (update :y * tiles-high)))
(def move-pixels-per-second 120)

(defn paint-wall
  [background-image {:keys [size position] :as wall}]
  (let [{start-x :x start-y :y} position
        {size-x :x size-y :y} size
        end-x (dec (+ start-x size-x))
        end-y (dec (+ start-y size-y))]
    (doseq [x (range start-x (inc end-x))
            y (range start-y (inc end-y))]
      (q/set-pixel background-image x y (apply q/color (get gui/colours :grey))))))

(defn background-image
  [state walls]
  (let [{:keys [tile-size]} (db/gui-info state)
        image-size (-> tile-size
                       (update :x * tiles-wide)
                       (update :y * tiles-high))
        background-image (q/create-image (:x image-size) (:y image-size))]
    (doseq [x (range (:x image-size))
            y (range (:y image-size))]
      (q/set-pixel background-image x y (apply q/color (get gui/colours :green))))
    (doseq [wall walls]
      (paint-wall background-image wall))
    (q/update-pixels background-image)
    background-image))

(defn tile-positions
  [state expected-shorthand]
  (let [{:keys [tile-size]} (db/gui-info state)]
    (doall
      (for [y (range tiles-high)
            x (range tiles-wide)
            :let [shorthand (nth (nth basic-map y) x)]
            :when (= expected-shorthand shorthand)]
        {:x (* x (:x tile-size))
         :y (* y (:y tile-size))}))))

(defn walls
  [state]
  (let [{:keys [tile-size]} (db/gui-info state)]
    (->> (tile-positions state :w)
         (map #(wall/create % tile-size))
         (set))))

(defn one-in
  [n]
  (fn [_] (zero? (rand-int 4))))

(defn bricks [state]
  (let [{:keys [tile-size]} (db/gui-info state)]
    (->> (tile-positions state nil)
         (remove (one-in 4))
         (map #(brick/create % tile-size))
         (set))))

(defn players
  [state]
  (let [{:keys [tile-size]} (db/gui-info state)]
    (->> (tile-positions state :p)
         (map-indexed (fn [player-id position]
                        {player-id (player/create player-id position tile-size)}))
         (apply merge))))

(defn bomb-power-ups
  [bricks]
  (->> bricks
       (filter (one-in 4))
       (map bomb-power-up/create)
       (set)))

(defn speed-power-ups
  [bricks bomb-power-ups]
  (let [bomb-power-up-coordinates (set (map :coordinates bomb-power-ups))]
    (->> bricks
         (remove #(bomb-power-up-coordinates (:coordinates %)))
         (filter (one-in 4))
         (map speed-power-up/create)
         (set))))

(defn initial-state
  [state]
  (let [walls (walls state)
        bricks (bricks state)
        players (players state)
        bomb-power-ups (bomb-power-ups bricks)
        speed-power-ups (speed-power-ups bricks bomb-power-ups)]
    {:background-image (background-image state walls)
     :walls walls
     :bricks bricks
     :bomb-power-ups bomb-power-ups
     :speed-power-ups speed-power-ups
     :players players}))

(defn move-players
  [state]
  (reduce #(player/move-player %1 %2) state (vals (db/players state))))

(defn lay-bombs
  [state]
  (reduce #(player/lay-bomb %1 %2) state (vals (db/players state))))

(defn explode-bombs
  [state exploded-bombs]
  (reduce #(bomb/explode-bomb %1 %2) state exploded-bombs))

(defn kill-players
  [state explosions]
  (reduce #(player/remove-if-dead %1 %2 explosions) state (vals (db/players state))))

(defn destroy-bricks
  [state explosions]
  (reduce #(brick/remove-if-hit %1 %2 explosions) state (db/bricks state)))

(defn power-up-players
  [state]
  (reduce #(player/power-up %1 %2) state (vals (db/players state))))

(defn update-state
  [state]
  (let [state (db/tick-game-time state)
        current-time (db/game-time state)
        exploded-bombs (filter #(< (:bomb-explodes-at %) current-time) (db/bombs state))
        explosions (db/explosions state)
        extinguished-explosions (filter #(< (:extinguishes-at %) current-time) explosions)]
    (-> state
        (kill-players explosions)
        (move-players)
        (power-up-players)
        ;; TODO: Bombs should make other bombs explode
        (explode-bombs exploded-bombs)
        (destroy-bricks explosions)
        (lay-bombs)
        (db/dissoc-explosions extinguished-explosions))))
