(ns clj-epub.core
  "input and output EPUB files"
  (:use [clj-epub epub zipf markup])
  (:import [java.io ByteArrayOutputStream]))


(defn- write-epub
  "write EPUB on zip file"
  [zos epub]
  (stored zos (:mimetype epub))
  (doseq [key [:meta-inf :content-opf :toc-ncx]]
    (deflated zos (key epub)))
  (doseq [t (:html epub)]
    (deflated zos t))
  (.flush zos))


(defn text->epub
  "generate EPUB data. args are epub title of metadata, includes text files."
  [{input-files :input title :title author :author markup-type :markup book-id :id}]
  (let [id       (or book-id (generate-uuid))
        eptexts  (files->epub-texts markup-type input-files)]
    {:mimetype    (mimetype)
     :meta-inf    (meta-inf)
     :content-opf (content-opf title (or author "Nobody") id eptexts)
     :toc-ncx     (toc-ncx id eptexts)
     :html        eptexts}))


(defn epub->file
  "output EPUB file from apply EPUB info"
  [epub filename]
  (with-open [zos (open-zipfile filename)]
    (write-epub zos epub)))


(defn epub->byte
  "output EPUB bytes"
  [epub]
  (with-open [baos (ByteArrayOutputStream.)
              zos (open-zipstream baos)]
    (write-epub zos epub)
    (.toByteArray baos)))

