(ns moti.entity
  (:use plumbing.core)
  (:require
   [moti.vector :as vector]))

(defprotocol PEntity
  (update [this timer state])
  (display [this timer state])
  (collision-offset [this entity])
  (collision-response [this offset]))

(defn vertices [{:keys [pos dim]}]
  (let [[x y] pos
        [w h] dim]
    [[x y]
     [(+ x w) y]
     [(+ x w) (+ y h)]
     [x (+ y h)]]))

(defn axis [p1 p2]
  (->> p2 (map - p1) vector/normal vector/unit))

(defn axes [vertices]
  (->> (-> vertices cycle rest)
       (map axis vertices)
       (distinct-by (fn [[x y]]
                      (Math/abs
                       (if (zero? x) 0 (/ y x)))))))

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