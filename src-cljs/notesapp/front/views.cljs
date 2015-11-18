(ns notesapp.front.views
  (:require [reagent.core  :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [cljs.pprint :refer [pprint]]))

(declare dispatch-user-input handle-tags-input)

(defn tags-filter []
  (let [
        ;; val (atom "")
        ]
    (fn []
      [:input#tags-filter {:type "text"
                           :on-change #(dispatch-user-input :set-tags-filter %)}])))
(defn dispatch-user-input [event-id dom-evt]
  (let [iv (-> dom-evt .-target .-value)]
    (dispatch-sync [event-id iv])))

(defn click-tag [tag-id]
  (fn [] (js/console.log "click: " tag-id)))

(defn tags-list []
  (let [tags (subscribe [:filtered-tags])]
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

;; enter: 13, tab: 9, esc: 27, arrow down: 40, up: 38
(def key-codes-map {:enter 13 :esc 27 :down 40 :up 38})
(defn debug-event [e]
  (let [keyCode (.-keyCode e)]
    (println "key code:" keyCode ", is number? " (number? keyCode) ", ctrl: " (.-ctrlKey e))
    ;; (println "event properties:" (js/Object.keys e))
    (condp #(= (get key-codes-map %1) %2) keyCode
      :esc (do (set! (.-value (.-target e)) "")
               (dispatch-sync [:set-tags-match-str ""])
               (.preventDefault e))
      nil)
    ))

(defn handle-submit [e]
  (let [el (.getElementById js/document "new-tag")]
    (println (.-value el))
    (set! (.-value el) "")))

(defn tag-completions-div []
  (let [matched-tags (subscribe [:matched-tags])]
    (fn []
      [:div#completions
       (for [tag-candidate @matched-tags]
         (let [{:keys [id]} tag-candidate]
           [:p {:key (:id tag-candidate)} (:tag-name tag-candidate)]))])))

(defn entry-crud-block []
  (let []
    (fn []
      [:div#edit-block
       ;; [initial-focus-wrapper]
       [:input {:id "new-tag" :type "text" :on-change nil
                ;; :on-key-down debug-event
                }]
       [:button {:type "submit"
                 :on-click handle-submit} "保存"]
       [:div#tags-block
        [:span.notetag "tag-1"]
        [:span.notetag "tag-2"]
        [:span.notetag "tag-3"]
        [:span#tags-wrapper
         [:input#note-tags
          {:type "text"
           :on-change #(dispatch-user-input :set-tags-match-str %)
           :on-key-down handle-tags-input}]
         [tag-completions-div]]]])))
(defn handle-tags-input [e]
  (debug-event e))

(defn notes-block []
  (let [notes (subscribe [:sorted-notes])]
    (fn []
      ;; (pprint @notes)
      [:div#notes-block
       [:ul
        (for [note @notes]
          (let [{:keys [id]} note]
            [:li {:key (:id note) :on-click nil}
             [:p (:content note)]
             [:p (str "最后修改时间: " (:updated-at note))]]))
        ;; [:p "hello, notes block"]
        ]])))

(defn edit-tab-banner []
  [:div#edit-tab-banner.clear
   [:ul#tab-banner
    [:li.active "笔记"]
    [:li "标签"]]])
(defn main-panel []
  [:div {:style {:width "100%" :height "100%"}}
   [:div#left-bar
    [tags-filter]
    [tags-list]
    ]
   [:div#content-panel
    [edit-tab-banner]
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
