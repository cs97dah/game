(ns game.sprites.player
  (:require [clojure.set :as set]
            [game.db :as db]
            [game.gui :as gui]
            [game.sprites.bomb :as bomb]
            [game.sprites.core :as sprites]
            [medley.core :as medley]
            [quil.core :as q]
            [taoensso.timbre :as log]))

(def player-colours
  {0 (gui/colour :red)})

(def player-keys
  {0 {:ArrowUp :up
      :ArrowDown :down
      :ArrowLeft :left
      :ArrowRight :right
      :space :bomb}})

(def keys-for-player (->> player-keys
                          (medley/map-vals #(-> % keys set))))

(def relevant-keys (->> player-keys
                        (vals)
                        (mapcat keys)
                        (set)))

(def bomb-key-for-player (->> player-keys
                              (medley/map-vals #(reduce (fn [_ [key action]]
                                                          (when (= action :bomb)
                                                            (reduced key))) nil %))))

(defrecord Player
  [position size player-id bomb-strength dead? speed-multiplier]
  sprites/Sprite

  (render [_]
    ;; TODO: Render something when the player is dead
    (when-not dead?
      (let [{:keys [x y]} position]
        (apply q/fill (get player-colours player-id))
        (q/no-stroke)
        (q/rect x y (:x size) (:y size))))))

(defn create
  [player-id position tile-size]
  (let [size (-> tile-size
                 (update :x quot 2)
                 (update :y #(* 3 (quot % 4))))
        position (-> position
                     (update :x + (quot (:x size) 2))
                     (update :y + (- (:y tile-size) (:y size))))]
    (map->Player {:position position
                  :size size
                  :player-id player-id
                  :bomb-strength 1
                  :speed-multiplier 1})))

(defn player-commands
  [state player-id]
  (let [keys-pressed-relevant-to-player (set/intersection (db/keys-pressed state) (keys-for-player player-id))
        directions-for-keys (select-keys (get player-keys player-id) keys-pressed-relevant-to-player)]
    (set (vals directions-for-keys))))

(def move-vectors
  {:up {:x 0 :y -1}
   :down {:x 0 :y 1}
   :left {:x -1 :y 0}
   :right {:x 1 :y 0}})

(defn sqrt
  [x]
  #?(:clj  (Math/sqrt x)
     :cljs (.sqrt js/Math x)))

(defn coordinates-to-measure-from
  [{:keys [position size] :as player} direction tile-size]
  (sprites/coordinates
    (case direction
      (:up :left) position
      (merge-with + position size {:x -1 :y -1}))
    tile-size))

(defn player-directions
  [state player-id]
  (let [directions (disj (player-commands state player-id) :bomb)]
    (not-empty
      (cond-> directions
       (and (:up directions)
            (:down directions))
       (disj :up :down)

       (and (:left directions)
            (:right directions))
       (disj :left :right)))))

(defn abs
  [x]
  (cond-> x
    (neg? x)
    (* -1)))

(defn move-player
  [state {:keys [player-id speed-multiplier position size] :as player}]
  (if-let [directions (player-directions state player-id) ]
    (let [direction-map (->> directions
                             (map move-vectors)
                             (apply merge-with +))          ;; TODO: Could be net zero - no need to recalculate this in which case
          ;_ (log/info "Request to move in direction" direction)
          {:keys [tile-size]} (db/gui-info state)
          potential-object-distances (reduce (fn [potential-object-distances direction]
                                               (let [coordinates (coordinates-to-measure-from player direction tile-size)
                                                     potential-coordinates (merge-with + coordinates (get move-vectors direction))
                                                     potential-object (db/get-potential-object state potential-coordinates)]
                                                 ;(log/info "My coordinates" coordinates)
                                                 ;(log/info "My potential-coordinates" potential-coordinates)
                                                 ;(log/info "My potential-blocking-object" (db/get-potential-object state potential-coordinates))
                                                 ;  (medley/assoc-some potential-objects direction potential-object)
                                                 (if-not potential-object
                                                   potential-object-distances
                                                   (do
                                                     ;(log/info "My position + size" position size)
                                                     ;(log/info "My coordinates" coordinates)
                                                     ;(log/info "potential-object" potential-object)
                                                     (let [potential-object-distances
                                                           (case direction
                                                             :up
                                                             (assoc potential-object-distances :y (- (:y position) (+ (-> potential-object :position :y) (-> potential-object :size :y) #_ -1)))

                                                             :down
                                                             (assoc potential-object-distances :y (- (-> potential-object :position :y) (+ (:y position) (:y size)#_ -1)))

                                                             :left
                                                             (assoc potential-object-distances :x (- (:x position) (+ (-> potential-object :position :x) (-> potential-object :size :x)#_ -1)))

                                                             :right
                                                             (assoc potential-object-distances :x (- (-> potential-object :position :x) (+ (:x position) (:x size) #_-1))))]
                                                       ; (log/info "potential-object-distances" potential-object-distances)
                                                       potential-object-distances)))))
                                             nil directions)
          {:keys [move-pixels-per-second]} (db/gui-info state)
          ;_ (log/info  "move-pixels-per-second"move-pixels-per-second )
          ;_ (log/info   "speed-multiplier"speed-multiplier )
          ;_ (log/info  "(db/delta-time state)"(db/delta-time state))
          straight-line-distance (* move-pixels-per-second speed-multiplier (db/delta-time state))
          ideal-move-map (if (or (zero? (:x direction-map))
                                 (zero? (:y direction-map)))
                           (medley/map-vals #(* straight-line-distance %) direction-map)
                           (let [distance-in-each-direction (sqrt (/ (* straight-line-distance straight-line-distance) 2))]
                             (medley/map-vals #(* distance-in-each-direction %) direction-map)))]
      (if potential-object-distances
        ;; TODO: Fix this - there might be some left over distance that could be
        ;; used here - seen when running along a wall and trying to move into it
        ;; at the same time - should move at the same speed as just running down
        ;; it but doesn't (it's slower)
        (let [x (if-let [potential-x-distance (:x potential-object-distances)]
                  (let [ideal-x-move (abs (:x ideal-move-map))]
                    (cond-> (min ideal-x-move potential-x-distance)
                      (neg? (:x ideal-move-map))
                      (* -1)))
                  (:x ideal-move-map))
              y (if-let [potential-y-distance (:y potential-object-distances)]
                  (let [ideal-y-move (abs (:y ideal-move-map))]
                    (cond-> (min ideal-y-move potential-y-distance)
                      (neg? (:y ideal-move-map))
                      (* -1)))
                  (:y ideal-move-map))
              actual-move-map {:x x :y y}
              state (update-in state (conj (db/path-player-id player-id) :position) #(merge-with + % actual-move-map))]
          state)
        (update-in state (conj (db/path-player-id player-id) :position) #(merge-with + % ideal-move-map))))
    state))

;; TODO: This fn should live in the bomb namespace
(defn proposed-bomb-position-coordinates-and-size
  [state {:keys [position size] :as player}]
  (let [centre-of-player (-> position
                             (update :x + (quot (:x size) 2))
                             (update :y + (quot (:y size) 2)))
        {:keys [tile-size]} (db/gui-info state)
        coordinates (sprites/coordinates centre-of-player tile-size)
        position (sprites/position-of-coordinates state coordinates)]
    {:position position
     :coordinates coordinates
     :size tile-size}))

(defn laying-bomb?
  [state player-id]
  (:bomb (player-commands state player-id)))

(defn can-lay-bomb?
  [state proposed-bomb-position-and-size]
  (let [bombs (db/bombs state)]
    (not (sprites/sprite-intersects proposed-bomb-position-and-size bombs))))

(defn lay-bomb*
  [state {:keys [player-id] :as player}]
  (let [player (db/player state player-id)
        proposed-bomb-position-coordinates-and-size (proposed-bomb-position-coordinates-and-size state player)]
    (-> (cond-> state
          (can-lay-bomb? state proposed-bomb-position-coordinates-and-size)
          (db/assoc-bomb (bomb/create state player proposed-bomb-position-coordinates-and-size)))
        (db/dissoc-key-pressed (bomb-key-for-player player-id)))))

(defn lay-bomb
  [state {:keys [player-id] :as player}]
  (cond-> state
    (laying-bomb? state player-id)
    (lay-bomb* player)))

(defn remove-if-dead
  [state {:keys [player-id] :as player} explosions]
  (cond-> state
    (sprites/sprite-intersects player explosions)
    (db/player-dead player-id)))

(defn power-up
  [state {:keys [player-id] :as player}]
  ;; TODO: This could all be more efficient by working off coordinates
  (let [bomb-power-up (sprites/sprite-intersects player (db/bomb-power-ups state))
        speed-power-up (when-not bomb-power-up
                         (sprites/sprite-intersects player (db/speed-power-ups state)))]
    (cond-> state
      bomb-power-up
      (->
        (db/bomb-power-up player-id)
        (db/dissoc-bomb-power-up bomb-power-up))

      speed-power-up
      (->
        (db/speed-power-up player-id)
        (db/dissoc-speed-power-up speed-power-up)))))
