(ns game.map.core
  (:require [clojure.string :as string]
            [game.sprites :as sprites]
            [game.db :as db]
            [quil.core :as q]
            [taoensso.timbre :as log]
            [game.gui :as gui]))

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

(defn draw-map-square
  [background-image {:keys [size position type] :as tile}]
  (let [{start-x :x start-y :y} position
        end-x (+ start-x size)
        end-y (+ start-y size)
        colour (case type
                 :wall :grey
                 :green)]
    (doseq [x (range start-x (inc end-x))
            y (range start-y (inc end-y))]
      (q/set-pixel background-image x y (apply q/color (get gui/colours colour))))))

(defn background-image
  [state]
  (let [basic-map (->> basic-map
                       (map seq)
                       (map (partial map (comp keyword #(when-not (string/blank? %) %) str))))
        width (-> basic-map first seq count)
        height (-> basic-map count)
        {:keys [map-size tile-size]} (db/gui-info state)
        tiles (for [y (range height)
                    x (range width)]
                (let [shorthand (nth (nth basic-map y) x)
                      tile-type (get shorthand->tile-type shorthand)
                      tile-basics (sprites/sprite state tile-type)]
                  (assoc tile-basics :position {:x (+ x (* x tile-size)) :y (+ y (* y tile-size))} :size tile-size)))
         background-image (q/create-image (:x map-size) (:y map-size))]
    (doseq [tile tiles]
      (draw-map-square background-image tile))
    (q/update-pixels background-image)
    background-image))

(defn generate
  [state]
  (let [basic-map (->> basic-map
                       (map seq)
                       (map (partial map (comp keyword #(when-not (string/blank? %) %) str))))
        width (-> basic-map first seq count)
        height (-> basic-map count)
        {:keys [ tile-size]} (db/gui-info state)
        tiles (for [y (range height)
                    x (range width)]
                (let [shorthand (nth (nth basic-map y) x)
                      tile-type (get shorthand->tile-type shorthand)
                      tile-basics (sprites/sprite state tile-type)]
                  (assoc tile-basics :position {:x (+ x (* x tile-size)) :y (+ y (* y tile-size))})))
        bricks (->> tiles
                    (filter (type= :empty-square))
                    (filter (fn [_] (pos? (rand-int 4))))
                    (into []))
        players (->> tiles
                     (filter (type= :player))
                     (map-indexed hash-map)
                     (apply merge))]
    {:background-image (background-image state)
     :sprites {:walls (filter (type= :wall) tiles)
               :bricks bricks
               :players players}}
    ))