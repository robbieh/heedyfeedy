(ns heedyfeedy.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub :name
 (fn [db] (:name db)))

(re-frame/reg-sub :server
  (fn [db] (:server db)))

(re-frame/reg-sub :aw-test
  (fn [db] (:aw-test db)))

(re-frame/reg-sub :heedy-objects
  (fn [db] (:heedy-objects db)))

(re-frame/reg-sub :basket
  (fn [db] (:basket db)))

(re-frame/reg-sub :heedy-send-queue
  (fn [db] (:heedy-send-queue db)))

;(re-frame/reg-sub :error-messages
;  (fn [db _] (:error-messages db)))
