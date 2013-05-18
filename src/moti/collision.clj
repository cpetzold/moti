(ns moti.collision
  (:require
   [moti.vector :as vector]
   [moti.entity :as entity]))

(defn overlap [[min1 max1] [min2 max2]]

  )

(defn project-vertices [axis vertices]
  (->> vertices
       (map #(vector/projection % axis))
       (sort-by vector/length)
       #_((juxt first last))))

(defn sat [a b]
  (let [verts (map entity/vertices [a b])
        axes (->> verts (mapcat entity/axes) distinct)]
    (for [axis axes]
      (->> verts
           (map #(project-vertices axis %))
           (apply overlap)))))