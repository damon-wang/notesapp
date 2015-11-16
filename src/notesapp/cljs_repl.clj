(ns notesapp.cljs-repl
  (:require
   [cljs.build.api :as api]
   [cemerick.piggieback :as pig]
   [cljs.repl :as repl]
   [cljs.repl.node :as node]
   [cljs.repl.rhino :as rhino])
  )
(def config  {
  :main 'notesapp.front.core
  :asset-path "js/out"
  :output-to "resources/public/js/main.js"
  :output-dir "resources/public/js/out"
  })
(defn build-cljs []
  (api/build "src-cljs" config))

(defn nrepl-cljs []
  (pig/cljs-repl
   (node/repl-env)
   ;; (rhino/repl-env)
   {:output-dir (:output-dir config)}))
(defn node-repl []
  (repl/repl
   (node/repl-env)
   :watch "src-cljs"
   :output-dir (:output-dir config)
   ))
