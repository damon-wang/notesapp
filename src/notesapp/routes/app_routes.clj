(ns notesapp.routes.app-routes
  (:require [notesapp.db.core :as db]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [clj-time.core :as tc]
            [clj-time.coerce :as tco]
            [clojure.java.io :as io]))

(defn tags []
  ;; (println)
  (let [names
        (reduce #(assoc %1 (:id %2) %2) {}
                (map #(clojure.set/rename-keys
                       % {:tag_name :tag-name :created_at :created-at :updated_at :updated-at})
                     (db/get-tags)))]
    {:body names})
  )
(defn notes []
  (let [notes
        (reduce #(assoc %1 (:id %2) %2) {}
                (map #(clojure.set/rename-keys
                       % {:created_at :created-at :updated_at :updated-at})
                     (db/get-notes)))]
    {:body notes}))
  
(defn save-tag [tname desc]
  (println (str "save tag: " tname ", " desc))
  (let [now (db/sql-now)]
    (db/save-tag<! {:tag_name tname :description desc
                    :created_at now :updated_at now})
    (ok "save success")))

(defroutes app-routes
  (GET "/tags" [] (tags))
  (POST "/tags" [tname desc] (save-tag tname desc))
  (GET "/notes" [] (notes))
  ;; (GET "/docs" [] (ok (-> "docs/docs.md" io/resource slurp)))
  )

