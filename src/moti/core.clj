(ns moti.core
  (:require
   [penumbra.app :as app]
   [penumbra.opengl :as gl]
   [penumbra.opengl.effects :as effects]
   [penumbra.text :as text]
   [moti.vector :as vector]
   [moti.entity :as entity]
   [moti.collision :as collision])
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

(defn draw-vector [pos v]
  (gl/push-matrix
   (apply gl/translate pos)
   (gl/draw-lines
    (gl/vertex 0 0)
    (apply gl/vertex v))))

(defn draw-rectangle [pos dim]
  (let [[hw hh] (map #(/ % 2) dim)]
    (gl/push-matrix
     (apply gl/translate pos)
     (gl/draw-quads
      (doseq [[x y] [[(- hw) hh] [hw hh] [hw (- hh)] [(- hw) (- hh)]]]
        (gl/vertex x y))))))

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
            :else 20)])
        (entity/update-position dt)))
  
  (display [this dt state]
    (gl/color 0 1 0)
    (draw-rectangle pos dim)))

(defrecord Wall [pos dim]
  entity/PEntity
  (update [this dt state] this)
  (display [this dt state]
    (gl/color 0.2 0.2 0.2)
    (draw-rectangle pos dim)))

(defn display-entities [dt state]
  (doseq [entity (:entities state)]
    (entity/display entity dt state)))

(defn display [[dt t] state]
  (text/write-to-screen (format "%s" (int (/ 1 dt))) 10 10)
  (display-entities dt state)
  (app/repaint!))

(defn collide [a b]
  (let [[px py] (collision/aabb a b)]
    (if (and px py)
      (let [horizontal (< (Math/abs px) (Math/abs py))]
        (-> a
            (update-in
             [:vel]
             (fn [[vx vy]]
               (if horizontal
                 [0 vy]
                 [vx 0])))
            (update-in
             [:pos]
             (fn [[x y]]
               (if horizontal
                 [(+ x px) y]
                 [x (- y py)])))))
      a)))

(defn update-entities [entities dt state]
  (-> (map #(entity/update % dt state) entities)
      vec
      (update-in [1] #(collide % (nth entities 0)))))

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
   [(Wall. [400 550] [800 100])
    (Player. [0 0] [0 0] [400 300] [12 24])]})

(defn jump [state]
  (println (get-in state [:entities 1 :vel]))
  (update-in state [:entities 1 :vel] (fn [[vx vy]] [vx -10])))

(defn key-press [key state]
  (case key
    "r" (init-state)
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