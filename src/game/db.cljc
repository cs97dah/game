(ns game.db)

(def path-gui-info [:gui])
(def path-background-image (conj path-gui-info :background-image))

(defn gui-info
  [state]
  (get-in state path-gui-info))

(defn assoc-background-image
  [state image]
  (assoc-in state path-background-image image))

(defn background-image
  [state]
  (get-in state path-background-image))

(defn init-state []
  (-> {}
      (assoc-in (conj path-gui-info :size) {:x 500 :y 500})
      (assoc-in (conj path-gui-info :tile-size) {:x 32 :y 32})))