(ns game.db
  (:require [taoensso.timbre :as log]))

(def path-gui-info [:gui])
(def path-background-image (conj path-gui-info :background-image))
(def path-sprites [:sprites])
(def path-bricks (conj path-sprites :bricks))
(def path-walls (conj path-sprites :walls))
(def path-players [:players])
(defn path-player-id [player-id] (conj path-players player-id))
(def path-keys-pressed [:keys-pressed])

(defn gui-info
  [state]
  (get-in state path-gui-info))

(defn assoc-background-image
  [state image]
  (assoc-in state path-background-image image))

(defn assoc-sprites
  [state sprites]
  (assoc-in state path-sprites sprites))

(defn assoc-players
  [state players]
  (assoc-in state path-players players))

(defn players
  [state]
  (get-in state path-players))

(defn sprites
  "Return all sprites (including players) that need to be rendered"
  [state ]
  (concat (get-in state path-bricks)
          (vals (players state))))

(defn background-image
  [state]
  (get-in state path-background-image))

(defn init-state
  [map-width-height tile-width-height ]
  (assoc-in {} path-gui-info {;; TODO: Make these consistent - either xy pairs or a single int
                              :map-size {:x map-width-height :y map-width-height}
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
