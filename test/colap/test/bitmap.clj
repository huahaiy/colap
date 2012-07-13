(ns colap.test.bitmap
  {:author "Huahai Yang"}
  (:use [colap.bitmap] 
        [clojure.test]
        [clojure.java.io])
  (:import [javaewah EWAHCompressedBitmap]))

(def ^:private max-pos 2147483583) ;the maximum settable bit (Integer.MAX_VALUE - 64)
(def ^:private ext-bm (apply append-position! (EWAHCompressedBitmap.) [0 max-pos]))
(def ^:private rnd-bm (EWAHCompressedBitmap.))

(declare ^:private ^:dynamic rnd-coll)

(defn rnd-ints 
  [n limit]
  (repeatedly n #(rand-int limit)))

(defn rnd-bitmaps-fixture
  [f]
  (binding [rnd-coll (rnd-ints 100 max-pos)]
    (try 
      (apply append-position! rnd-bm (apply sorted-set rnd-coll))
      (f)
      (finally 
        (.clear rnd-bm)))))

(use-fixtures :each rnd-bitmaps-fixture)

(deftest from-to-positions
  (is (= (bitmap->positions ext-bm) [0 max-pos]))
  (is (= (bitmap->positions rnd-bm) (seq (apply sorted-set rnd-coll))))
  (is (= ext-bm (positions->bitmap (bitmap->positions ext-bm))))
  (is (= rnd-bm (positions->bitmap (bitmap->positions rnd-bm)))))

(deftest insert-remove
  (is (= (bitmap->positions (insert-position ext-bm 1)) [0 1 max-pos]))
  (is (= (insert-position ext-bm 3 2 729) (positions->bitmap [0 2 3 729 max-pos])))
  (let [s (set (bitmap->positions rnd-bm))
        xs (filter #(nil? (s %)) (range))
        x (first xs)
        nf (take 5 xs)] 
    (is (= rnd-bm (remove-position (insert-position rnd-bm x) x)))
    (is (= rnd-bm (apply remove-position (apply insert-position rnd-bm nf) nf)))))

(deftest set-or-not
  (is (position-set? ext-bm max-pos))
  (is (position-set? ext-bm 0 max-pos))
  (is (position-set? (insert-position rnd-bm 289) 289))
  (is (position-set? (insert-position rnd-bm 3 899) 899 3)))

(deftest from-to-bytebuffer 
  (is (= ext-bm (bytebuffer->bitmap (bitmap->bytebuffer ext-bm))) 
      "Error serializing bitmap to bytebuffer")
  (is (= rnd-bm (bytebuffer->bitmap (bitmap->bytebuffer rnd-bm))) 
      "Error serializing bitmap to bytebuffer"))

(defn benchmark 
  [n i f x]
  (str i "," 
       (second (first 
                 (re-seq #"[^\d]+(\d+\.\d+).+" 
                         (with-out-str 
                           (time (dotimes [_ n] (f x))))))) 
       "\n"))

(defn run-benchmark
  [f o] 
  (with-open [w (writer o :append true)]
    (doseq [x (rnd-ints 1000 40000)] 
      (.write w (benchmark 1 x f (rnd-ints x max-pos))))
    ;(doseq [x (rnd-ints 1000 max-pos)] 
      ;(.write w (benchmark 100 x f [x])))
    (.flush w)))
