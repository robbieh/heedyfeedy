(ns heedyfeedy.views
  (:require
   [re-frame.core :as re-frame]
   [heedyfeedy.subs :as subs]
   [heedyfeedy.events :as events]
   ))

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    [:div
     [:h1
      "Hello from " @name]
     ]))

(defn main-page []
  (let [aw-test (re-frame/subscribe [:aw-test])]
    [:div.main
     [:div.header "HeedyFeedy" ]
     [:div.body [:p (str @aw-test)]
      ]
     ]))

(defn update-server-info [])

(defn login-panel []
  [:div.login
   (main-panel)
   [:input#login-url {:default-value "http://"}]
   [:input#login-user {:placeholder "user"}]
   [:input#login-token {:placeholder "token"}]
   [:button.connect  {
                     :on-click 
                     #(re-frame/dispatch 
                        [::events/update-server-info
                         {
                          :url (.. (js/document.querySelector "input#login-url") -value)
                          :user (.. (js/document.querySelector "input#login-user") -value)
                          :token (.. (js/document.querySelector "input#login-token") -value)
                          }])}
    "Connect"]]
  )

(defn starter "Conditionally show login panel or main page"
  []
  (let [server-info (re-frame/subscribe [:server])
        ]
    (if (empty? @server-info) 
      (login-panel)
      (main-page))
    ))
