(ns colap.cassandra
  {:doc "Use Cassandra as data store"
   :author "Huahai Yang"}
  (:use [clj-hector.ddl]
        [clj-hector.core]
        [colap.bitmap])
  (:import [me.prettyprint.hector.api Serializer]))

(def ^:dynamic *keyspace* (keyspace (cluster "Test Cluster" "localhost") "testks"))
(def ^:dynamic *cf-name* "User")

(defn bm-serializer
  "return an instance of hector Serializer that can serialize/deserialize 
  an EWAHCompresedBitmap to/from ByteBuffer, "
  [bm]
  (proxy [Serializer] []
    (toByteBuffer [bm]
      (bitmap->bytebuffer bm))
    (fromByteBuffer [bb]
      (bytebuffer->bitmap bb))))

(defn store-bitmap
  "store a EWAHCompresedBitmap as a column value"
  [dim val bm]
  (put *keyspace* *cf-name* dim {val bm} 
       :n-serializer :string :v-serializer bm-serializer)) 

(defn retrieve-bitmap
  "retrieve a EWAHCompresedBitmap as a column value"
  [dim val]
  (-> (get-columns *keyspace* *cf-name* dim val 
                   :n-serializer :string :v-serializer bm-serializer) 
    (get val)))
