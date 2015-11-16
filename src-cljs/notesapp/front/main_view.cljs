(ns notesapp.front.main-view
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST]]))

(defn handler [response]
  (.log js/console
        (str (js->clj response))
        (map? (identity response))
        ))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-tags []
  (GET "/tags" {:handler handler
                :error-handler error-handler}))
(defn save-tag []
  (POST "/tags"
        {:params {:name "emacs"
                  :desc    "The awesome editor - 匠人之心适合"}

         :format :json
         :handler handler
         :error-handler error-handler}))

(defn hello []
  [:h3 "<做笔记是个好习惯>"])
(defn render-root []
  (reagent/render
   [hello]
   (.getElementById js/document "app")))
