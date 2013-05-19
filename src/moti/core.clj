(ns moti.core
  (:use plumbing.core)
  (:require
   [penumbra.app :as app]
   [penumbra.opengl :as gl]
   [penumbra.opengl.effects :as effects]
   [penumbra.text :as text]
   [moti.vector :as vector]
   [moti.draw :as draw]
   [moti.entity :as entity]
   [moti.collision :as collision])
  (:import
   [org.lwjgl.opengl DisplayMode]))

(def +dim+ [1024 768])
(def +center+ (map #(/ % 2) +dim+))

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

(defrecord Player [acc vel pos dim]
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
            (app/key-pressed? :down) 30
            :else 0)])
        (entity/update-position dt)))

  (display [this dt state]
    (gl/color 0 1 0)
    (draw/rectangle pos dim)))

(defrecord Wall [pos dim]
  entity/PEntity
  (update [this dt state] this)
  (display [this dt state]
    (gl/color 0.2 0.2 0.2)
    (draw/rectangle pos dim)))

(defn display-entities [dt state]
  (doseq [entity (:entities state)]
    (entity/display entity dt state)))

(defn display [[dt t] state]
  (text/write-to-screen (format "%s" (int (/ 1 dt))) 10 10)
  (display-entities dt state)
  (app/repaint!))

(defn collide [a b]
  (let [overlap (collision/sat a b)
        vel-halt (map #(if (zero? %) 1 0) overlap)]
    (if overlap
      (-> a
          (update-in [:pos] #(map - % overlap))
          (update-in [:vel] #(map * % vel-halt)))
      a)))

(defn update-entities [entities dt state]
  (-> (map #(entity/update % dt state) entities)
      vec
      (update-in [1] #(collide % (nth entities 0)))))

(defn update [[dt t] state]
  (if (:paused state)
    state
    (-> state
        (update-in [:entities] #(update-entities % dt state)))))

(defn init [state]
  (app/display-mode!
   (get-display-mode {:resolution [1024 768]}))
  (app/title! "Moti")
  (app/vsync! true)
  state)

(defn reshape [[x y w h] state]
  (gl/viewport x y w h)
  (gl/ortho-view x (+ x w) (+ y h) y -1 1)
  (gl/load-identity)
  state)

(defn init-state []
  {:paused false
   :entities
   [(Wall. [512 400] [300 100])
    (Player. [0 0] [0 0] [512 0] [12 24])]})

(defn jump [state]
  (update-in state [:entities 1 :vel] (fn [[vx vy]] [vx (- vy 20)])))

(defn key-press [key state]
  (case key
    "r" (init-state)
    "p" (update-in state [:paused] not)
    " " (jump state)
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