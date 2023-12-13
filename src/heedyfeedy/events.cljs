(ns heedyfeedy.events
  (:require
   [re-frame.core :as re-frame]
   [heedyfeedy.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [ajax.core :as ajax]
   [cljs.reader]
   ))

; cofx =======================================================================
(re-frame/reg-cofx :now
  (fn [cofx _]
    (assoc cofx :now (.now js/Date ))))

(re-frame/reg-cofx :local-store-server-config
 (fn [cofx _]
   (assoc cofx :local-store-server-config
          (into (sorted-map) (some->>
                              (.getItem js/localStorage db/ls-server-config-key)
                              (cljs.reader/read-string))))))

(re-frame/reg-cofx :local-store-basket
 (fn [cofx _]
   (assoc cofx :local-store-basket
          (into (sorted-map) (some->>
                              (.getItem js/localStorage db/ls-basket-key)
                              (cljs.reader/read-string))))))

(re-frame/reg-cofx :local-store-objects
 (fn [cofx _]
   (assoc cofx :local-store-objects
          (into (sorted-map) (some->>
                              (.getItem js/localStorage db/ls-objects-key)
                              (cljs.reader/read-string))))))
; interceptors ===============================================================
(def store-server-config-interceptor 
  (re-frame/after db/server-config->local-store))
(def store-basket-interceptor
  (re-frame/after db/basket->local-store))
(def store-objects-interceptor
  (re-frame/after db/objects->local-store))
;(def interceptors [store-server-config-interceptor])

; events =====================================================================
(re-frame/reg-event-db ::initialize-db 
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-fx ::update-server-info store-server-config-interceptor
 (fn [cofx [ekey server-map]]
   (let [db               (:db cofx)
         {:keys [server]} server-map]
   {:db (-> db (assoc :server server-map)
               (update-in [:server] dissoc :show-server-info))
    :http-xhrio {:method          :get
                 :uri             (str (:url server-map) "/api/objects")
                 :headers         {:Authorization (str "Bearer " (:token server-map))}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::login-success]
                 :on-failure      [::login-failure]}}
   )))

(re-frame/reg-event-db ::show-server-info 
 (fn [db [_ _]]
   (assoc-in db [:server :show-server-info] true)))

(re-frame/reg-event-fx ::login-success
  (fn [cofx [_ response]]
                       (println "login sucess" (str response))
    {:fx [[:dispatch [::heedy-receive-objects response]]]}
    ))

(re-frame/reg-event-db ::login-failure
  (fn [db [_ response]]
    (-> db
        (assoc-in [:server :show-server-info] true)
        (assoc-in [:server :error] response)
        )))


(re-frame/reg-event-fx ::heedy-get-objects
 (fn [{:keys [db]} [_ _]]
   {:http-xhrio {:method          :get
                 :uri             (str (-> db :server :url) "/api/objects" )
                 :headers         {:Authorization (str "Bearer " (-> db :server :token))}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::heedy-receive-objects]
                 :on-failure      []}}
   ))

(re-frame/reg-event-fx ::heedy-receive-objects store-objects-interceptor
 (fn [{:keys [db]} [_ response]] 
   {:db (assoc db :heedy-objects response)}
   ))

;(re-frame/reg-event-fx ::heedy-get-object)
;(re-frame/reg-event-fx ::heedy-get-timeseries)
;(re-frame/reg-event-fx ::heedy-post-timeseries)

(re-frame/reg-event-fx ::add-to-basket 
 [(re-frame/inject-cofx :now) store-basket-interceptor]
 (fn [{:keys [db now]} [_ item]]
     {:db (assoc-in db [:basket now] item)}
   ))

(re-frame/reg-event-db ::remove-all-from-basket store-basket-interceptor
  (fn [db [_ _]]
    (assoc db :basket {})))

(re-frame/reg-event-db ::remove-last-from-basket store-basket-interceptor
  (fn [db [_ _]]
    (let [last-date (last (keys (:basket db)))]
      (update-in db [:basket] dissoc last-date))))

(re-frame/reg-event-db ::upload-to-heedy-success store-basket-interceptor
 (fn [db [_ date response]]
   (if (= "ok" (:result response))
     (update-in db [:basket] dissoc date)
     (update-in db [:error-messages] conj response)
     )))

(re-frame/reg-event-db ::upload-to-heedy-failure
 (fn [db [_ error-response]]
   (update-in db [:error-messages] conj error-response)
   ))

(re-frame/reg-event-fx ::upload-to-heedy
  (fn [{:keys [db]} [_ _]]
    (let [basket (:basket db)]
      {:http-xhrio
        (for [[date [id _ value]] basket
              :let [sdate (-> date (/ 1000.0) int) ;Heedy dislikes millisecond epoch
                    body [{:t sdate :d value}]]]
          {:method          :post
           :uri             (str (-> db :server :url) "/api/objects/" id "/timeseries" )
           :params          body
           :headers         {:Authorization (str "Bearer " (-> db :server :token))}
           :format          (ajax/json-request-format)
           :response-format (ajax/json-response-format {:keywords? true})
           :on-success      [::upload-to-heedy-success date]
           :on-failure      [::upload-to-heedy-failure]}
          )
       }
    )))

(re-frame/reg-event-db ::show-errors
  (fn [db [_ _]]
    (assoc db :show-errors true)))

(re-frame/reg-event-db ::hide-errors
  (fn [db [_ _]]
    (dissoc db :show-errors )))

(re-frame/reg-event-db ::clear-errors
  (fn [db [_ _]]
    (-> db 
        (dissoc :show-errors )
        (assoc :error-messages [] ))))
