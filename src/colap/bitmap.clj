(ns colap.bitmap
  {:doc "Operations on a bitmap, which is implemented by an 
        EWAHCompressedBitmap"
   :author "Huahai Yang"}
  (:use [slingshot.slingshot :only [throw+]])
  (:import [javaewah EWAHCompressedBitmap]
           [colap.bytebuffer OutputStream InputStream]
           [java.nio ByteBuffer]
           [java.io DataOutputStream DataInputStream]))

(defn bitmap->positions 
  "Return a seq of the positions of 1 in the bitmap"
  [^EWAHCompressedBitmap bm]
  (iterator-seq (.iterator bm)))

(defn append-position! 
  "Set the corresponding positions of the given bitmap according to the
  given integers, which should be greater than the existing positions,
  unique and in ascending order."
  ([^EWAHCompressedBitmap bm p] 
    (if (< p (.sizeInBits bm)) 
      (throw+ "Can only append to the end of a bitmap") 
      (.set bm p)) 
    bm)
  ([^EWAHCompressedBitmap bm p & ps] 
    (doseq [x (cons p ps)] (append-position! bm x))
    bm))

(defn positions->bitmap
  "Load a collection of unordered 1 positions into a new bitmap"
  [coll]
  (apply append-position! (EWAHCompressedBitmap.) (apply sorted-set coll)))

(defn- insert-to-list
  [^java.util.List l p]
  (let [i (java.util.Collections/binarySearch l p)]
    (when (< i 0)
      (.add l (- (inc i)) p))
    l))

(defn- remove-from-list
  [^java.util.List l p]
  (let [i (java.util.Collections/binarySearch l p)]
    (when (>= i 0)
      (.remove l i))
    l))

(defn- do-at-positions
  [^EWAHCompressedBitmap bm f coll] 
  (let [l (.getPositions bm)]
    (doseq [p coll] (f l (int p)))
    (apply append-position! (EWAHCompressedBitmap.) (seq l))))

(defn insert-position
  "Return a new bitmap with given positions added"
  ([^EWAHCompressedBitmap bm p] 
    (.or bm (append-position! (EWAHCompressedBitmap.) p)))
  ([^EWAHCompressedBitmap bm p & ps] 
    (let [coll (cons p ps) n (count coll)]
      (if (< n 22500) 
        (do-at-positions bm insert-to-list coll)
        (.or bm (positions->bitmap coll))))))

(defn remove-position
  "Return a new bitmap with given positions removed"
  ([^EWAHCompressedBitmap bm p]
    (apply append-position! (EWAHCompressedBitmap.) 
           (seq (remove-from-list (.getPositions bm) (int p)))))
  ([^EWAHCompressedBitmap bm p & ps] 
    (do-at-positions bm remove-from-list (cons p ps))))

(defn- in-list?
  [^java.util.List l p] 
  (>= (java.util.Collections/binarySearch l (int p)) 0))

(defn position-set?
  "Return true if the given positions of the bitmp are set"
  ([^EWAHCompressedBitmap bm p]
    (in-list? (.getPositions bm) p))
  ([^EWAHCompressedBitmap bm p & ps]
    (let [l (.getPositions bm)] 
      (every? #(in-list? l %) (cons p ps)))))
  
(defn bitmap->bytebuffer
  "Return a Bytebuffer filled with the given bitmap"
  [^EWAHCompressedBitmap bm]
  (let [bb (ByteBuffer/allocate (.serializedSizeInBytes bm))] 
    (.serialize bm (-> bb 
                     (OutputStream.) 
                     (DataOutputStream.)))
    (.rewind bb)))

(defn bytebuffer->bitmap
  "Recover a bitmap from the given ByteBuffer"
  [bb]
  (let [bm (EWAHCompressedBitmap.)]
    (.deserialize bm (-> bb
                       (InputStream.)
                       (DataInputStream.)))
    bm))

