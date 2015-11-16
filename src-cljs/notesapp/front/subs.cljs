(ns notesapp.front.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))
(re-frame.core/clear-sub-handlers!)
(register-sub
 :visiable-tags
 (fn [db _]
   (reaction (:tags @db))))
   ;; (let [tags (reaction (:tags @db))
         ;; filter-str (reaction (:filter-str @db))
         ;; ]
     ;; (reaction (vec (filter #(> (.indexOf (:name %) @filter-str) -1) (vals @tags))))
     ;; tags
     ;; )))

(register-sub
 :match-str
 (fn [db _]
   (reaction (:match-str @db))))

(register-sub
 :open-tabs
 (fn [db _]
   (reaction (:open-tabs @db))))
