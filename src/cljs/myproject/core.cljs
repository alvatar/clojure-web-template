(ns myproject.core
  (:require
   [taoensso.encore :as encore :refer-macros (have have?)]
   [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
   [cljs.core.async :as async :refer (<! >! put! take! chan)]
   [taoensso.sente :as sente :refer (cb-success?)]
   [taoensso.sente.packers.transit :as sente-transit]
   [reagent.core :as r]
   [posh.reagent :as p]
   [datascript.core :as d]
   [garden.core :refer [css]]
   ;; -----
   [myproject.utils :as utils :refer [log*]]
   [myproject.client :as client]))


(goog-define *is-dev* false)

(enable-console-print!)
(timbre/set-level! :debug)

;;
;; UI globals
;;

(def db-schema {})
(def db-conn (d/create-conn db-schema))
(p/posh! db-conn)

(d/transact! db-conn
             [{:user/name "Alvatar"}])

;;
;; Utils
;;

(defn clj->json [ds] (.stringify js/JSON (clj->js ds)))

;;
;; Sente
;;

(defonce router_ (atom nil))

(declare event-msg-handler)

;;
;; Event Handlers
;;

(defmethod client/-event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (when (:first-open? new-state-map)
      (log* new-state-map))))

(defmethod client/-event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?user ?csrf-token ?handshake-data] ?data]
    ;; (reset! (:user-id app-state) ?user)
    (when-not (= ?user :taoensso.sente/nil-uid)
      (log* "HANDSHAKE"))))

;;
;; UI Components
;;

(def common-style
  (css [:h1 {:font-weight "bold"}]))

(defonce style-node
  (let [node (js/document.createElement "style")]
    (js/document.head.appendChild node)
    node))

(aset style-node "innerHTML" style)

(defn app []
  [:section.section>div.container
   [:h1.title
    (str "HELLO "
         @(p/q '[:find ?n .
                 :where [?e]
                 [?e :user/name ?n]]
               db-conn)
         "!")]
   [:p.subtitle "Let's go!"]])

;;
;; Init
;;

(r/render [app] (js/document.getElementById "app"))

(client/start-router!)
