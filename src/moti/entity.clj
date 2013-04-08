(ns moti.entity)

(defprotocol PEntity
  (update [this timer state])
  (display [this timer state]))

(defn update-velocity [entity]
  (update-in
   entity [:vel]
   (fn [vel]
     (->> vel
         (map + (:acc entity))
         (map #(* 0.95 %))))))

(defn update-position [entity]
  (let [entity (update-velocity entity)]
    (update-in
     entity [:pos]
     #(map + % (:vel entity)))))