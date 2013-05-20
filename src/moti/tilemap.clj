(ns moti.tilemap
  (:require
   [penumbra.opengl :as gl]
   [moti.entity :as entity]
   [moti.draw :as draw]))


(def +test+
  [[1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1]
   [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]])


(defrecord Tilemap [tiles tile-size]
  entity/PEntity
  (update [this dt state]
    this)

  (display [this dt state]
    (gl/color 0.2 0.2 0.2)
    (doseq [[y row] (map-indexed vector tiles)
            [x tile] (map-indexed vector row)
            :when (pos? tile)
            :let [pos (map * (repeat tile-size) [x y])]]
      (draw/rectangle pos (repeat 2 tile-size)))))

(defn make-tilemap [tiles tile-size]
  (Tilemap. tiles tile-size))