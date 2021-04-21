(ns game.handler.game
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [hiccup.core :refer [html]]
            [integrant.core :as ig]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
[ring.util.anti-forgery :refer [anti-forgery-field]]
            [taoensso.sente :as sente]
            [taoensso.encore :as enc]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [taoensso.timbre :as log]
            [medley.core :as medley]))


(defn main-page []
  (let [csrf-token (force *anti-forgery-token*)]

    (log/info "generated csrf-token" csrf-token)
    [:html {:lang "en" :class "example"}
     [:head
      [:title "Game"]
      [:link {:rel "stylesheet"
              :href "/assets/normalize.css/normalize.css"}]
      [:link {:rel "stylesheet"
              :href "/css/site.css"}]]
     [:body
      (let [csrf-token (force ring.middleware.anti-forgery/*anti-forgery-token*)]
        [:div#sente-csrf-token {:data-csrf-token csrf-token}])
      [:div#game-div]
      [:div#sente-csrf-token {:data-csrf-token csrf-token}]
      [:div [:p "Hello"]]
      ;[:script {:src "/js/main.js"}]
      ]]))

(defmethod ig/init-key :game.handler/game [_ options]
  (context "" []
    (GET "/" []
      (log/info "Returning page:")
      (html (main-page)))))

