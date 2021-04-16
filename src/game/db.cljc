(ns game.db
  (:require [taoensso.timbre :as log]))

(def path-gui-info [:gui])
(def path-background-image (conj path-gui-info :background-image))
(def path-sprites [:sprites])
(defn path-sprite [sprite-type] (conj path-sprites sprite-type))

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

(defn background-image
  [state]
  (get-in state path-background-image))

(defn init-state []
  (-> {}
      (assoc-in (conj path-gui-info :map-size) {:x 500 :y 500})
      (assoc-in (conj path-gui-info :tile-size) 32)))