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
   [moti.collision :as collision]
   [moti.tilemap :as tilemap])
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
            :else 30)])
        (entity/update-position dt)))

  (display [this dt state]
    (gl/color 0 1 0)
    (draw/rectangle pos dim))

  (collision-response [this offset]
    (if-not offset
      this
      (let [vel-halt (map #(if (zero? %) 1 0) offset)]
        (-> this
            (update-in [:pos] #(map - % offset))
            (update-in [:vel] #(map * % vel-halt)))))))

(defn display [[dt t] state]
  (doseq [[_ e] (select-keys state [:player :tilemap])]
    (entity/display e dt state))
  (text/write-to-screen (format "%s" (int (/ 1 dt))) 10 10)
  (app/repaint!))

(defn update [[dt t] state]
  (if (:paused state)
    state
    (-> state
        (update-in [:player] #(entity/update % dt state))
        (update-in [:player] #(entity/collision-response
                               % (entity/collision-offset (:tilemap state) %))))))

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
   :tilemap (tilemap/make-tilemap tilemap/+test+ 64)
   :player (Player. [0 0] [0 0] [512 0] [12 24])})

(defn jump [state]
  (update-in state [:player :vel] (fn [[vx vy]] [vx (- vy 20)])))

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