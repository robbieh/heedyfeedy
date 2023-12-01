(ns heedyfeedy.db
  (:require
   [re-frame.core :as re-frame]
   ))

(def ls-server-config-key "heedyfeedy-server-config")

;(.removeItem js/localStorage ls-server-config-key )
(def default-db
  {:name "HeedyFeedy"
   :server (into {} (some->> (.getItem js/localStorage ls-server-config-key)
                             (cljs.reader/read-string)))
   :heedy-objects {}
   :heedy-ts {}
   :heedy-send-queue {}
   :basket {}
   :error-messages []
   })

(defn server-config->local-store [^PersistentArrayMap db]
  (println "to local store")
  (println db)
  (.setItem js/localStorage ls-server-config-key (str (:server db))))


(re-frame/reg-cofx :local-store-server-config
 (fn [cofx _]
   (assoc cofx :local-store-server-config
          (into (sorted-map) (some->>
                              (.getItem js/localStorage ls-server-config-key)
                              (cljs.reader/read-string))))))
