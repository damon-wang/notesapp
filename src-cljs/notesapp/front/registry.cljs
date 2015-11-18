(ns notesapp.front.registry
  (:require
   [cljs.pprint :refer [pprint]]
   [clojure.string :as str]
   [reagent.core :as reagent :refer [atom]]
   [ajax.core :refer [GET POST]]
   [re-frame.core :as re-frame :refer
    [register-handler debug path trim-v after register-sub subscribe dispatch dispatch-sync]])
  (:require-macros
   [reagent.ratom :refer [reaction]]
   ))

(declare next-tag-id literal-db)

(defn legacy-register-handlers []
  (re-frame.core/clear-event-handlers!)
  ;; (register-handler
  ;;  :initialise-db
  ;;  (fn [_ _] literal-db)
  ;;  )
  ;; (register-handler
  ;;  :add-tag
  ;;  [(path :tags) trim-v] ;; apply order: from index 0 to max index
  ;;  (fn [tags [text]]
  ;;    (let [id (next-tag-id tags)]
  ;;      (assoc tags id {:id id :name text}))))
  ;; (register-handler :set-match-str [trim-v] nil)
  nil)

(defn next-tag-id [tags]
  ((fnil inc 0) (last (keys tags))))

(defn print-response [response]
  (pprint response))

(defn error-handler [{:keys [status status-text]}]
  (println (str "something bad happened: " status " " status-text)))

(defn get-tags []
  (js/setTimeout
   #(GET "/tags" {:handler (fn [res] (dispatch-sync [:reset-tags res]))
                  :response-format :transit
                  :error-handler error-handler}) 0))
(defn get-notes []
  (GET "/notes" {:handler (fn [res]
                            (dispatch-sync [:reset-notes res])
                            ;; (pprint @(subscribe [:sorted-notes]))
                            )
                 :response-format :transit
                 ;; :keywords? true
                 :error-handler error-handler}))

(defn save-tag []
  (POST "/tags"
        {:params {:tname "emacs"
                  :desc    "高可配置的编辑器"
                  }
         :format :json
         :handler print-response
         :error-handler error-handler}))

;; (sort-by (fn [[k v]] (:updated-at v)) (fn [a b] (compare b a)) sample-tags)
(def sample-tags
  {"1" {:id 1 :tag-name "java" :description "the java language" :updated-at "2015-11-12"}
   "2" {:id 2 :tag-name "javascript" :description "the javascript language for web"
            :updated-at "2015-11-13"}
   "3" {:id 3 :tag-name "营养" :description "有关营养方面的知识" :updated-at "2015-11-09"}
   })

(defn reset-notes [db [_ notes]]
  (-> db
      (assoc :paged-notes notes)))

(defn reset-tags [db [_ tags]]
  (-> db
      (assoc :tags tags)
      (dissoc :loading-home-data?)))

(defn load-home-data [db _]
  (assoc db :loading-home-data? true))

(defn query-initialized [db _]
  (reaction (not (:loading-home-data? @db))))

(defn query-sorted-tags [db _]
  (reaction
   (mapv #(get % 1) (sort-by (fn [[k v]] (:updated-at v))
                     #(compare %2 %1)
                     (:tags @db)))))

(defn query-sorted-notes [db _]
  (reaction
   (mapv #(get % 1) (sort-by (fn [[k v]] (:updated-at v))
                     #(compare %2 %1)
                     (:paged-notes @db)))))


(defn substr-match? [target-str filter-str]
  (> (.indexOf (.toLowerCase target-str) (.toLowerCase filter-str)) -1))

(defn query-filtered-tags [db _]
  (reaction
   (let [sorted-tags
         (reaction
          (mapv #(get % 1)
                (sort-by (fn [[k v]] (:updated-at v)) #(compare %2 %1) (:tags @db))))
         ;; (subscribe [:sorted-tags])
         filter-str (reaction (:tag-filter-str @db))
         ]
     ;; (println "filter str:" @filter-str)
     (if (clojure.string/blank? @filter-str)
       @sorted-tags
       (filterv
        #(substr-match? (:tag-name %) @filter-str)
        @sorted-tags)))))

(defn query-matched-tags [db _]
  (reaction
   (let [sorted-tags
         (reaction
          (mapv #(get % 1)
                (sort-by (fn [[k v]] (:updated-at v)) #(compare %2 %1) (:tags @db))))
         ;; (subscribe [:sorted-tags])
         match-str (reaction (:tags-match-str @db))
         ]
     ;; (println "filter str:" @filter-str)
     (if (clojure.string/blank? @match-str)
       []
       (filterv
        #(substr-match? (:tag-name %) @match-str)
        @sorted-tags)))))


(defn display-sorted-tags []
  ;; (dispatch-sync [:reset-tags sample-tags])
  ;; (dispatch-sync [:set-tags-filter "养"])
  (dispatch-sync [:set-tags-match-str "养"])
  (pprint @(subscribe [:matched-tags]))
  nil)

(defn set-tags-filter [db [_ filter-str]]
  ;; (println "set tags filter..." filter-str)
  (-> db
      (assoc :tag-filter-str (str/trim (or filter-str "")))))

(defn set-tags-match-str [db [_ tags-input-str]]
  (-> db
      (assoc :tags-match-str (str/trim (or tags-input-str "")))))



(defn init-appstate-registry []
  (re-frame/clear-event-handlers!)
  (re-frame/clear-sub-handlers!)
  (register-handler :reset-tags nil reset-tags)
  (register-handler :load-home-data load-home-data)
  (register-handler :reset-notes nil reset-notes)
  (register-handler :set-tags-filter set-tags-filter)
  (register-handler :set-tags-match-str set-tags-match-str)
  (register-sub :sorted-tags query-sorted-tags)
  (register-sub :filtered-tags query-filtered-tags)
  (register-sub :matched-tags query-matched-tags)
  (register-sub :sorted-notes query-sorted-notes)
  (register-sub :home-data-loaded? query-initialized)
  (dispatch-sync [:load-home-data])
  (js/setTimeout #(dispatch-sync [:reset-tags sample-tags]) 1000)
  ;; (get-tags)
  (get-notes)
  )
  
