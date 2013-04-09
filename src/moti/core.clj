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

(defn draw-rectangle [pos [w h]]
  (gl/push-matrix
   (apply gl/translate pos)
   (gl/draw-quads
    (doseq [[x y] [[0 0] [w 0] [w h] [0 h]]]
      (gl/vertex x y)))))

(defrecord Player [acc vel pos]
  entity/PEntity
  (update [this dt state]
    (-> this
        (assoc :acc
          [(cond
            (app/key-pressed? :left) -30
            (app/key-pressed? :right) 30
            :else 0)
           (cond
            (app/key-pressed? :up) -30
            :else 20)])
        (entity/update-position dt)))
  
  (display [this dt state]
    (gl/color 0 1 0)
    (draw-rectangle pos [12 24])))

(defrecord Wall [pos dim]
  entity/PEntity
  (update [this dt state] this)
  (display [this dt state]
    (gl/color 1 1 1)
    (draw-rectangle pos dim)))

(defn display-entities [dt state]
  (doseq [entity (:entities state)]
    (entity/display entity dt state)))

(defn display [[dt t] state]
  (text/write-to-screen (format "%s" (int (/ 1 dt))) 10 10)
  (display-entities dt state)
  (app/repaint!))

(defn update-entities [entities dt state]
  (map #(entity/update % dt state) entities))

(defn update [[dt t] state]
  (-> state
      (update-in [:entities] #(update-entities % dt state))))

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

(defn init-state []
  {:entities
   [(Player. [0 0] [0 0] [100 100])
    (Wall. [0 500] [800 100])]})

(defn key-press [key state]
  (case key
    "r" (init-state)
    state))

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