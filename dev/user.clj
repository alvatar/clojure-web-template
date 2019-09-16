(ns user
  (:require [ring.middleware.reload :refer [wrap-reload]]
            [figwheel-sidecar.repl-api :as figwheel]
            [clojure.java.shell]
            [com.stuartsierra.component :as component]
            [myproject.core]))

;; Let Clojure warn you when it needs to reflect on types, or when it does math
;; on unboxed numbers. In both cases you should add type annotations to prevent
;; degraded performance.
(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(def http-handler (wrap-reload #'myproject.core/app))

;; (defn start-less []
;;   (future
;;     (println "Starting less.")
;;     (clojure.java.shell/sh "lein" "less" "auto")))

(defn run []
  ;; (start-less)
  (figwheel/start-figwheel!)
  (def system (myproject.core/system))
  (alter-var-root #'system component/start)
  (in-ns 'myproject.core))

;; To stop
;; (component/stop user/system)
;; To restart
;; (component/start user/system)

(def browser-repl figwheel/cljs-repl)
