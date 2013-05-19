(ns moti.draw
  (:require
   [penumbra.opengl :as gl]))

(defn vector [pos v]
  (gl/push-matrix
   (apply gl/translate pos)
   (gl/draw-lines
    (gl/vertex 0 0)
    (apply gl/vertex v))))

(defn rectangle [[x y] [w h]]
  (gl/push-matrix
   (gl/translate x y)
   (gl/draw-quads
    (doseq [[px py] [[0 0] [w 0] [w h] [0 h]]]
      (gl/vertex px py)))))
