(ns heedyfeedy.views
  (:require
   [re-frame.core :as re-frame]
   [heedyfeedy.subs :as subs]
   [heedyfeedy.events :as events]
   [reagent.core :as reagent]
   ))

(defn heedy-number-object [obj]
  (let [number  (reagent/atom "")
        schema  (get-in obj [:meta :schema])
        id      (:id obj)
        objname (:name obj)
        ]
    ^{:key id} [:div.heedy-object 
     [:p.heedy-object-description (or (:name obj) (:description obj))]
     [:div.heedy-entry-line
       [:input.heedy-number {:type :number
                             :size :10
                             :on-change #(reset! number (-> % .-target .-value))
                             :on-key-down #(case (.-which %)
                                             13 (re-frame/dispatch [::events/add-to-basket [id objname @number]]) 
                                             nil)
                             }]
       [:div.basket-button
         {:on-click #(re-frame/dispatch [::events/add-to-basket [id objname (js/parseInt @number )] ])}
         [:img {:src "/icons/arrow-downward.svg"}]
        ]
      ]
     ]))

(defn heedy-string-object [obj]
  (let [text    (reagent/atom "")
        schema  (get-in obj [:meta :schema])
        id      (:id obj)
        objname (:name obj)
        ]
    ^{:key id} [:div.heedy-object 
     [:p.heedy-object-description (or (:name obj) (:description obj))]
     [:div.heedy-entry-line
       [:input.heedy-string {:size :14 
                             :on-change #(reset! text (-> % .-target .-value))
                             :on-key-down #(case (.-which %)
                                             13 (re-frame/dispatch [::events/add-to-basket [id objname @text]])
                                             nil)
                             }]
       [:div.basket-button
         {:on-click #(re-frame/dispatch [::events/add-to-basket [id objname @text ] ])}
         [:img {:src "/icons/arrow-downward.svg"}]
        ]
      ]
     ]))

(defn heedy-enum-object [obj]
  (let [schema (get-in obj [:meta :schema])
        id      (:id obj)
        objname (:name obj)
        enum    (get-in obj [:meta :schema :enum])
        ]
    (for [item enum]
    ^{:key (str id "-" item)} [:div.heedy-object 
       {:on-click #(re-frame/dispatch [::events/add-to-basket [id objname item] ])}
       [:p.heedy-object-description item]
     ])
    ))

(defn basket-div [basket]
  [:footer.app-footer
    (for [[date content] basket
          :let [[id objname value] content] ]
      ^{:key (str id "-" date "-basket")} [:div.basket-item
                                  [:p objname]
                                  [:p value]
                                  ]
      )
   [:div.footer-icon.push {:on-click #(re-frame/dispatch [::events/remove-all-from-basket])}
     [:img {:src "/icons/delete-forever.svg"}]
    ]
   [:div.footer-icon {:on-click #(re-frame/dispatch [::events/remove-last-from-basket])}
     [:img {:src "/icons/backspace.svg"}]
    ]
   [:div.footer-icon {:on-click #(re-frame/dispatch [::events/upload-to-heedy])}
     [:img {:src "/icons/upload.svg"}]
    ]
   ]
  )
(defn main-page []
  (let [heedy-objects (re-frame/subscribe [:heedy-objects])
        basket        (re-frame/subscribe [:basket])]
     [:<> 
      [:nav.app-header "HeedyFeedy" 
       [:img#server.header-icon.push {:src "/icons/account_box.svg"
                                 :on-click #(re-frame/dispatch [::events/show-server-info])
                                 }]
       [:img#refresh.header-icon {:src "/icons/refresh.svg"
                                  :on-click #(re-frame/dispatch [::events/heedy-get-objects] )}]
       ]
     [:main.app-body
      ; (for [object @heedy-objects
      ;       :let [
      ;         schema-type (get-in object [:meta :schema :type])
      ;         enum?       (get-in object [:meta :schema :enum])]]
      ;       [:p (:name object) schema-type enum?]
      ;       )
              
      (for [object @heedy-objects
            :let [
              schema-type (get-in object [:meta :schema :type])
              enum?       (get-in object [:meta :schema :enum])
              x-display   (get-in object [:meta :schema :x-display])
              schema-type (if enum? "enum" schema-type)
              schema-type (if x-display x-display schema-type)
            ]]
        (case schema-type
              "number"  (heedy-number-object object)
              "string"  (heedy-string-object object)
              "enum"    (heedy-enum-object object)
              ^{:key (:id object)} [:p "schema missing:" schema-type  ]
              )

        )
      ]
     (when (not (empty? @basket)) (basket-div @basket))
     ]))

(defn update-server-info [])

(defn login-panel [server-info]
  [:div.login
   [:p "Connect to your Heedy server"]
   [:input#login-url {:default-value (or (:url @server-info ) "http://") }]
   [:input#login-user {:placeholder "user" :default-value (:user @server-info)}]
   [:input#login-token {:placeholder "token" :default-value (:token @server-info)}]
   [:button.connect  {
                     :on-click 
                     #(re-frame/dispatch 
                        [::events/update-server-info
                         {
                          :url (.. (js/document.querySelector "input#login-url") -value)
                          :user (.. (js/document.querySelector "input#login-user") -value)
                          :token (.. (js/document.querySelector "input#login-token") -value)
                          }])}
    "Connect"]
   (when (:error @server-info) [:p (:error @server-info)])
   ]
  )
;(re-frame/dispatch ::heedy-get-objects)
(defn starter "Conditionally show login panel or main page"
  []
  (let [server-info (re-frame/subscribe [:server])
        ]
;(day8.re-frame-10x/show-panel! false)
;(println "info" (:show-server-info @server-info) )
    (if (:show-server-info @server-info) 
      (login-panel server-info)
      (main-page))
    ))
