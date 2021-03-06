(ns notesapp.front.views
  (:require [reagent.core  :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [cljs.pprint :refer [pprint]]))

(declare dispatch-user-input handle-tags-input note-tags-input)

(defn tags-filter []
  (let [
        ;; val (atom "")
        ]
    (fn []
      [:div#tags-filter
       [:input {:type "text" :on-change #(dispatch-user-input :set-tags-filter %)}]])))
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

(defn not-selected? [selected-tags tag-name]
  (empty?
   (filter #(= (.toLowerCase tag-name) (.toLowerCase (:tag-name %))) selected-tags)))

;; enter: 13, tab: 9, esc: 27, arrow down: 40, up: 38
(def key-codes-map {:enter 13 :esc 27 :down 40 :up 38 :tab 9})
(defn handle-tags-input [e matched-tags cti tags-map-by-name selected-tags]
  (let [keyCode (.-keyCode e)]
    ;; (println "key code:" keyCode ", is number? " (number? keyCode) ", ctrl: " (.-ctrlKey e))
    (condp #(= (get key-codes-map %1) %2) keyCode
      :esc (do (set! (.-value (.-target e)) "")
               (dispatch-sync [:set-tag-match-str ""])
               (dispatch-sync [:update-cti :reset])
               (.preventDefault e))
      :down (do (dispatch-sync [:update-cti 1])
                (.preventDefault e))
      :up (do (dispatch-sync [:update-cti -1])
              (.preventDefault e))
      :enter (do
               (let [selected-tag (get matched-tags cti)]
                 (println "selected-tag: " selected-tag)
                 (if selected-tag
                   (do
                     ;; selected-tags will be not in matched-tags 
                     ;; (if (not-selected? selected-tags (:tag-name selected-tag)))
                     (dispatch-sync [:append-selected-tag (:id selected-tag) (:tag-name selected-tag)]))
                   (do
                     (println "no tag selected")
                     (let [input-str (.toLowerCase (.-value (.-target e)))]
                       (if-let [matched-tag (get tags-map-by-name input-str)]
                         (do
                           (println "but input match tag:" matched-tag)
                           (if (not-selected? selected-tags input-str)
                             (dispatch-sync
                              [:append-selected-tag (:id matched-tag) (:tag-name matched-tag)])
                             (do
                               (println "but already selected"))))
                         (do
                           (println "will as a new tag:" input-str))))
                     )))
               (set! (.-value (.-target e)) "")
               (dispatch-sync [:set-tag-match-str ""])
               (dispatch-sync [:update-cti :reset]))
      
      nil)
    ))

(defn tag-completions-div []
  (let [matched-tags (subscribe [:matched-tags])]
    (fn []
      )))

(defn note-tab []
  (let [active-tab (subscribe [:active-tab])
        selected-tags (subscribe [:selected-tags])]
    (fn []
      [:div#note-tab {:style {:display (if (= @active-tab :note-tab) "block" "none")}}
       [:div.row.clear
        [:div {:class "row-left"}
         [:label "笔记内容"]]
        [:div {:class "row-right"}
         [:textarea {:id "new-tag" :rows 5
                     :on-change #(dispatch-user-input :set-note-content %)
                     }]]]
       [:div.row.clear
        [:div {:class "row-left"} [:label "标签"]]
        [:div {:class "row-right"}
         (for [tag @selected-tags]
           [:span.notetag
            {:key (:seq-id tag)
             :on-click #(println "selected-tag-id" (:tag-id tag))}
            (:tag-name tag)])
         [note-tags-input]]]
       [:button {:type "submit"
                 :on-click #(dispatch-sync [:save-note])} "保存"]])))


(defn note-tags-input []
  (let [matched-tags (subscribe [:matched-tags])
        cti (subscribe [:cti])
        tags-map-by-name (subscribe [:tags-map-by-name])
        selected-tags (subscribe [:selected-tags])]
    (fn []
      ;; (println "note-tags-input cti value:" @cti)
      ;; (println "note-tags-input matched-tags" @matched-tags)
      [:span#tags-wrapper
       [:input#tags-input
        {:type "text"
         :on-change #(dispatch-user-input :set-tag-match-str %)
         :on-key-down #(handle-tags-input % @matched-tags @cti @tags-map-by-name @selected-tags)}]
       (when (> (count @matched-tags) 0)
         [:div#completions
          (doall
           (map (fn [tag index]
                  (let [props {:key (:id tag)}]
                    [:p
                     (if (= @cti index) (assoc props :class "selected") props)
                     (:tag-name tag)]))
                @matched-tags (range 0 (count @matched-tags))))])
       ])))

(defn tag-tab []
  (let [active-tab (subscribe [:active-tab])]
    (fn []
      [:div#tag-tab {:style {:display (if (= @active-tab :tag-tab) "block" "none")}}
       ;; [initial-focus-wrapper]
       [:span "标签名: "]
       [:input {:id "tag-name" :type "text" :on-change nil
                ;; :on-key-down debug-event
                }]
       [:br]
       [:span "标签描述: "]
       [:textarea {:id "tag-description" :rows 3 :width "100%" :on-change nil
                   ;; :on-key-down debug-event
                   }]
       [:br]       
       [:button {:type "submit"
                 :on-click #(dispatch-sync [:create-tag])} "保存标签"]
       ])))

(defn tab-blocks [])

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
        ]])))

(defn edit-tab-banner []
  (let [active-tab (subscribe [:active-tab])]
    (fn []
      ;; (println "active-tab:" @active-tab)
      ;; [:div#edit-tab-banner.clear]
      [:ul {:id "tab-banner"}
       [:li
        {:on-click
         (fn [e] (dispatch-sync [:set-active-tab :note-tab]))
         :class (if (= @active-tab :note-tab) "active" "")}
        [:a {:href "#" :on-click (fn [e] (.preventDefault e))} "笔记"]]
       [:li
        {:on-click (fn [e] (dispatch-sync [:set-active-tab :tag-tab]))
         :class (if (= @active-tab :tag-tab) "active" "")}
        [:a {:href "#" :on-click (fn [e] (.preventDefault e))} "标签"]]])))

(defn main-panel []
  [:div {:style {:width "100%" :height "100%"}}
   [:div#left-bar
    [tags-filter]
    [tags-list]
    ]
   [:div#content-panel
    [edit-tab-banner]
    [note-tab]
    [tag-tab]
    [notes-block]]])

(defn top-panel []
  (let [ready? (subscribe [:home-data-loaded?])]
    (fn []
      (if-not @ready?
        ;; [:div#loading [:p "正在加载..."]]
        [:p#loading "正在加载..."]
        [main-panel]))))
(defn render-root []
  ;; (dispatch-sync [:reset-tags xxx])
  (reagent/render
   [top-panel]
   (.getElementById js/document "app")))
