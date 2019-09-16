(ns myproject.core
  (:require [clojure.java.io :as io]
            [clojure.stacktrace :refer [print-stack-trace]]
            [clojure.pprint :refer [pprint]]
            [com.stuartsierra.component :as component]
            ;; Ring
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            ;; Compojure
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources not-found]]
            [environ.core :refer [env]]
            ;; Logging
            [taoensso.timbre :as log]
            ;; Web
            [ring.middleware.defaults :refer :all]
            [ring.middleware.stacktrace :as trace]
            [prone.middleware :as prone]
            [aleph [netty] [http]]
            [compojure.route :as route]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.aleph :refer [get-sch-adapter]]
            [taoensso.sente.packers.transit :as sente-transit]
            ;; Internal
            [myproject.actions :as actions]
            [myproject.html :as html])
  (:import java.lang.Integer
           java.net.InetSocketAddress)
  (:gen-class))


(log/set-level! :debug)

;;
;; Sente setup
;;

(let [packer (sente-transit/get-transit-packer)
      {:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {:packer packer})]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)              ; ChannelSocket's receive channel
  (def chsk-send! send-fn)           ; ChannelSocket's send API fn
  (def connected-uids connected-uids))

(defn user-home [req]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body html/index})

(defroutes app
  (GET "/" req user-home)
  (resources "/")
  ;; Sente
  (GET "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req))
  (not-found "Woooot? Not found!"))

;;
;; HTTP Server
;;

(defn- new-server [ip port]
  (aleph.http/start-server
   (-> app
       prone/wrap-exceptions
       (wrap-defaults (assoc-in (if (env :production) secure-site-defaults site-defaults)
                                [:params :keywordize] true))
       wrap-gzip)
   {:port (Integer. (or port (env :port) 5000))
    :socket-address (when ip (new InetSocketAddress ip port))}))

(defrecord Server [aleph ip port]
  component/Lifecycle
  (start [this]
    (let [a (new-server ip port)]
      (println "Server started.")
      (assoc this :aleph a)))
  (stop [this]
    (.close (:aleph this))
    (println "Server closed.")
    (assoc this :aleph nil)))

;;
;; Sente Websockets event router (`event-msg-handler` loop)
;;

(defrecord WebsocketsRouter [sente]
  component/Lifecycle
  (start [this]
    (assoc this
           :sente
           (sente/start-server-chsk-router! ch-chsk actions/event-msg-handler)))
  (stop [this]
    ((:sente this))
    (assoc this :sente nil)))

;;
;; System (puts together the components)
;;

(defn system [& [config-options]]
  (let [{:keys [ip port]} config-options]
    (component/system-map
     :server (Server. nil ip port)
     :websockets-router (WebsocketsRouter. nil))))

(defn -main [& [port ip]]
  (let [s (system {:ip ip :port port})]
    (component/start s)
    (aleph.netty/wait-for-close (get-in s [:server :aleph]))))
