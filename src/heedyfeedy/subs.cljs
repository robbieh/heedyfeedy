(ns heedyfeedy.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub :server :-> :server)
(re-frame/reg-sub :basket :-> :basket)
(re-frame/reg-sub :error-messages :-> :error-messages)
(re-frame/reg-sub :heedy-objects :-> :heedy-objects)

(defn add-display-type [object-map]
  (let [
        display-type (get-in object-map [:meta :schema :type])
        enum?        (get-in object-map [:meta :schema :enum])
        x-display    (get-in object-map [:meta :schema :x-display])
        display-type (if enum? "enum" display-type)
        display-type (if x-display x-display display-type)
        ]
    (assoc object-map :display-type display-type)))

(defn add-group [object-map]
  (case (:display-type object-map)
    "enum" (assoc object-map :group (:name object-map))
    (assoc object-map :group "General")
  ))

(re-frame/reg-sub :heedy-objects-annotated
 :<- [:heedy-objects]
 (fn [objects query-vec]
    (let [updated  (for [object objects]   
                     (-> object add-display-type add-group))
          grouped  (group-by :group updated)]
      vec grouped)))

(re-frame/reg-sub :heedy-send-queue
  (fn [db] (:heedy-send-queue db)))

;(re-frame/reg-sub :error-messages
;  (fn [db _] (:error-messages db)))
