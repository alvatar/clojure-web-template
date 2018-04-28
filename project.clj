(defproject myproject "0.1.0-SNAPSHOT"
  :description "myproject"
  :url "http://myproject.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.126"]
                 [com.cognitect/transit-clj "0.8.300"]
                 [com.cognitect/transit-cljs "0.8.243"]
                 [environ "1.1.0"]
                 [ring "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [bk/ring-gzip "0.3.0"]
                 [prone "1.5.0"]
                 [aleph "0.4.4"]
                 [compojure "1.6.0"]
                 [com.taoensso/encore "2.94.0"]
                 [com.taoensso/sente "1.12.0"]
                 [com.taoensso/timbre "4.10.0"]
                 ;; Database
                 [org.clojure/java.jdbc "0.7.5"]
                 [org.postgresql/postgresql "42.2.1"]
                 ;; HTML
                 [hiccup "1.0.5"]
                 [garden "1.3.4"]
                 ;; Cljs
                 [rum "0.11.2"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-environ "1.1.0"]]

  :jvm-opts ["--add-modules" "java.xml.bind"]
  
  :min-lein-version "2.7.1"

  :source-paths ["src/clj" "src/cljs" "src/cljc"]

  :test-paths ["test/clj" "test/cljc"]

  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/js"]

  :uberjar-name "myproject-server.jar"

  ;; Use `lein run` if you just want to start a HTTP server, without figwheel
  :main myproject.server

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (run) and
  ;; (browser-repl) live.
  :repl-options {:init-ns user}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs" "src/cljc"]
                :figwheel true
                ;; Alternatively, you can configure a function to run every time figwheel reloads.
                ;; :figwheel {:on-jsload "myproject.core/on-figwheel-reload"}
                :compiler {:main myproject.core
                           :asset-path "js/out"
                           :output-to "resources/public/js/myproject.js"
                           :output-dir "resources/public/js/out"
                           :source-map-timestamp true}}
               {:id "test"
                :source-paths ["src/cljs" "test/cljs" "src/cljc" "test/cljc"]
                :compiler {:output-to "resources/public/js/testable.js"
                           :main myproject.test-runner
                           :optimizations :none}}
               {:id "min"
                :source-paths ["src/cljs" "src/cljc"]
                :jar true
                :compiler {:main myproject.core
                           :output-to "resources/public/js/myproject.js"
                           :output-dir "target"
                           :source-map-timestamp false
                           :optimizations :advanced
                           :pretty-print false}}]}
  ;; When running figwheel from nREPL, figwheel will read this configuration
  ;; stanza, but it will read it without passing through leiningen's profile
  ;; merging. So don't put a :figwheel section under the :dev profile, it will
  ;; not be picked up, instead configure figwheel here on the top level.
  :figwheel {;; :http-server-root "public"       ;; serve static assets from resources/public/
             ;; :server-port 3449                ;; default
             ;; :server-ip "127.0.0.1"           ;; default
             :css-dirs ["resources/public/css"]  ;; watch and update CSS
             ;; Instead of booting a separate server on its own port, we embed
             ;; the server ring handler inside figwheel's http-kit server, so
             ;; assets and API endpoints can all be accessed on the same host
             ;; and port. If you prefer a separate server process then take this
             ;; out and start the server with `lein run`.
             :ring-handler user/http-handler
             ;; Start an nREPL server into the running figwheel process. We
             ;; don't do this, instead we do the opposite, running figwheel from
             ;; an nREPL process, see
             ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
             ;; :nrepl-port 7888
             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"
             :server-logfile "log/figwheel.log"}

  :doo {:build "test"}

  :profiles {:dev
             {:dependencies [[figwheel "0.5.15"]
                             [figwheel-sidecar "0.5.15"]
                             [com.cemerick/piggieback "0.2.2"]
                             [org.clojure/tools.nrepl "0.2.13"]
                             [midje "1.9.1"]]
              :plugins [[lein-figwheel "0.5.15"]
                        [lein-doo "0.1.6"]
                        [lein-ancient "0.6.15"]
                        [lein-midje "3.2.1"]]
              :source-paths ["dev"]
              :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
              :env {:env "dev"}}
             :test
             {:dependencies [[midje "1.9.1"]]
              :plugins [[lein-midje "3.2.1"]]
              :env {:env "test"}}
             :uberjar
             {:source-paths ^:replace ["src/clj" "src/cljc"]
              :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
              :omit-source true
              :aot :all
              :env {:env "uberjar"}}})
