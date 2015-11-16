(ns notesapp.front.views
  (:require [reagent.core  :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [cljs.pprint :refer [pprint]]))

(defn tags-filter []
  (let [
        ;; val (atom "")
        ]
    (fn []
      [:input {:type "text"
               ;; :value @val
               :on-change #(let [iv (-> % .-target .-value)]
                             ;; (reset! val iv)
                             (dispatch-sync [:set-match-str iv]))}])))

(defn click-tag [tag-id]
  (fn [] (js/console.log "click: " tag-id)))

(defn tags-list []
  (let [tags (subscribe [:sorted-tags])]
    (fn []
      ;; (println "subscribe-----")
      ;; (pprint @tags)
      [:ul
       (for [tag @tags]
         (let [{:keys [id]} tag]
           [:li {:key (:id tag) :on-click (click-tag id) } (:tag-name tag)]))
       ;; (map (fn [tag] [:li (:name tag)]) @tags)
       ])))

(defn inspect-hiccup []
  (do
    (with-redefs [click-tag :f]
      (pprint ((tags-list))))
    nil))

(def initial-focus-wrapper
  (with-meta identity
    {:component-did-mount #(.focus (reagent/dom-node %))}))

(defn entry-crud-block []
  (let []
    (fn []
      [:div#edit-block
       [initial-focus-wrapper [:input {:id "new-tag" :type "text" :on-change nil}]]])))

(defn notes-block []
  (let [notes (subscribe [:sorted-notes])]
    (fn []
      (pprint @notes)
      [:div#notes-block
       [:ul
        (for [note @notes]
          (let [{:keys [id]} note]
            [:li {:key (:id note) :on-click nil}
             [:p (:content note)]
             [:p (str "最后修改时间: " (:updated-at note))]]))
        ;; [:p "hello, notes block"]
        ]])))

(defn main-panel []
  [:div {:style {:width "100%" :height "100%"}}
   [:div#left-bar
    ;; [tags-filter]
    [tags-list]]
   [:div#content-panel
    [entry-crud-block]
    [notes-block]]])

(defn top-panel []
  (let [ready? (subscribe [:home-data-loaded?])]
    (fn []
      (if-not @ready?
        [:div#loading [:p "正在加载..."]]
        [main-panel]))))
(defn render-root []
  ;; (dispatch-sync [:reset-tags xxx])
  (reagent/render
   [top-panel]
   (.getElementById js/document "app")))
