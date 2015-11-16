(ns notesapp.front.handlers
  (:require
   [reagent.core :as reagent :refer [atom]]
   [re-frame.core :refer [register-handler debug path trim-v after]]))

(defn default-db []
  (loop [i 1 map (sorted-map)]
    (if (= i 21)
      {:tags map}
      (recur
       (inc i)
       (assoc map i {:id i :name (str "标签-" i)})))))

(defn set-match-str [db [text]]
  ;; (println ">>[" (count text) "]")
  (assoc db :match-str text))

(defn set-todo-page [db tag-ids todo-entries]
  ;; (assoc db :open-tags {:tag-ids [tag-id] :tabs {tag-id todo-entries}})
  (-> db
      (assoc-in [:open-tags :tabs (get tag-ids 0)] todo-entries)
      (assoc-in [:open-tags :tag-ids] tag-ids)))

(defn empty-tabs-state [db]
  (assoc db :open-tabs []))

(defn add-todo-page [db page-details]
  (update-in db [:open-tabs] conj page-details))

(defn temp [_ _]
   (-> (default-db)
     (set-match-str [""])
     empty-tabs-state
     (add-todo-page
      {:tag-id 1 :tag-name "clojure" :page-nu 1
       :entries
       [{:id 1 :content "one todo for tag x"}
        {:id 2 :content "abc todo for tag x,y"}]})
     (add-todo-page
      {:tag-id 2 :tag-name "nodejs" :page-nu 1
       :entries
       [{:id 3 :content "first entry for nodejs"}
        {:id 4 :content "second entry for nodejs"}]})
     ))
