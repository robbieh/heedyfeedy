(ns heedyfeedy.events
  (:require
   [re-frame.core :as re-frame]
   [heedyfeedy.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [ajax.core :as ajax]
   ))

(def store-server-config-interceptor 
  (re-frame/after db/server-config->local-store))
;(def interceptors [store-server-config-interceptor])

(re-frame/reg-event-db ::initialize-db 
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-fx ::update-server-info store-server-config-interceptor
 (fn [cofx [ekey server-map]]
   (let [db               (:db cofx)
         {:keys [server]} server-map]
     (println server-map)
     (println cofx)
     (println (str (:url server-map) "/api/users/" (:user server-map)))
   {:db (assoc db :server server-map)
    :http-xhrio {:method          :get
                 :uri             (str (:url server-map) "/api/users/" (:user server-map))
                 :headers         {:Authorization (str "Bearer " (:token server-map))}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::login-success]
                 :on-failure      [::login-failure]}}
   )))

(re-frame/reg-event-db ::login-success
  (fn [db [_ response]]
    (assoc-in db [:server :userinfo] response)))

(re-frame/reg-event-db ::login-failure
  (fn [db [_ response]]
    (-> db
        (assoc-in [:server :error] true)
        ((partial merge-with conj) {:error-messages [response]}))))


(re-frame/reg-event-db ::test-backend-login
  (fn [db _]
    ))

(re-frame/reg-event-db ::aw-test-update
  (fn [db [ekey data]]
    (assoc db :aw-test data)))
