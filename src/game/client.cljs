(ns game.client
  (:require-macros
    [cljs.core.async.macros :as asyncm :refer [go go-loop]])
  (:require
    ;; <other stuff>
    [cljs.core.async :as async :refer [<! >! put! chan]]
    [taoensso.sente  :as sente :refer [cb-success?]] ; <--- Add this
    ))

(def ?csrf-token
  (when-let [el (.getElementById js/document "sente-csrf-token")]
    (.getAttribute el "data-csrf-token")))

(defonce channel-socket-client (sente/make-channel-socket-client!
                                 "/websockets/chsk" ; Note the same path as before
                                 ?csrf-token
                                 {:type :auto ; e/o #{:auto :ajax :ws}
                                  }))

(let [{:keys [chsk ch-recv send-fn state]}channel-socket-client]

  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

