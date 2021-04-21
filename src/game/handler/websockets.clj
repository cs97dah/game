(ns game.handler.websockets
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [taoensso.timbre :as log]
            [taoensso.sente :as sente]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(reset! sente/debug-mode?_ true)

(defmethod ig/init-key ::websockets [_ options]
  (log/info "Init" ::websockets)
  (context "/not-this" []
    (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
    (POST "/chsk" req (ring-ajax-post                req))))
