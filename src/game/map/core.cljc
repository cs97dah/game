(ns game.map.core
  (:require [clojure.string :as string]
            [game.db :as db]
            [quil.core :as q]
            [game.sprites.wall :as wall]
            [game.sprites.brick :as brick]
            [game.sprites.bomb :as bomb]
            [taoensso.timbre :as log]
            [game.sprites.player :as player]
            [game.gui :as gui]
            [medley.core :as medley]))

(def basic-map
  "Key:
    w = wall
    p = spawn locations of players 1 -4
    f = free space"
  (->> ["wwwwwwwwwwwwwww"
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

(defn paint-wall
  [background-image {:keys [size position] :as wall}]
  (let [{start-x :x start-y :y} position
        {size-x :x size-y :y} size
        end-x (+ start-x size-x)
        end-y (+ start-y size-y)]
    (doseq [x (range start-x (inc end-x))
            y (range start-y (inc end-y))]
      (q/set-pixel background-image x y (apply q/color (get gui/colours :grey))))))

(defn background-image
  [state walls]
  (let [{:keys [tile-size]} (db/gui-info state)
        image-width (* tile-size (inc tiles-wide))
        image-height (* tile-size (inc tiles-high))
        background-image (q/create-image image-width image-height)]
    (doseq [x (range image-width)
            y (range image-height)]
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
        {:x (+ x (* x tile-size))
         :y (+ y (* y tile-size))}))))

(defn walls
  [state]
  (let [{:keys [tile-size]} (db/gui-info state)]
    (->> (tile-positions state :w)
         (map #(wall/create % tile-size))
         (set))))

(defn bricks [state]
  (let [{:keys [tile-size]} (db/gui-info state)]
    (->> (tile-positions state nil)
         (filter (fn [_] (pos? (rand-int 4))))
         (map #(brick/create % tile-size))
         (set))))

(defn players
  [state]
  (let [{:keys [tile-size]} (db/gui-info state)]
    (->> (tile-positions state :p)
         (map-indexed (fn [player-id position]
                        {player-id (player/create player-id position tile-size)}))
         (apply merge))))

(defn generate
  [state]
  (let [walls (walls state)
        bricks (bricks state)
        players (players state)]
    {:background-image (background-image state walls)
     :walls walls
     :bricks bricks
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

(defn update-state
  [state]
  (let [current-time (db/game-time state)
        exploded-bombs (filter #(< (:bomb-explodes-at %) current-time) (db/bombs state))
        explosions (db/explosions state)
        extinguished-explosions (filter #(< (:extinguishes-at %) current-time)explosions )]
    (-> state
        (kill-players explosions)
        (move-players)
        (destroy-bricks explosions)
        (lay-bombs)
        (explode-bombs exploded-bombs)
        (db/dissoc-explosions extinguished-explosions))))
