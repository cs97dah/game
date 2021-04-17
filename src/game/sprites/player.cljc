(ns game.sprites.player
  (:require [game.sprites.core :as sprites]
            [game.gui :as gui]
            [quil.core :as q]
            [game.db :as db]
            [clojure.set :as set]
            [medley.core :as medley]
            [taoensso.timbre :as log]))

(def player-colours
  {0 (gui/colour :red)})

(def player-keys
  {0 {:ArrowUp :up
      :ArrowDown :down
      :ArrowLeft :left
      :ArrowRight :right}})

(def keys-for-player (->> player-keys
                          (medley/map-vals #(-> % keys set))))

(def relevant-keys (->> player-keys
                        (vals)
                        (mapcat keys)
                        (set)))

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

(defn directions
  [state player-id]
  ;(log/info "(db/keys-pressed state)" (db/keys-pressed state) player-id)
  ;(log/info "(keys-for-player player-id)" (keys-for-player player-id) player-id)
  (let [keys-pressed-relevant-to-player (set/intersection (db/keys-pressed state) (keys-for-player player-id))
        directions-for-keys (select-keys (get player-keys player-id) keys-pressed-relevant-to-player)]

    (vals directions-for-keys)))

(def move-vectors
  {:up [0 -2]
   :down [0 2]
   :left [-2 0]
   :right [2 0]})

(defn move-player*
  [state player-id direction]
  (let [[x y] (get move-vectors direction)]
    (-> state
        (update-in (conj (db/path-player-id player-id) :position :x) + x)
        (update-in (conj (db/path-player-id player-id) :position :y) + y))))

(defn move-player
  [state {:keys [player-id] :as player}]
  ;(log/info "move-player" player)
  (let [directions (directions state player-id)]
    ;(log/info "move-player" player directions)
    (reduce #(move-player* %1 player-id %2) state directions)))
