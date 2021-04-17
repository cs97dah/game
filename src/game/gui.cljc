(ns game.gui
  (:require [quil.core :as q]))

(def colours {:light-green [0 163 117]
              :peach [226 133 110]
              :blue [16 37 66]
              :yellow [253 231 76]
              :purple [142 141 190]
              :light-blue [57 160 237]
              :green [30 63 32]
              :red [154 41 15]
              :black [0 0 0]
              :grey [209 226 240]
              :brown [122 68 25]})

(defn colour
  [colour]
  (get colours colour))
