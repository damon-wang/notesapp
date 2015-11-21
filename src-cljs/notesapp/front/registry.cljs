(ns notesapp.front.registry
  (:require
   [cljs.pprint :refer [pprint]]
   [clojure.string :as str]
   [reagent.core :as reagent :refer [atom]]
   [reagent.ratom :refer [make-reaction]]
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

(defn query-sorted-notes [db _]
  (reaction
   (mapv #(get % 1) (sort-by (fn [[k v]] (:updated-at v))
                     #(compare %2 %1)
                     (:paged-notes @db)))))


(defn substr-match? [target-str filter-str]
  (> (.indexOf (.toLowerCase target-str) (.toLowerCase filter-str)) -1))

(defn sort-tags-by [db sort-key]
  (mapv #(get % 1)
        (sort-by (fn [[k v]] (sort-key v))
                 #(compare %2 %1)
                 (:tags db))))

(defn filtered-tags [sorted-tags tags-filter]
  (if (clojure.string/blank? tags-filter)
      sorted-tags
      (filterv
       #(substr-match? (:tag-name %) tags-filter)
       sorted-tags)))

(defn matched-tags [sorted-tags tag-match-str]
  (if (clojure.string/blank? tag-match-str)
      []
      (filterv
       #(substr-match? (:tag-name %) tag-match-str)
       sorted-tags)))

(defn test-handle-and-sub []
  (dispatch-sync [:update-cti "xxx yyy"])
  (pprint @(subscribe [:cti]))
  nil)

(defn set-tags-filter [db [_ filter-str]]
  ;; (println "set tags filter..." filter-str)
  (-> db
      (assoc :tags-filter (str/trim (or filter-str "")))))

(defn set-candidate-tag-index [db [_ i]]
  (assert (number? i) "cti should be a number")
  (-> db
      (assoc :candidate-tag-index i)))

(defn next-cti [curr-index ctc v]
  (assert (every? number? [curr-index ctc v]))
  (assert (> ctc 0))
  (println curr-index ctc v)
  (let [after-v (+ curr-index v)
        max-index (- ctc 1)]
    ;; (println "mod: " (mod after-v ctc) ", max-index: " max-index)
    (cond
      (< after-v 0) max-index
      (> after-v max-index) (mod after-v ctc)
      :else after-v)))


(defn init-appstate-registry []
  (re-frame/clear-event-handlers!)
  (re-frame/clear-sub-handlers!)
  (register-handler
   :init-db
   (fn [db _]
     (-> db
         (assoc :candidate-tag-index -1)
         (assoc :selected-tags []))))
  (register-handler :reset-tags nil reset-tags)
  (register-handler :load-home-data load-home-data)
  (register-handler :reset-notes nil reset-notes)

  (register-handler :set-tags-filter set-tags-filter)
  (register-sub
   :tags-filter
   (fn [db _]
     (make-reaction (fn tags-filter [] (get-in @db [:tags-filter])))))
  (register-sub
   :sorted-tags
   (fn [db _]
     (reaction (sort-tags-by @db :updated-at))))
  (register-sub
   :filtered-tags
   (fn [db _]
     (let [sorted-tags (subscribe [:sorted-tags])
           tags-filter (subscribe [:tags-filter])]
       (make-reaction (fn ftags [] (filtered-tags @sorted-tags @tags-filter))))))

  (register-handler
   :set-tag-match-str
   (fn [db [_ tag-input]]
     (-> db
         (assoc :tag-match-str (str/trim (or tag-input "")))))) 
  (register-sub
   :tag-match-str
   (fn [db _] (reaction (:tag-match-str @db))))

  (register-handler
   :set-active-tab
   (fn [db [_ tab-id]] (assoc db :active-tab tab-id)))
  (register-sub
   :active-tab
   (fn [db _] (reaction (:active-tab @db))))
  
  (register-handler
   :update-cti  ;; cti: candidate tag index
   (fn [db [_ arg]]
     (println "arg: " arg)
     (if (= arg :reset)
       (assoc db :candidate-tag-index -1)
       (let [cur-index (:candidate-tag-index db)
             candidate-count
             (count (matched-tags (sort-tags-by db :updated-at) (:tag-match-str db)))]
         (println "ci:" cur-index)
         (println "cc: " candidate-count)
         (if (> candidate-count 0)
           (assoc db :candidate-tag-index (next-cti cur-index candidate-count arg))
           db)
         ))))
  (register-sub
   :cti
   (fn [db _] (reaction (:candidate-tag-index @db))))

  (register-handler
   :append-selected-tag
   (fn [db [_ tag-id tag-name]]
     (println "append-selected-tag: " tag-id tag-name)
     (-> db
         (assoc
          :selected-tags
          (conj (:selected-tags db) {:tag-id tag-id :tag-name tag-name})))))

  (register-sub
   :selected-tags
   (fn [db _] (reaction (:selected-tags @db))))

  (register-sub
   :matched-tags
   (fn [db _]
     (let [sorted-tags (subscribe [:sorted-tags])
           tag-match-str (subscribe [:tag-match-str])]
       (reaction
        (if (clojure.string/blank? @tag-match-str)
          []
          (filterv #(substr-match? (:tag-name %) @tag-match-str) @sorted-tags))))))
  (register-sub :sorted-notes query-sorted-notes)
  (register-sub :home-data-loaded? query-initialized)
  (register-sub :all-db (fn [db _] (reaction @db)))
  (dispatch-sync [:init-db])
  (dispatch-sync [:load-home-data])
  (js/setTimeout #(dispatch-sync [:reset-tags sample-tags]) 1000)
  ;; (get-tags)
  ;; (get-notes)
  )

