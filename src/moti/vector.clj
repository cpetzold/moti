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

(defn scalar-projection [a b]
  (let [len (length b)]
    (if (zero? len)
      0
      (/ (dotprod a b) len))))

(defn projection [a b]
  (let [b-len (length b)]
    (if (zero? b-len)
      [0 0]
      (map *
           (repeat (scalar-projection a b))
           (map / b (repeat b-len))))))
