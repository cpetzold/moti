(ns moti.entity)

(defprotocol PEntity
  (update [this timer state])
  (display [this timer state]))

(defn update-velocity [entity dt]
  (update-in
   entity [:vel]
   (fn [vel]
     (->> vel
          (map (fn [acc vel] (+ vel (* acc dt))) (:acc entity))
          (map #(* 0.99 %))))))

(defn update-position [entity dt]
  (let [entity (update-velocity entity dt)]
    (update-in
     entity [:pos]
     #(map + % (:vel entity)))))