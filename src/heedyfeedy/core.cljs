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

(def aw-test (atom {}))
(add-watch aw-test :aw-test (fn [key atom old new] (re-frame/dispatch-sync [::events/aw-test-update new])))
(comment (swap! aw-test assoc :new 3)
         (identity @aw-test))
(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
