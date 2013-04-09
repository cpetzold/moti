(ns moti.vector)

(defn dotprod [a b]
  (apply + (map * a b)))

(defn length [a]
  (Math/sqrt (dotprod a a)))

(defn normal [[x y]]
  [(- y) x])

(defn unit [a]
  (let [len (length a)]
    (if (zero? len)
      [0 0]
      (map #(/ % len) a))))
