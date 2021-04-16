(ns game.sprites
  (:require [game.db :as db]
            [quil.core :as q]
            [game.gui :as gui]
            [taoensso.timbre :as log]))

(def sprites
  {:wall (fn [state]
           (let [{:keys [tile-size]} (db/gui-info state)]
             {:shorthand :w
              ;  :size tile-sizex
              :type :wall}))
   :spawn-player (fn [state]
                   (let [{:keys [tile-size]} (db/gui-info state)]
                     {:shorthand :s
                      :size tile-size
                      :type :spawn-player}))
   :player (fn [state]
             (let [{:keys [tile-size]} (db/gui-info state)
                   {:keys [x y] } tile-size]
               {:shorthand :p
                :size tile-size #_{:x (quot x 2) :y (* 3 (quot y 4))}
                :type :player}))
   ;; Empty squares are empty but can be filled with bricks
   :empty-square (fn [state]
                   (let [{:keys [tile-size]} (db/gui-info state)]
                     {:shorthand nil
                      :size tile-size
                      :type :empty-square}))
   ;; Free squares are empty and must be left that way
   :free-square (fn [state]
                  (let [{:keys [tile-size]} (db/gui-info state)]
                    {:shorthand :f
                     :size tile-size
                     :type :free-square}))
   :brick (fn [state]
            (let [{:keys [tile-size]} (db/gui-info state)]
              {:shorthand :b
               :size tile-size
               :type :brick}))})

(defn sprite
  [state type]
  ((get sprites type) state))
#_
(defn render-tile
  [im {:keys [type size position] :as tile}]
  (let [{start-x :x start-y :y} position
        {width :x height :y} size
        end-x (+ start-x width)
        end-y (+ start-y height)
        colour (case type
                 :wall :grey
                 :green)]
    (doseq [x (range start-x (inc end-x))
            y (range start-y (inc end-y))]
      #_(when (= :spawn-player type)
          (log/info :spawn-player colour x y))
      (q/set-pixel im x y (apply q/color (get gui/colours colour))))))

(defn draw-brick
  [{:keys [position] :as brick} tile-size]
  ;(log/info "draw-brick" brick tile-size)
  (let [{:keys [x y]} position]
    (apply q/fill (get gui/colours :brown))
    (q/rect x y tile-size tile-size)))

(defn draw-player
  [{:keys [position size] :as player}]
  (let [{:keys [x y]} position
        {height :y width :x} size]
    (apply q/fill (get gui/colours :red))
    (q/rect x y width height)))
