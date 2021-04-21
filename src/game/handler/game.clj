(ns game.handler.game
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [integrant.core :as ig]))

(defmethod ig/init-key :game.handler/game [_ options]
  (context "" []
    (GET "/" []
      (io/resource "game/main.html"))))

