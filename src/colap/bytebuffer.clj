(ns colap.bytebuffer
  {:doc "Define IO streams that are directly backed by a given 
        java.nio.ByteBuffer. This nanmespace needs to be AOT compiled."
   :author "Huahai Yang"})

(gen-class 
  :name colap.bytebuffer.OutputStream
  :extends java.io.OutputStream
  :init init
  :state state
  :constructors {[java.nio.ByteBuffer] []}
  :exposes-methods {write writeSuper}
  :main false)

(defn -init 
  "Initialize the stream with a given ByteBuffer"
  [bb]
  [[] bb])

(defn -write
  "Implements the write methods of java.io.OutputStream"
  ([^colap.bytebuffer.OutputStream this b]
    (if (= (type b) (Class/forName "[B")) 
      (.writeSuper this ^bytes b)
      (.put ^java.nio.ByteBuffer (.state this) 
            (clojure.lang.RT/uncheckedByteCast ^int b))))
  ([^colap.bytebuffer.OutputStream this ^bytes b o l]
    (.writeSuper this b ^int o ^int l)))

(gen-class 
  :name colap.bytebuffer.InputStream
  :extends java.io.InputStream
  :init init
  :state state
  :constructors {[java.nio.ByteBuffer] []}
  :exposes-methods {read readSuper}
  :main false)

(defn -read
  "Implements the read methods of java.io.InputStream"
  ([^colap.bytebuffer.InputStream this]
    (let [bb ^java.nio.ByteBuffer (.state this)] 
      (if (.hasRemaining bb) 
        (clojure.lang.RT/uncheckedIntCast (bit-and 0xff ^byte (.get bb)))
        (int -1))))
  ([^colap.bytebuffer.InputStream this ^bytes b]
    (.readSuper this b))
  ([^colap.bytebuffer.InputStream this ^bytes b o l]
    (.readSuper this b ^int o ^int l)))

