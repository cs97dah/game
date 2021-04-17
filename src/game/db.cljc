(ns game.db
  (:require [taoensso.timbre :as log]))

(def path-gui-info [:gui])
(def path-background-image (conj path-gui-info :background-image))
(def path-sprites [:sprites])
(def path-bricks (conj path-sprites :bricks))
(def path-players (conj path-sprites :players))
(defn path-player-id [player-id] (conj path-players player-id))
(defn path-player-direction [player-id] (conj (path-player-id player-id) :direction))

(defn gui-info
  [state]
  (get-in state path-gui-info))

(defn assoc-background-image
  [state image]
  (assoc-in state path-background-image image))

(defn assoc-sprites
  [state sprites]
  (assoc-in state path-sprites sprites))

(defn sprites
  "Return all sprites that need to be rendered"
  [state ]
  ;(log/info "sprites, players:" (vals (get-in state path-players)))
  (concat (get-in state path-bricks)
          (vals (get-in state path-players))
          ))

(defn update-player-position
  [state player-id x y]
  (update-in state [:sprites :players player-id] #(-> %
                                                      (update-in [:position :x] + x)
                                                      (update-in [:position :y] + y))))

(defn background-image
  [state]
  (get-in state path-background-image))

(defn init-state
  [map-width-height tile-width-height ]
  (assoc-in {} path-gui-info {;; TODO: Make these consistent - either xy pairs or a single int
                              :map-size {:x map-width-height :y map-width-height}
                              :tile-size tile-width-height}))

(def set-conj (fnil conj #{}))

(defn assoc-player-direction
  [state player-id direction]
  (update-in state (path-player-direction player-id) set-conj direction))

(defn dissoc-player-direction
  [state player-id direction]
  (update-in state (path-player-direction player-id) disj direction))

(defn direction-player
  [state player-id]
  (get-in state (path-player-direction player-id)))