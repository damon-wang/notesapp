(defproject notesapp "0.1.0"

  :description "A practical notes app for daily usage"
  :url "http://damon-wang.com/notesapp"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [clj-ns-browser "1.3.1"]
                 ;; [alembic "0.3.2"]
                 ;; [spyscope "0.1.4"]
                 ;; [im.chit/vinyasa "0.4.2"]
                 [clj-time "0.9.0"]
                 [figwheel-sidecar "0.4.1"]
                 [selmer "0.9.4"]
                 [com.taoensso/timbre "4.1.4"]
                 [com.taoensso/tower "3.0.2"]
                 [markdown-clj "0.9.78"]
                 [environ "1.0.1"]
                 [compojure "1.4.0"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.1.5"]
                 [ring "1.4.0"
                  :exclusions [ring/ring-jetty-adapter]]
                 [metosin/ring-middleware-format "0.6.0"]
                 [metosin/ring-http-response "0.6.5"]
                 [bouncer "0.3.3"]
                 [prone "0.8.2"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.webjars/bootstrap "3.3.5"]
                 [org.webjars/jquery "2.1.4"]
                 [migratus "0.8.7"]
                 [conman "0.2.4"]
                 [org.postgresql/postgresql "9.4-1203-jdbc41"]
                 [org.clojure/clojurescript "1.7.145" :scope "provided"]
                 [org.clojure/tools.reader "0.10.0"]
                 [reagent "0.5.1"]
                 [reagent-forms "0.5.13"]
                 [reagent-utils "0.1.5"]
                 [re-frame "0.5.0"]
                 [secretary "1.2.3"]
                 [org.clojure/core.async "0.2.371"]
                 [cljs-ajax "0.5.1"]
                 [org.immutant/web "2.1.0"]]

  :min-lein-version "2.0.0"
  :uberjar-name "notesapp.jar"
  :jvm-opts ["-server"]

  :source-paths ["src" "src-cljs"]

  ;; :main notesapp.core
  :libdir-path "personal/lib"  ;; lein libdir
  :migratus {:store :database}

  :plugins [[lein-environ "1.0.1"]
            [migratus-lein "0.2.0"]
            [lein-cljsbuild "1.1.0"]]
  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]
  :cljsbuild
  {:builds
   {:app
    {:source-paths ["src-cljs"]
     :figwheel {:on-jsload "notesapp.front.core/on-reload" }
     :compiler
     {:main notesapp.front.core
      :asset-path "js/out"
      :output-to "resources/public/js/app.js"
      :output-dir "resources/public/js/out"
      ;; :source-map-timestamp true
      :source-map true}}}}
  :repl-options
  {
   ;; :nrepl-middleware
   ;; ["cider.nrepl/cider-middleware"
   ;;  "refactor-nrepl.middleware/wrap-refactor"
   ;;  "cemerick.piggieback/wrap-cljs-repl"
   ;;  ]
   }  
  
  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
              :hooks [leiningen.cljsbuild]
              :cljsbuild
              {:builds
               {:app
                {:source-paths ["env/prod/cljs"]
                 :compiler {:optimizations :advanced :pretty-print false}}}} 
             
             :aot :all}
   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]
   :project/dev  {:dependencies [[ring/ring-mock "0.3.0"]
                                 [ring/ring-devel "1.4.0"]
                                 [pjstadig/humane-test-output "0.7.0"]
                                 [com.cemerick/piggieback "0.2.1"]
                                 ;; [lein-figwheel "0.4.1"]
                                 ;; [org.clojure/tools.nrepl "0.2.12"]
                                 ;; [cider/cider-nrepl "0.10.0-SNAPSHOT"]
                                 [cider/cider-nrepl "0.9.1"]  ;; 0.10.0-SNAPSHOT
                                 ;; [refactor-nrepl "1.1.0"]
                                 [mvxcvi/puget "1.0.0"]]
                  :plugins [[lein-figwheel "0.4.1"]]
                   :cljsbuild
                   {:builds
                    {:app
                     {:source-paths ["env/dev/cljs"]
                      ;; :compiler {:source-map true}
                      }}} 
                  
                  :figwheel
                  {:http-server-root "public"
                   :server-port 3449
                   :nrepl-port 7002
                   :nrepl-middleware
                   ["cider.nrepl/cider-middleware"
                    "refactor-nrepl.middleware/wrap-refactor"
                    "cemerick.piggieback/wrap-cljs-repl"
                    ]
                   ;; :ring-handler notesapp.handler/app

                   :css-dirs ["resources/public/css"]
                   }
                  
                  :repl-options
                  {:port 6677
                   ;; :init-ns notesapp.core
                   }
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]
                  ;;when :nrepl-port is set the application starts the nREPL server on load
                  :env {:dev        true
                        :port       3000
                        :nrepl-port 7000}}
   :project/test {:env {:test       true
                        :port       3001
                        :nrepl-port 7001}}
   :profiles/dev {}
   :profiles/test {}})
