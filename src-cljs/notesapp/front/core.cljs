(ns notesapp.front.core
  (:require [notesapp.front.main-view :as mv]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [notesapp.front.handlers :as handlers]
            [notesapp.front.views :as views]
            [notesapp.front.subs :as subs]
            [notesapp.front.scratch]
            [notesapp.front.registry :as registry]
            ))

;; (enable-console-print!)

(defn hello []
  (js/console.log "code snapshot at: " (.toLocaleString (js/Date.))))

(declare init)
(defn on-reload []
  ;; (js/console.log "on-reload...")
  (hello)
  ;; (mv/render-root)
  (init)
  )

(defn ^:export init []
  ;; (registry/init-appstate-registry)
  (views/render-root)
  ;; (views/inspect-hiccup)
  ;; (hello)p
  ;; (mv/get-tags)
  ;; (mv/save-tag)
  )
