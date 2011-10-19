(ns clj-epub.core
  "input and output EPUB files"
  (:use [clj-epub epub zipf markup])
  (:import [java.io ByteArrayOutputStream]
           [java.util UUID]))


(def default-values
     "Default EPUB metadata"
     {:title "Untitled"
      :author "Nobody"
      :book-id generate-uuid
      :language :en})


(defn generate-uuid
  "Generate uuid for OPF element dc:identifier(BookID)"
  []
  (str (UUID/randomUUID)))


(defn- write-epub
  "Write EPUB on zip file"
  [zos epub]
  (stored zos (:mimetype epub))
  (doseq [key [:meta-inf :content-opf :toc-ncx]]
    (deflated zos (key epub)))
  (doseq [t (:html epub)]
    (deflated zos t))
  (.flush zos))


(defn text->epub
  "Generate EPUB data. Args are epub title of metadata, includes text files."
  [{input-files :inputs title :title author :author markup-type :markup book-id :id lang :language}]
  (let [eptexts  (files->epub-texts markup-type input-files)
        metadata {:title    (or title (:title default-values))
                  :author   (or author (:author default-values))
                  :id       (or book-id (:book-id default-values))
                  :sections eptexts
                  :language (or lang (:lang default-values))}]
    {:mimetype    (mimetype)
     :meta-inf    (meta-inf)
     :content-opf (content-opf metadata)
     :toc-ncx     (toc-ncx (:id metadata) eptexts)
     :html        eptexts}))


(defn epub->file
  "Output EPUB file from apply EPUB info"
  [epub filename]
  (with-open [zos (open-zipfile filename)]
    (write-epub zos epub)))


(defn epub->byte
  "Output EPUB byte array"
  [epub]
  (with-open [baos (ByteArrayOutputStream.)
              zos (open-zipstream baos)]
    (write-epub zos epub)
    (.toByteArray baos)))

