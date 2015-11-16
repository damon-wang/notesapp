(ns notesapp.front.registry
  (:require
   [cljs.pprint :refer [pprint]]
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
                  :error-handler error-handler}) 2000))
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

(defn display-sorted-tags []
  (dispatch-sync [:reset-tags sample-tags])
  (pprint @(subscribe [:sorted-tags]))
  nil)

(defn init-appstate-registry []
  (re-frame/clear-event-handlers!)
  (re-frame/clear-sub-handlers!)
  (register-handler :reset-tags nil reset-tags)
  (register-handler :load-home-data load-home-data)
  (register-handler :reset-notes nil reset-notes)
  (register-sub :sorted-tags query-sorted-tags)
  (register-sub :sorted-notes query-sorted-notes)
  (register-sub :home-data-loaded? query-initialized)
  (dispatch-sync [:load-home-data])
  ;; (js/setTimeout #(dispatch-sync [:reset-tags sample-tags]) 2000)
  (get-tags)
  (get-notes)
  )
  
