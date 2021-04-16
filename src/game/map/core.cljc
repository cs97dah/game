(ns game.map.core
  (:require [clojure.string :as string]
            [game.sprites :as sprites]
            [game.db :as db]
            [quil.core :as q]
            [taoensso.timbre :as log]))

(def basic-map
  "Key:
    w = wall
    s = spawn locations of players 1 -4"
  ["wwwwwwwwwwwwwww"
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
   "wwwwwwwwwwwwwww"])

(def shorthand->tile-type
  (->> sprites/sprites
       (vals)
       (map #(% nil))
       (reduce (fn [shorthand->tile-type {:keys [shorthand type]}]
                 (assoc shorthand->tile-type shorthand type)) nil)))

(defn type=
  [type]
  #(= type (:type %)))

(defn generate
  [state]
  (let [basic-map (->> basic-map
                       (map seq)
                       (map (partial map (comp keyword #(when-not (string/blank? %) %) str))))
        width (-> basic-map first seq count)
        height (-> basic-map count)
        {image-x :x image-y :y} (:size (db/gui-info state))
        tiles (for [y (range height)
                    x (range width)]
                (let [shorthand (nth (nth basic-map y) x)
                      tile-type (get shorthand->tile-type shorthand)
                      {{x-size :x y-size :y} :size :as tile-basics} (sprites/sprite state tile-type)]
                  (assoc tile-basics :position {:x (+ x (* x x-size)) :y (+ y (* y y-size))})))
        bricks (->> tiles
                    (filter (type= :empty-square))
                    (filter (fn [_] (pos? (rand-int 4)))))]
    (let [background-image (q/create-image image-x image-y)]
      ;(log/info "background-image"background-image)
      (doseq [tile tiles]
        (sprites/render-tile background-image tile))
      (q/update-pixels background-image)
      {:background-image background-image
       :sprites {:walls (filter (type= :wall) tiles)
                 :bricks bricks}}))
  )