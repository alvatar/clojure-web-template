(ns user
  (:require [myproject.server]
            [ring.middleware.reload :refer [wrap-reload]]
            [figwheel-sidecar.repl-api :as figwheel]
            [clojure.java.shell]))

;; Let Clojure warn you when it needs to reflect on types, or when it does math
;; on unboxed numbers. In both cases you should add type annotations to prevent
;; degraded performance.
(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(def http-handler
  (wrap-reload #'myproject.server/app))

(defn start-less []
  (future
    (println "Starting less.")
    (clojure.java.shell/sh "lein" "less" "auto")))

(defn run []
  (figwheel/start-figwheel!)
  (start-less)
  (myproject.server/stop!)
  (myproject.server/start!)
  (in-ns 'myproject.server))

(def browser-repl figwheel/cljs-repl)
