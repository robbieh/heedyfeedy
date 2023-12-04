(ns heedyfeedy.db
  (:require
   [re-frame.core :as re-frame]
   [cljs.reader]
   ))

(def ls-server-config-key "heedyfeedy-server-config")
(def ls-basket-key "heedyfeedy-basket")
(def ls-objects-key "heedyfeedy-objects")

;(.removeItem js/localStorage ls-server-config-key )
(def default-db
  {
   :server (into {} (some->> (.getItem js/localStorage ls-server-config-key)
                             (cljs.reader/read-string)))
   :heedy-objects (some->> (.getItem js/localStorage ls-objects-key)
                             (cljs.reader/read-string))
   :heedy-send-queue {}
   :basket (into {} (some->> (.getItem js/localStorage ls-basket-key)
                             (cljs.reader/read-string)))
   :error-messages []
   })

(defn server-config->local-store [^PersistentArrayMap db]
  (.setItem js/localStorage ls-server-config-key (str (:server db))))

(defn basket->local-store [^PersistentArrayMap db]
  (.setItem js/localStorage ls-basket-key (str (:basket db))))

(defn objects->local-store [^PersistentArrayMap db]
  (.setItem js/localStorage ls-objects-key (str (:heedy-objects db))))

