(ns moti.tilemap
  (:require
   [penumbra.opengl :as gl]
   [moti.entity :as entity]
   [moti.draw :as draw]
   [moti.collision :as collision]))

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

(defrecord Tile [type pos dim]
  entity/PEntity
  (update [this dt state]
    this)

  (display [this dt state]
    (gl/color 0.2 0.2 0.2)
    (draw/rectangle pos dim))

  (collision-offset [this entity]
    (collision/sat entity this)))

(defrecord Tilemap [tiles]
  entity/PEntity
  (update [this dt state]
    (doseq [tile tiles]
      (entity/update tile dt state)))

  (display [this dt state]
    (doseq [tile tiles]
      (entity/display tile dt state)))

  (collision-offset [this entity]
    (->> tiles
         (map #(entity/collision-offset % entity))
         (drop-while nil?)
         first)))

(defn make-tilemap [tile-data tile-size]
  (Tilemap.
   (for [[y row] (map-indexed vector tile-data)
         [x tile-type] (map-indexed vector row)
         :when (pos? tile-type)
         :let [pos (map * (repeat tile-size) [x y])]]
     (Tile. tile-type pos (repeat 2 tile-size)))))