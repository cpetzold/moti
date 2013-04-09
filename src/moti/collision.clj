(ns moti.collision)

(defn position-delta [a b]
  (->> (map :pos [a b])
       (apply map -)
       #_(map #(Math/abs %))))

(defn half-dimensions [entity]
  (map #(/ % 2) (:dim entity)))

(defn aabb [a b]
  (let [pos-delta (position-delta a b)
        half-dims (->> (map half-dimensions [a b])
                       (apply interleave)
                       (partition 2))]
    #_(println pos-delta)
    (map
     (fn [delta [d1 d2]]
       (let [s (+ d1 d2)]
         (when (pos? (- s (Math/abs delta)))
           (if (pos? delta)
             (- delta s)
             (+ s delta)))))
     pos-delta
     half-dims)))