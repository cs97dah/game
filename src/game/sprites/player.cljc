(ns game.sprites.player
  (:require [clojure.set :as set]
            [game.db :as db]
            [game.gui :as gui]
            [game.sprites.bomb :as bomb]
            [game.sprites.core :as sprites]
            [medley.core :as medley]
            [quil.core :as q]))

(def player-colours
  {0 (gui/colour :red)})

(def player-keys
  {0 {:ArrowUp :up
      :ArrowDown :down
      :ArrowLeft :left
      :ArrowRight :right
      :space :bomb}})

(def keys-for-player (->> player-keys
                          (medley/map-vals #(-> % keys set))))

(def relevant-keys (->> player-keys
                        (vals)
                        (mapcat keys)
                        (set)))

(def bomb-key-for-player (->> player-keys
                              (medley/map-vals #(reduce (fn [_ [key action]]
                                                          (when (= action :bomb)
                                                            (reduced key))) nil %))))

(defrecord Player
  [position size player-id bomb-strength dead?]
  sprites/Sprite

  (render [_]
    ;; TODO: Render something when the player is dead
    (when-not dead?
      (let [{:keys [x y]} position]
        (apply q/fill (get player-colours player-id))
        (q/rect x y (:x size) (:y size))))))

(defn create
  [player-id position tile-size]
  (let [size (-> tile-size
                 (update :x quot 2)
                 (update :y #(* 3 (quot % 4))))
        position (-> position
                     (update :x + (quot (:x size) 2))
                     (update :y + (- (:y tile-size) (:y size))))]

    (map->Player {:position position
                  :size size
                  :player-id player-id
                  :bomb-strength 1})))

(defn player-commands
  [state player-id]
  (let [keys-pressed-relevant-to-player (set/intersection (db/keys-pressed state) (keys-for-player player-id))
        directions-for-keys (select-keys (get player-keys player-id) keys-pressed-relevant-to-player)]
    (set (vals directions-for-keys))))

(def move-vectors
  {:up [0 -1]
   :down [0 1]
   :left [-1 0]
   :right [1 0]})

(defn move-player*
  [state player-id direction]
  (let [[x y :as proposed-move] (get move-vectors direction)
        player (db/player state player-id)]
    ;; TODO: Use update-position in sprite.core
    (cond-> state
      (sprites/can-move? state player proposed-move)
      (->
        (update-in (conj (db/path-player-id player-id) :position :x) + x)
        (update-in (conj (db/path-player-id player-id) :position :y) + y)))))

(defn move-player
  [state {:keys [player-id] :as player}]
  (let [commands (player-commands state player-id)]
    (reduce #(move-player* %1 player-id %2) state (disj commands :bomb))))

(defn proposed-bomb-position-and-size
  [state {:keys [position size] :as player}]
  (let [{:keys [x y] :as centre-of-player} {:x (+ (:x position) (quot (:x size) 2))
                                            :y (+ (:y position) (quot (:y size) 2))}
        {:keys [tile-size]} (db/gui-info state)
        {:keys [x y] :as tile-index} {:x (quot x (:x tile-size))
                                      :y (quot y (:y tile-size))}]
    {:position {:x (+ (* (:x tile-size) x) x)
                :y (+ (* (:y tile-size) y) y)}
     :size tile-size}))

(defn laying-bomb?
  [state player-id]
  (:bomb (player-commands state player-id)))

(defn can-lay-bomb?
  [state proposed-bomb-position-and-size]
  (let [bombs (db/bombs state)]
    (not (sprites/sprite-intersects proposed-bomb-position-and-size bombs))))

(defn lay-bomb*
  [state {:keys [player-id] :as player}]
  (let [player (db/player state player-id)
        proposed-bomb-position-and-size (proposed-bomb-position-and-size state player)]
    (-> (cond-> state
          (can-lay-bomb? state proposed-bomb-position-and-size)
          (db/assoc-bomb (bomb/create state player proposed-bomb-position-and-size)))
        (db/dissoc-key-pressed (bomb-key-for-player player-id)))))

(defn lay-bomb
  [state {:keys [player-id] :as player}]
  (cond-> state
    (laying-bomb? state player-id)
    (lay-bomb* player)))

(defn remove-if-dead
  [state {:keys [player-id] :as player} explosions]
  (cond-> state
    (sprites/sprite-intersects player explosions)
    (db/player-dead player-id)))

(defn power-up
  [state {:keys [player-id] :as player}]
  (let [bomb-power-ups (db/bomb-power-ups state)
        power-up (sprites/sprite-intersects player bomb-power-ups)]
    (cond-> state
      power-up
      (->
        (db/bomb-power-up player-id)
        (db/dissoc-bomb-power-up power-up)))))
