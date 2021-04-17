(ns game.sprites.player
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]
            [game.db :as db]
            [clojure.set :as set]
            [game.sprites.bomb :as bomb]
            [medley.core :as medley]
            [taoensso.timbre :as log]))

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
  [position size player-id]
  sprites/Sprite

  (render [_]
    (let [{:keys [x y]} position]
      (apply q/fill (get player-colours player-id))
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
                  :player-id player-id})))

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
        {:keys [x y] :as tile-index} {:x (quot x tile-size)
                                      :y (quot y tile-size)}]
    {:position {:x (+ (* tile-size x) x)
                :y (+ (* tile-size y) y)}
     :size {:x tile-size
            :y tile-size}}))

(defn laying-bomb?
  [state player-id]
  (let [r
        (:bomb (player-commands state player-id))]
    (log/info "laying-bomb?"r )
    r))

(defn can-lay-bomb?
  [state proposed-bomb-position-and-size]
  (let [bombs (db/bombs state)
        can-lay-bomb? (not (sprites/sprite-intersects? proposed-bomb-position-and-size bombs))]
    (log/info "can-lay-bomb?" can-lay-bomb?)
    can-lay-bomb?
    ))

(defn lay-bomb*
  [state {:keys [player-id] :as player}]
  (let [player (db/player state player-id)
        proposed-bomb-position-and-size (proposed-bomb-position-and-size state player)]
    (-> (cond-> state
          (can-lay-bomb? state proposed-bomb-position-and-size)
          (db/assoc-bomb (bomb/create proposed-bomb-position-and-size)))
        (db/dissoc-key-pressed (bomb-key-for-player player-id)))))

(defn lay-bomb
  [state {:keys [player-id] :as player}]
  (cond-> state
    (laying-bomb? state player-id)
    (lay-bomb* player)))