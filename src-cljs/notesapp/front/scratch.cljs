(ns notesapp.front.scratch
  (:require [cljs.pprint :refer [pprint]]))

(enable-console-print!)

(defn f2 []
  (dorun (pprint (for [x (range 10)] (range x)))))
