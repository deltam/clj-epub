(ns clj-epub.core
  "input and output EPUB files"
  (:use [clj-epub epub zipf markup]
        [clojure.java.io :only (file)])
  (:import [clj-epub.markup Chapter Section]
           [java.io ByteArrayOutputStream]
           [java.util UUID]))


(defn generate-uuid
  "Generate uuid for OPF element dc:identifier(BookID)"
  []
  (str (UUID/randomUUID)))


(def #^{:doc "Default EPUB metadata."}
     default-metadata
     {:title "Untitled"
      :author "Nobody"
      :id (fn [] (generate-uuid)) ; random
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
  (let [metadata {:title    (or title   (:title    default-metadata))
                  :author   (or author  (:author   default-metadata))
                  :id       (or book-id ((:id  default-metadata)));; eval generate-uuid
                  :language (or lang    (:language default-metadata))}
        sections (files->sections input-files markup-type)
        chapters (map #(Chapter. % (slurp %) markup-type) input-files)]
    {:metadata metadata
     :chapters chapters}))

(defn gen-epub-files
  ""
  [epub]
  (let [sections  (flatten (map #(text->sections %) (:chapters epub)))]
    {:mimetype    (mimetype)
     :meta-inf    (meta-inf)
     :content-opf (content-opf (:metadata epub) sections)
     :toc-ncx     (toc-ncx (:id (:metadata epub)) (:title (:metadata epub)) sections)
     :sections    sections}))

(defn epub->file
  "Return java.io.File of EPUB file. Output EPUB file from apply EPUB info."
  [epub filename]
  (with-open [zos (open-zipfile filename)]
    (write-epub zos (gen-epub-files epub)))
  (file filename))

(defn epub->byte
  "Output EPUB byte array"
  [epub]
  (with-open [baos (ByteArrayOutputStream.)
              zos (open-zipstream baos)]
    (write-epub zos epub)
    (.toByteArray baos)))



;;; EPUB Generation DSL

(defn to-chapters
  "Return Chapter from String or File,
   formatted Plain Text, Markdown, HTML.
    Example:
    (to-chapters [{:chapter \"chapter1\"
                   :text \"first . \"}
                  {:chapter \"chapter2\"
                   :html \"<b>write html</b>\"}
                  {:chapter \"make by file\"
                   :file \"samples/hello.md\"
                   :type :markdown}
                  {:chapter \"make by plain text file\"
                   :file \"samples/hello.txt\"
                   :type :plain}])"
  [source-data]
  (flatten
   (map #(let [chapter-by (fn [body] (Chapter. (:chapter %) body (:type %)))]
           (cond
            (:text %) (chapter-by (:text %))
            (:file %) (chapter-by (slurp (:file %)))
            (:html %) (chapter-by (:html %))))
        source-data)))

(defn make-epub
  ""
  [body]
  (let [chapters (to-chapters (:sections body))
        metadata {:title (:title body)
                  :author (:author body)
                  :id (if (= :random (:book-id body))
                             (generate-uuid)
                             (:book-id body))
                  :language (:language body)}]
    {:metadata metadata
     :chapters chapters}))


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
;       :resources {:files ["samples/test.gif" "samples/abc.css"]
;                   :folder "resource"})
;    (epub->file my-epub "my-epub.epub")
