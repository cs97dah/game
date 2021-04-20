(ns game.db)

(def path-gui-info [:gui])
(def path-background-image (conj path-gui-info :background-image))
(def path-bricks [:bricks])
(def path-bricks-by-coordinates [:bricks-by-coords])
(def path-walls [:walls])
(def path-walls-by-coordinates [:walls-by-coords])
(def path-players [:players])
(def path-bombs [:bombs])
(def path-explosions [:explosions])
(def path-bomb-power-ups [:bomb-power-ups])
(def path-speed-power-ups [:speed-power-ups])
(def path-bombs-by-coordinates [:bombs-by-coords])
(defn path-player-id [player-id] (conj path-players player-id))
(def path-keys-pressed [:keys-pressed])
(def path-time [:time])
(def path-time-now (conj path-time :now))
(def path-time-previous (conj path-time :previous))
(def path-game-state [:game-state])

(defn gui-info
  [state]
  (get-in state path-gui-info))

(defn assoc-background-image
  [state image]
  (assoc-in state path-background-image image))

(defn assoc-wall-by-coordinates
  [state {:keys [coordinates] :as wall}]
  (assoc-in state (conj path-walls-by-coordinates coordinates) wall))

(defn assoc-walls
  [state walls]
  (let [state (assoc-in state path-walls walls)]
    ;; TODO: Can the above be scrapped to just have this instead?
    (reduce assoc-wall-by-coordinates state walls)))

(defn dissoc-brick
  [state {:keys [coordinates] :as brick}]
  (-> state
      (update-in path-bricks-by-coordinates dissoc coordinates)
      (update-in path-bricks disj brick)))

(defn wall-at?
  [state coordinates]
  (get-in state (conj path-walls-by-coordinates coordinates)))

(defn assoc-brick-by-coordinates
  [state {:keys [coordinates] :as brick}]
  (assoc-in state (conj path-bricks-by-coordinates coordinates) brick))

(defn assoc-bricks
  [state bricks]
  (let [state (assoc-in state path-bricks bricks)]
    ;; TODO: Can the above be scrapped to just have this instead?
    (reduce assoc-brick-by-coordinates state bricks)))

(defn assoc-bomb-power-ups
  [state bomb-power-ups]
  (assoc-in state path-bomb-power-ups bomb-power-ups))

(defn bomb-power-ups
  [state]
  (get-in state path-bomb-power-ups))

(defn dissoc-bomb-power-up
  [state power-up]
  (update-in state path-bomb-power-ups disj power-up))

(defn assoc-speed-power-ups
  [state speed-power-ups]
  (assoc-in state path-speed-power-ups speed-power-ups))

(defn speed-power-ups
  [state]
  (get-in state path-speed-power-ups))

(defn dissoc-speed-power-up
  [state power-up]
  (update-in state path-speed-power-ups disj power-up))

(defn brick-at?
  [state coordinates]
  (get-in state (conj path-bricks-by-coordinates coordinates)))

(defn assoc-players
  [state players]
  (assoc-in state path-players players))

(defn players
  [state]
  (get-in state path-players))

(defn bricks
  [state]
  (get-in state path-bricks))

(defn walls
  [state]
  (get-in state path-walls))

(defn background-image
  [state]
  (get-in state path-background-image))

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

(defn player
  [state player-id]
  (get-in state (path-player-id player-id)))

(defn assoc-game-state
  [state game-state]
  (assoc-in state path-game-state game-state))

(defn player-dead
  [state player-id]
  (let [state (update-in state (path-player-id player-id) assoc :dead? true)
        remaining-players (->> (get-in state path-players)
                               (vals)
                               (remove :dead?))]
    (cond-> state
      (nil? (second remaining-players))
      (assoc-game-state :game-over))))

(defn bomb-power-up
  [state player-id]
  (update-in state (conj (path-player-id player-id) :bomb-strength) inc))

(defn speed-power-up
  [state player-id]
  (update-in state (conj (path-player-id player-id) :speed-multiplier) #(* 1.2 %)))

(defn bombs
  [state]
  (get-in state path-bombs))

(defn assoc-bomb
  [state {:keys [coordinates] :as bomb}]
  (-> state
      ;; TODO just put bombs in this coords map
      (update-in path-bombs set-conj bomb)
      (assoc-in (conj path-bombs-by-coordinates coordinates) bomb)))

(defn- millis []
  ;; TODO: Keep a game time in state
  #?(:clj  (System/currentTimeMillis)
     :cljs (.getTime (js/Date.))))

(defn game-time
  [state]
  (get-in state path-time-now))

(defn tick-game-time
  [state]
  (-> state
      (assoc-in path-time-previous (game-time state))
      (assoc-in path-time-now (millis))))

(defn game-time-plus-millis
  [state millis]
  (+ (game-time state) millis))

(defn delta-time
  [state]
  (/ (- (get-in state path-time-now) (get-in state path-time-previous)) 1000))

(defn init-state
  [map-size tile-size move-pixels-per-second]
  (-> {}
      (assoc-in path-gui-info {:map-size map-size
                               :tile-size tile-size
                               :move-pixels-per-second move-pixels-per-second})
      (tick-game-time)))

(defn dissoc-bomb
  [state {:keys [coordinates] :as bomb}]
  (-> state
      (update-in path-bombs disj bomb)
      (update-in path-bombs-by-coordinates dissoc coordinates)))

(defn assoc-explosions
  [state explosions]
  (update-in state path-explosions #(apply set-conj % explosions)))

(defn explosions
  [state]
  (get-in state path-explosions))

(defn dissoc-explosions
  [state explosions]
  (update-in state path-explosions #(apply disj % explosions)))

(defn get-potential-object
  [state potential-coordinates]
  (or (get-in state (conj path-bombs-by-coordinates potential-coordinates))
      (get-in state (conj path-walls-by-coordinates potential-coordinates))
      (get-in state (conj path-bricks-by-coordinates potential-coordinates))))

(defn game-state
  [state]
  (get-in state path-game-state))

(defn winner
  [state]
  (let [remaining-players (->> (get-in state path-players)
                               (vals)
                               (remove :dead?))]
    (if (empty? remaining-players)
      "It was a draw!"
      (str "Player " (-> remaining-players first :player-id inc) " wins!"))))
