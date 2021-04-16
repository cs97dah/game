(ns game.db
  (:require [taoensso.timbre :as log]))

(def path-gui-info [:gui])
(def path-background-image (conj path-gui-info :background-image))
(def path-sprites [:sprites])
(defn path-sprite [sprite-type] (conj path-sprites sprite-type))
(def path-players [:players])
(defn path-player-id [player-id] (conj path-players player-id))
(defn path-player-direction[player-id] (conj (path-player-id player-id) :direction))

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
  [state sprite-type]
  (get-in state (path-sprite sprite-type)))

(defn update-player-position
  [state player-id x y]
  (log/info "PLAYER:" player-id  #_ (get-in state (path-player-id player-id)))
  (update-in state [:sprites :players player-id] #(-> %
                                              (update-in [:position :x] + x)
                                              (update-in [:position :y] + y)
                                              ))
  )

(defn background-image
  [state]
  (get-in state path-background-image))

(defn init-state []
  (-> {}
      (assoc-in (conj path-gui-info :map-size) {:x 500 :y 500})
      (assoc-in (conj path-gui-info :tile-size) 32)))

(def  set-conj (fnil conj #{}))

(defn assoc-player-direction
  [state player-id direction]
  (update-in state (path-player-direction player-id) set-conj direction))

(defn dissoc-player-direction
  [state player-id direction]
  (update-in state (path-player-direction player-id) disj direction))

(defn direction-player
  [state player-id]
  (get-in state (path-player-direction player-id)))