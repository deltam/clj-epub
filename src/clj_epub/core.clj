(ns clj-epub.core
  "input and output EPUB files"
  (:use [clj-epub epub zipf markup]
        [clojure.java.io :only (file)])
  (:import [java.io ByteArrayOutputStream]
           [java.util UUID]))


(defn generate-uuid
  "Generate uuid for OPF element dc:identifier(BookID)"
  []
  (str (UUID/randomUUID)))


(def #^{:doc "Default EPUB metadata."}
     default-metadata
     {:title "Untitled"
      :author "Nobody"
      :book-id generate-uuid ; random
      :language "en"})


(defn- write-epub
  "Write EPUB on zip file"
  [zos epub]
  (stored zos (:mimetype epub))
  (doseq [key [:meta-inf :content-opf :toc-ncx]]
    (deflated zos (key epub)))
  (doseq [t (:sections epub)]
    (deflated zos t))
  (.flush zos))


;(defn str->epub

(defn textfile->epub
  "Generate EPUB data. Args are epub title of metadata, includes text files."
  [{input-files :inputs title :title author :author markup-type :markup book-id :id lang :language}]
  (let [sections (files->sections input-files markup-type)
        metadata {:title    (or title   (:title    default-metadata))
                  :author   (or author  (:author   default-metadata))
                  :id       (or book-id (:book-id  default-metadata))
                  :language (or lang    (:language default-metadata))
                  :sections sections}]
    {:mimetype    (mimetype)
     :meta-inf    (meta-inf)
     :content-opf (content-opf metadata)
     :toc-ncx     (toc-ncx (:id metadata) sections)
     :sections    sections}))


(defn epub->file
  "Return java.io.File of EPUB file. Output EPUB file from apply EPUB info."
  [epub filename]
  (with-open [zos (open-zipfile filename)]
    (write-epub zos epub))
  (file filename))


(defn epub->byte
  "Output EPUB byte array"
  [epub]
  (with-open [baos (ByteArrayOutputStream.)
              zos (open-zipstream baos)]
    (write-epub zos epub)
    (.toByteArray baos)))



;;; EPUB Generation DSL

(defn to-sections
  "Return EPUB Section from String, File. 
    Example:
    (def sec [{:chapter \"chapter1\" :text \"first . \"}
           {:chapter \"chapter2\" :html \"<b>write html</b>\"}
           {:chapter \"make by file\" :file \"samples/hello.md\" :type :markdown}
           {:chapter \"make by plain text file\" :file \"samples/hello.txt\" :type :plain}])"
  [section-data]
  (reduce concat-sections
          (map #(let [type (or (:type %) :plain)]
                  (if (:text %) (text->sections (:chapter %) (:text %) type)
                      (if (:file %) (text->sections (:chapter %) (slurp (:file %)) type)
                          (if (:html %) (text->sections (:chapter %) (:html %) type)))))
               section-data)))

(defn make-epub
  ""
  [body]
  (let [sections (to-sections (:sections body))
        metadata {:title (:title body)
                  :author (:author body)
                  :book-id (if (= :random (:book-id body))
                             (generate-uuid)
                             (:book-id body))
                  :language (:language body)}]
    {:mimetype    (mimetype)
     :meta-inf    (meta-inf)
     :content-opf (content-opf metadata)
     :toc-ncx     (toc-ncx (:id metadata) sections)
     :sections    sections}))
                  
                  

;    (defepub my-epub
;      :title "my-epub"
;       :author "deltam"
;       :book-id :random
;       :language "en"
;       :sections [
;         {:chapter "chapter1"
;          :text "first chapter"}
;         {:chapter "chapter2"
;          :html "<b>write html</b>"}
;         {:chapter "make by file"
;          :file "./samples/README.md"
;          :type :markdown}
;         {:chapter "make by html"
;          :file "./samples/test.html"
;          :type :html}
;         {:chapter "make by plain text"
;          :file "./samples/test.txt"
;          :type :plain}]
;       :resources {"samples/test.gif" "samples/abc.css"}))
;    (epub->file my-epub "my-epub.epub")
