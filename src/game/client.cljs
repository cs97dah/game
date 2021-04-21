(ns game.client
  (:require [game.core :as game]
            [taoensso.timbre :as log]
            [game.websockets :as websockets]))

(game/run-sketch)

(defonce _start-once (websockets/start!))
