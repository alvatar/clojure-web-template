(ns myproject.core
  (:require
   [taoensso.encore :as encore :refer-macros (have have?)]
   [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
   [cljs.core.async :as async :refer (<! >! put! take! chan)]
   [taoensso.sente :as sente :refer (cb-success?)]
   [taoensso.sente.packers.transit :as sente-transit]
   [cljs-react-material-ui.core :refer [get-mui-theme color]]
   [cljs-react-material-ui.icons :as ic]
   [cljs-react-material-ui.rum :as ui]
   [rum.core :as rum]))



(goog-define *is-dev* false)

(enable-console-print!)

;;
;; Utils
;;

(defn clj->json [ds] (.stringify js/JSON (clj->js ds)))

;;
;; Sente
;;

(let [packer (sente-transit/get-transit-packer)
      {:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" {:type :auto
                                           :packer packer})]
  (def chsk chsk)
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)) ; Watchable, read-only atom

(defn app []
  (ui/mui-theme-provider
   {:mui-theme (get-mui-theme {:palette {:text-color (color :blue600)}})}
   [:div {:style {:position "absolute"
                  :max-width "700px" :height "300px"
                  :margin "auto" :top "0" :bottom "0" :left "0" :right "0"}}
    (ui/paper
     [:div
      [:h2 {:style {:text-align "center"}} "Main"]
      [:h4 {:style {:text-align "center"}} "My app"]
      [:div {:style {:text-align "center"}}
       (ui/raised-button {:label "Login"
                          :style {:margin "1rem"}
                          :on-touch-tap
                          (fn [e] (chsk-send!
                                   [:user/store {:user-id 1}] 5000
                                   #(js/console.log "Received: " (clj->js %))))})]])]))

(rum/mount (app) (js/document.getElementById "app"))
