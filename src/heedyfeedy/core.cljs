(ns heedyfeedy.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [day8.re-frame.http-fx]
   [heedyfeedy.events :as events]
   [heedyfeedy.views :as views]
   [heedyfeedy.config :as config]
   ))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/starter] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (println "you've called INIT!")
  (dev-setup)
  (mount-root))
