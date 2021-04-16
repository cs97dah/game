(ns game.core
  (:require [quil.core :as q :include-macros true]
            [game.gui :as gui]
            [quil.middleware :as m]
            [medley.core :as medley]
            [taoensso.timbre :as log]
            [clojure.string :as string]
            [game.map.core :as map]
            [game.db :as db]))

;(def tile-size 32)
#_
(defn assoc-keys-into-vals
  [coll]
  (medley/map-kv (fn [k v]
                   [k (assoc v :type k)]) coll))

#_
(def map-key
  (assoc-keys-into-vals
    {:wall {:shorthand :w
            :size {:x tile-size :y tile-size}
            :colour :grey}
     :spawn-player-1 {:shorthand :1
                      :size {:x tile-size :y tile-size}
                      :colour :green}
     :spawn-player-2 {:shorthand :2
                      :size {:x tile-size :y tile-size}
                      :colour :green}
     :spawn-player-3 {:shorthand :3
                      :size {:x tile-size :y tile-size}
                      :colour :green}
     :spawn-player-4 {:shorthand :4
                      :size {:x tile-size :y tile-size}
                      :colour :green}
     ;; Empty squares are empty but can be filled with bricks
     :empty-square {:shorthand nil
                    :size {:x tile-size :y tile-size}
                    :colour :green}
     ;; Free squares are empty and must be left that way
     :free-square {:shorthand :f
                   :size {:x tile-size :y tile-size}
                   :colour :green}
     :bricks {:shorthand :b
              :size {:x tile-size :y tile-size}
              :colour :brown}}))
#_
(def shorthand->tile-name (reduce (fn [shorthand->tile-name [tile-name {:keys [shorthand]}]]
                                    (assoc shorthand->tile-name shorthand tile-name)
                                    ) nil map-key))



#_

(defn generate-background
  [state basic-map]
  (let [basic-map (->> basic-map
                       (map seq)
                       (map (partial map (comp keyword #(when-not (string/blank? %) %) str))))
        width (-> basic-map first seq count)
        height (-> basic-map count)
        tiles (for [y (range height)
                    x (range width)]
                (let [shorthand (nth (nth basic-map y) x)
                      tile-name (get shorthand->tile-name shorthand)
                      tile-basics (get map-key tile-name)]
                  (assoc tile-basics :position {:x (+ x (* x tile-size)) :y (+ y (* y tile-size))})))]
    (let [im (q/create-image 500 500)]
      (doseq [{:keys [type] :as tile} tiles]
        (render-tile im tile))
      (q/update-pixels im)
      im)))

#_
(defn players
  [basic-map]
  (let [basic-map (->> basic-map
                       (map seq)
                       (map (partial map (comp keyword #(when-not (string/blank? %) %) str))))
        width (-> basic-map first seq count)
        height (-> basic-map count)
        tiles (for [y (range height)
                    x (range width)]
                (let [shorthand (nth (nth basic-map y) x)
                      tile-name (get shorthand->tile-name shorthand)
                      tile-basics (get map-key tile-name)]
                  (assoc tile-basics :position {:x (+ x (* x tile-size)) :y (+ y (* y tile-size))})))]
    (->> tiles
         (filter #(#{:spawn-player-1 :spawn-player-2 :spawn-player-3 :spawn-player-4} (:type %)))
         (reduce (fn [players {:keys [size position type]}]
                   (let [player-id (-> type name last str keyword)
                         size' (update size :x #(int (/ % 2)))
                         position (update position :x #(+ % (int (/ (:x size) 4))))]
                     (assoc players player-id {:size size'
                                               :position position
                                               :type :player
                                               :colour :red}))) {}))))

#_(defn random-bricks
  [basic-map]
  (let [basic-map (->> basic-map
                       (map seq)
                       (map (partial map (comp keyword #(when-not (string/blank? %) %) str))))
        width (-> basic-map first seq count)
        height (-> basic-map count)
        tiles (for [y (range height)
                    x (range width)]
                (let [shorthand (nth (nth basic-map y) x)
                      tile-name (get shorthand->tile-name shorthand)
                      tile-basics (get map-key tile-name)]
                  (assoc tile-basics :position {:x (+ x (* x tile-size)) :y (+ y (* y tile-size))})))]
    (->> tiles
         (filter #(= :empty-square (:type %)))
         (remove #(= :free-square (:type %)))
         (filter (fn [_]
                   (pos? (rand-int 4))))
         (reduce (fn [bricks {:keys [size position]}]
                   (conj bricks {:size size
                                 :position position
                                 :type :brick
                                 :colour :brown})) []))))


(defn setup []
  (q/frame-rate 30)
  (let [state (db/init-state)]
    (-> state
        (db/assoc-background-image (map/generate state))
        ))
  #_{:background-image (generate-background basic-map)
   :sprites {:bricks (random-bricks basic-map)
             :players (players basic-map)}})


(defn update-state
  [state]
  state
  )

(defn square
  [{:keys [size position colour]}]
  (let [{:keys [x y]} position
        {height :y width :x} size]
    (apply q/fill (get gui/colours colour))
    (q/rect x y width height))
  )

(defn player [{:keys [size position colour] :as player}]
  (square player)
  )

(defn draw-state [state]
  (q/set-image 0 0 (db/background-image state))
#_  (let [{:keys [bricks players]} sprites]
    (doseq [brick bricks]
      (square brick))
    (doseq [p (vals players)]
      (player p))

    )

  )

(defn key-pressed
  [& args ]
  (log/info "key-pressed" args)
  (first args)
  )

(defn key-released
  [& args]
  (log/info "key-released" key)
  args)

; this function is called in index.html
(defn ^:export run-sketch []
  (q/defsketch game
               :host "game"
               :size [500 500]
               ; setup function called only once, during sketch initialization.
               :setup setup
               ; update-state is called on each iteration before draw-state.
               :update update-state
               :draw draw-state
               ; This sketch uses functional-mode middleware.
               ; Check quil wiki for more info about middlewares and particularly
               ; fun-mode.
               :middleware [m/fun-mode]

               :key-pressed key-pressed
               :key-released key-released

               ))

; uncomment this line to reset the sketch:
; (run-sketch)
