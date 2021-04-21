(ns game.handler.websocket
  (:require
    [compojure.core :refer :all]
    [clojure.java.io :as io]
    [hiccup.core :refer [html]]
    [integrant.core :as ig]
    [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]

    [taoensso.sente :as sente]
    [taoensso.encore :as enc]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
    [taoensso.timbre :as log]
    [medley.core :as medley]


    ;; <other stuff>
    [taoensso.sente :as sente]                              ; <--- Add this

    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]] ; <--- Recommended

    ;; Uncomment a web-server adapter --->
    ;; [taoensso.sente.server-adapters.http-kit      :refer (get-sch-adapter)]
    ;; [taoensso.sente.server-adapters.immutant      :refer (get-sch-adapter)]
    ;; [taoensso.sente.server-adapters.nginx-clojure :refer (get-sch-adapter)]
    ;; [taoensso.sente.server-adapters.aleph         :refer (get-sch-adapter)]
    [integrant.core :as ig]
    [ring.middleware.cors :as cors]))


;;; Add this: --->
(defonce channel-socket (sente/make-channel-socket! (get-sch-adapter) {}))
(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]} channel-socket]

  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)                                     ; ChannelSocket's receive channel
  (def chsk-send! send-fn)                                  ; ChannelSocket's send API fn
  (def connected-uids connected-uids)                       ; Watchable, read-only atom
  )


(defmethod ig/init-key
  :game.handler.websocket/websockets [_ options]
  (-> (context "/websockets" []
        (GET "/chsk" req
          (do
            (log/info "GET" req)
            (let [response (ring-ajax-get-or-ws-handshake req)]
              (log/info "Response>" response)
              response)))
        (POST "/chsk" req (ring-ajax-post req)))
      ;; Add necessary Ring middleware:
      (ring.middleware.defaults/wrap-defaults (assoc-in ring.middleware.defaults/site-defaults [:security :anti-forgery] false))
      (cors/wrap-cors :access-control-allow-origin [#".*"]
                      :access-control-allow-methods [:get :put :post :delete]
                      :access-control-allow-credentials ["true"]))
  )


(defmulti event :id)

(defmethod event :default [{:as ev-msg :keys [event]}]
  (println "Unhandled event: " event))

(defn start-router []
  (defonce router
           (sente/start-chsk-router! (:ch-recv channel-socket) event)))
