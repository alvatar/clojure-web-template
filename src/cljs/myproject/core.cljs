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
   [goog.style]
   ;; -----
   [myproject.globals :as globals :refer [db-conn display-type]]
   [myproject.utils :as utils :refer [log*]]
   [myproject.client :as client]))


(goog-define *is-dev* false)

(enable-console-print!)
(timbre/set-level! :debug)

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

(def styles
  (css [:h1 {:font-weight "bold"}]))

(defonce style-node (atom nil))
(if @style-node
  (goog.style/setStyles @style-node styles)
  (reset! style-node (goog.style/installStyles styles)))

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
