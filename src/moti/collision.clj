(ns moti.collision
  (:require
   [penumbra.opengl :as gl]
   [moti.vector :as vector]
   [moti.draw :as draw]
   [moti.entity :as entity]))

(defn overlap [[a0 a1] [b0 b1]]
  (cond
   (> a1 b1) (- (overlap [b0 b1] [a0 a1]))
   (or (>= a0 b1) (>= b0 a1)) 0
   (<= a0 b0) (- a1 b0)
   :else (- b1 a0)))

(defn project-vertices [axis vertices]
  (->> vertices
       (map #(vector/scalar-projection % axis))
       sort
       ((juxt first last))))

(defn sat [a b]
  (let [vert-seqs (map entity/vertices [a b])
        axes [[0 1] [1 0]]
        #_(->> vert-seqs (mapcat entity/axes) distinct)
        overlaps (for [axis axes]
                   (->> vert-seqs
                        (map #(project-vertices axis %))
                        (apply overlap)
                        repeat
                        (map * axis)))]
    (when (every? (fn [v] (some #(not (zero? %)) v)) overlaps)
      (println overlaps)
      (->> overlaps
           (sort-by vector/length)
           first))))