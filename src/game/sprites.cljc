(ns game.sprites
  (:require [game.db :as db]
            [quil.core :as q]
            [game.gui :as gui]
            [taoensso.timbre :as log]))

(def sprites
  {:wall (fn [state]
           (let [{:keys [tile-size]} (db/gui-info state)]
             {:shorthand :w
              :size tile-size
              :type :wall}))
   :spawn-player (fn [state]
                   (let [{:keys [tile-size]} (db/gui-info state)]
                     {:shorthand :s
                      :size tile-size
                      :type :spawn-player}))
   :player (fn [state]
             (let [{:keys [tile-size]} (db/gui-info state)]
               {:shorthand :p
                ;; TODO: This tile size is wrong needs fixing
                :size {:x (quot tile-size 2) :y (* 3 (quot tile-size 4))}
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
