(ns game.handler.game
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [hiccup.core :refer [html]]
            [integrant.core :as ig]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]

            [taoensso.sente :as sente]
            [taoensso.encore :as enc]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [taoensso.timbre :as log]
            [medley.core :as medley]))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) { :csrf-token-fn  nil})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(log/set-level! :trace)
(reset! sente/debug-mode?_ true)

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
      [:div#game-div]
      [:div#sente-csrf-token {:data-csrf-token csrf-token}]
      [:script {:src "/js/main.js"}]]]))

(defmethod ig/init-key :game.handler/game [_ options]
  (-> (context "" []
        (GET "/" []
          (html (main-page)))
        (GET "/chsk" req
          (let [{:keys [anti-forgery-token]} req
                req (update-in req [:headers] assoc "x-csrf-token" anti-forgery-token)
                ;   _ (log/info "Here>" (select-keys req [:headers :anti-forgery-token])) ;; Work around the auth failure - why is this happening?
                response (ring-ajax-get-or-ws-handshake req)]
            ;  _ (log/info "Here2>" req)
            ;(log/info "Response>" response)
            response

            ))
        (POST "/chsk" req (ring-ajax-post req)))
      (ring.middleware.defaults/wrap-defaults ring.middleware.defaults/site-defaults)
      ;(ring.middleware.anti-forgery/wrap-anti-forgery)
      )

  )

