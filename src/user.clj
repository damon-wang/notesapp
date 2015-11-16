(ns user)
;; (:require)
;; [figwheel-sidecar.repl-api :as ra]
;; [figwheel-sidecar.repl]
;; (:import java.util.concurrent.RejectedExecutionException)
(defn fig-start []
  (require '[figwheel-sidecar.repl-api :as fig-api]
           '[figwheel-sidecar.repl :as fig-repl])
  ((resolve 'fig-api/start-figwheel!)
   {:figwheel-options
    {:http-server-root "public"
     :css-dirs ["resources/public/css"]
     } ;; <-- figwheel server config goes here 
    :build-ids ["app"]   ;; <-- a vector of build ids to start autobuilding
    :all-builds          ;; <-- supply your build configs here
    ((resolve 'fig-repl/get-project-cljs-builds))
    }))

;; Please note that when you stop the Figwheel Server http-kit throws
;; a java.util.concurrent.RejectedExecutionException, this is expected

(defn fig-stop []
  ;; (try)
  ((resolve 'fig-api/stop-figwheel!))
  ;; executed in another thread, can't be caught
  ;; (throw (RejectedExecutionException. "xxx"))
  ;; (catch RejectedExecutionException e
  ;;   (println "figwheel stopped"))
  )

(defn fig-repl []
  ((resolve 'fig-api/cljs-repl)))

(defn start-app []
  (require '[notesapp.core :as c]
           '[environ.core :refer [env]])
  ((resolve 'c/start-http-server) ((resolve 'c/http-port) ((resolve 'env) :port)))
  (println "http server started..."))

(defn stop-app []
  ((resolve 'c/stop-http-server)))

(defn restart-app []
  (require '[clojure.tools.namespace.repl :refer [refresh]])
  (stop-app)
  ((resolve 'refresh))
  (start-app))

(defn- require? [symbol]
  (try (require symbol) true (catch Exception e false)))
(defn start-nrepl-server []
  (require '[clojure.tools.nrepl.server :as nrepl-serv])
  (let [middleware
        ["cider.nrepl/cider-middleware"
         ;; "refactor-nrepl.middleware/wrap-refactor"
         "cemerick.piggieback/wrap-cljs-repl"
         ]
        port 7001
        resolve-mw (fn [name]
                     (let [s (symbol name)
                           ns (symbol (namespace s))]
                       (if (and
                            (require? ns)
                            (resolve s))
                         (let [var (resolve s)
                               val (deref var)]
                           (if (vector? val)
                             (map resolve val)
                             (list var)))
                         (println (format "WARNING: unable to load \"%s\" middleware" name)))))
        middleware (mapcat resolve-mw middleware)]
    ((resolve 'nrepl-serv/start-server)
     :port port
     :handler (apply (resolve 'nrepl-serv/default-handler) middleware))
    (println "nrepl server started...")))

(defn ns-gui []
  (require '[clj-ns-browser.sdoc :as sdoc])
  ((resolve 'sdoc/sdoc*))) ;; sdoc is a macro, resolved result can't be one macro

(start-nrepl-server)
