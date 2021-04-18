(ns game.db
  (:require [taoensso.timbre :as log]
            [medley.core :as medley]))

(def path-gui-info [:gui])
(def path-background-image (conj path-gui-info :background-image))
(def path-bricks [:bricks])
(def path-bricks-by-coordinates [:bricks-by-coords])
(def path-walls [:walls])
(def path-walls-by-coordinates [:walls-by-coords])
(def path-players [:players])
(def path-bombs [:bombs])
(def path-explosions [:explosions])
(def path-bomb-power-ups [:bomb-power-ups])
(defn path-player-id [player-id] (conj path-players player-id))
(def path-keys-pressed [:keys-pressed])
(def path-time [:time])

(defn gui-info
  [state]
  (get-in state path-gui-info))

(defn assoc-background-image
  [state image]
  (assoc-in state path-background-image image))

(defn assoc-wall-by-coordinates
  [state {:keys [coordinates] :as wall}]
  (assoc-in state (conj path-walls-by-coordinates coordinates) wall))

(defn assoc-walls
  [state walls]
  (let [state (assoc-in state path-walls walls)]
    ;; TODO: Can the above be scrapped to just have this instead?
    (reduce assoc-wall-by-coordinates state walls)))

(defn dissoc-brick
  [state {:keys [coordinates] :as brick}]
  (-> state
      (update-in path-bricks-by-coordinates dissoc coordinates)
      (update-in  path-bricks disj brick)))

(defn wall-at?
  [state coordinates]
  (get-in state (conj path-walls-by-coordinates coordinates)))

(defn assoc-brick-by-coordinates
  [state {:keys [coordinates] :as brick}]
  (assoc-in state (conj path-bricks-by-coordinates coordinates) brick))

(defn assoc-bricks
  [state bricks]
  (let [state (assoc-in state path-bricks bricks)]
    ;; TODO: Can the above be scrapped to just have this instead?
    (reduce assoc-brick-by-coordinates state bricks)))

(defn assoc-bomb-power-ups
  [state bomb-power-ups]
  (assoc-in state path-bomb-power-ups bomb-power-ups))

(defn bomb-power-ups
  [state ]
  (get-in state path-bomb-power-ups))

(defn dissoc-bomb-power-up
  [state power-up]
  (update-in state path-bomb-power-ups disj power-up))

(defn brick-at?
  [state coordinates]
  (get-in state (conj path-bricks-by-coordinates coordinates)))

(defn assoc-players
  [state players]
  (assoc-in state path-players players))

(defn players
  [state]
  (get-in state path-players))

(defn bricks
  [state]
  (get-in state path-bricks))

(defn walls
  [state]
  (get-in state path-walls))

(defn background-image
  [state]
  (get-in state path-background-image))

(defn init-state
  [map-width-height tile-width-height ]
  (assoc-in {} path-gui-info {:map-size {:x map-width-height :y map-width-height}
                              ;; TODO: tile size should be xy pair like everything else
                              :tile-size tile-width-height}))

(def set-conj (fnil conj #{}))

(defn assoc-key-pressed
  [state key]
  (update-in state path-keys-pressed set-conj key))

(defn dissoc-key-pressed
  [state key]
  (update-in state path-keys-pressed disj key))

(defn keys-pressed
  [state]
  (get-in state path-keys-pressed))

(defn player
  [state player-id]
  (get-in state (path-player-id player-id)))

(defn player-dead
  [state player-id]
  (update-in state (path-player-id player-id) assoc :dead? true))

(defn bomb-power-up
  [state player-id]
  (update-in state (conj (path-player-id player-id) :bomb-strength) inc))

(defn bombs
  [state]
  (get-in state path-bombs))

(defn assoc-bomb
  [state bomb]
  (update-in state path-bombs set-conj bomb))

(defn- millis []
  ;; TODO: Keep a game time in state
  #?(:clj  (System/currentTimeMillis)
     :cljs (.getTime (js/Date.))))

(defn tick-game-time
  [state]
  (assoc-in state path-time (millis)))

(defn game-time
  [state]
  (get-in state path-time))

(defn game-time-plus-millis
  [state millis]
  (+ (game-time state) millis))

(defn dissoc-bomb
  [state bomb]
  (update-in state path-bombs disj bomb))

(defn assoc-explosions
  [state explosions]
  (update-in state path-explosions #(apply set-conj % explosions)))

(defn explosions
  [state]
  (get-in state path-explosions))

(defn dissoc-explosions
  [state explosions]
  (update-in state path-explosions #(apply disj % explosions)))
