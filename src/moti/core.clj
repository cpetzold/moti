(ns moti.core
  (:require
   [penumbra.app :as app]
   [penumbra.opengl :as gl]
   [penumbra.opengl.effects :as effects]
   [penumbra.text :as text]
   [moti.entity :as entity])
  (:import
   [org.lwjgl.opengl DisplayMode]))

(defn get-display-mode [mode-map]
  (->> (app/display-modes)
       (filter
        #(= (select-keys % (keys mode-map)) mode-map))
       first))

(defn world->ortho [dim xy]
  (map
   (fn [c d]
     (dec (double (* 2 (/ c d)))))
   xy dim))

(defn draw-rectangle [[w h]]
  (gl/draw-quads
   (gl/vertex 0 0)
   (gl/vertex w 0)
   (gl/vertex w h)
   (gl/vertex 0 h)))

(defrecord Player [acc vel pos]
  entity/PEntity
  (update [this t state]
    (-> this
        (assoc :acc
          [(cond
            (app/key-pressed? :left) -1
            (app/key-pressed? :right) 1
            :else 0)
           0])
        entity/update-position))
  
  (display [this t state]
    (gl/push-matrix
     (apply gl/translate pos)
     (gl/color 0 1 0)
     (draw-rectangle [12 24]))))

(defn display [[dt t] state]
  (text/write-to-screen (format "%s" (int (/ 1 dt))) 10 10)
  (entity/display (:player state) [dt t] state)
  (app/repaint!))

(defn update [timer state]
  (update-in state [:player] #(entity/update % timer state)))

(defn init [state]
  (app/display-mode!
   (get-display-mode {:resolution [800 600]}))
  (app/title! "Moti")
  (app/vsync! true)
  state)

(defn reshape [[x y w h] state]
  (gl/viewport x y w h)
  (gl/ortho-view x (+ x w) (+ y h) y -1 1)
  (gl/load-identity)
  state)

(defn key-press [key state]
  (case key
    "r" (init-state)
    state))

(defn init-state []
  {:player (Player. [0 0] [0 0] [100 100])})

(defn start []
  (app/start
   {:init init
    :reshape reshape
    :update (fn [& args] (apply update args))
    :display (fn [& args] (apply display args))
    :key-press (fn [& args] (apply key-press args))}
   (init-state)))

(comment

  (future (start))
  
)