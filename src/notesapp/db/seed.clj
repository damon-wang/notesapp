(ns notesapp.db.seed
  (:require
   [notesapp.db.core :as db]
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [clj-time.format :as tf]))

;; (tf/show-formatters)

(defn time-str [t]
  (str (t/to-time-zone t (t/default-time-zone))))

(defn n-entries [n field-key prefix]
  (map (fn [n]
         (let [time-point (tc/to-sql-time (t/plus (t/now) (t/days (- n))))
               ;; tp-str (time-str time-point)
               ]
           {field-key (str prefix n)
            :created_at time-point
            :updated_at time-point}))
       (range 1 (+ 1 n))))

;; (save-one-note {:content "foo bar" :created_at (db/sql-now) :updated_at (db/sql-now)})

(defn save-one-note [note]
  (db/save-note<! note))

(defn save-notes [n]   ;; warning: don't use count as arg name
  (map #(save-one-note %)
       (n-entries n :content "此为一条笔记: ")))
