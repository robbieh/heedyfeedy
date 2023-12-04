(ns heedyfeedy.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [heedyfeedy.events :as events]
   [heedyfeedy.views :as views]
   [heedyfeedy.config :as config]
   ;[page-renderer.api :as pr]
   ))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/starter] root-el)))

;(defn register-service-worker []
;  (if (.-serviceWorker js/navigator)
;    (-> (js/navigator.serviceWorker.register "/service-worker.js")
;        (.then  (re-frame/dispatch [:service-worker true]))
;        (.catch (re-frame/dispatch [:service-worker false])))))
;
;(defn service-worker [request]
;  (pr/respond-service-worker
;    {:script "/js/app.js"
;     :sw-default-url "/"
;     :sw-add-assets
;       ["/css/main.css"
;        "/pwa-icons/android-chrome-192x192.png"
;        "/icons/account_box.svg"
;        "/icons/arrow-downward.svg"
;        "/icons/backspace.svg"
;        "/icons/delete-forever.svg"
;        "/icons/refresh.svg"
;        "/icons/upload.svg"
;        ]}))
;
(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
