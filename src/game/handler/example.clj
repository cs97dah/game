(ns game.handler.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [integrant.core :as ig]))

(defmethod ig/init-key :game.handler/example [_ options]
  (context "/example" []
    (GET "/" []
      (io/resource "game/handler/example/example.html"))))
