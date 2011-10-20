(ns clj-epub.epub
  "making epub content & metadata"
  (:use [clojure.contrib.seq :only (find-first indexed)]
        [hiccup.core :only (html)]
        [hiccup.page-helpers :only (doctype xml-declaration)]
        [clj-epub.markup]))


(defn- find-at
  "return item's index in coll"
  [item coll]
  (first (find-first #(= item (last %))
                     (indexed coll))))

(defn- ftext [name text]
  "Binding name and text"
  {:name name :text text})


(defn mimetype
  "Body of mimetype file for EPUB format"
  []
  (ftext "mimetype"
         "application/epub+zip"))


(defn meta-inf
  "container.xml for EPUB format"
  []
  (ftext "META-INF/container.xml"
         (html
          (xml-declaration "UTF-8")
          [:container {:version "1.0" :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
           [:rootfiles
            [:rootfile {:full-path "OEBPS/content.opf" :media-type "application/oebps-package+xml"}]]])))


(defn content-opf
  "Content body & metadata(author, id, ...) on EPUB format"
  [metadata-map]
;  (let [or-set   (fn [key default-value] (or (key metadata-map) default-value))
;        title    (or-set :title    "Untitled")
;        author   (or-set :author   "Nobody")
;        id       (or-set :id       nil)
;        lang     (or-set :language "en")
;        sections (or-set :sections nil)]
  (let [title    (:title metadata-map)
        author   (:author metadata-map)
        id       (:id metadata-map)
        lang     (:language metadata-map)
        sections (:sections metadata-map)]
    (ftext "OEBPS/content.opf"
           (html
            (xml-declaration "UTF-8")
            [:package {:xmlns "http://www.idpf.org/2007/opf"
                       :unique-identifier "BookID"
                       :version "2.0"}
             [:metadata {:xmlns:dc "http://purl.org/dc/elements/1.1/"
                         :xmlns:opf "http://www.idpf.org/2007/opf"}
              [:dc:title title]
              [:dc:language lang]
              [:dc:creator author]
              [:dc:identifier {:id "BookID"} id]]
             [:manifest
              [:item {:id "ncx" :href "toc.ncx" :media-type "application/x-dtbncx+xml"}]
              (for [s sections]
                [:item {:id (:ncx s) :href (:src s) :media-type "application/xhtml+xml"}])]
             [:spine {:toc "ncx"}
              (for [s sections]
                [:itemref {:idref (:ncx s)}])]]))))


(defn toc-ncx
  "Index infomation on EPUB format"
  [book-id section_titles]
  (ftext "OEBPS/toc.ncx"
         (html
          (xml-declaration "UTF-8")
          "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">"
          [:ncx {:version "2005-1" :xmlns "http://www.daisy.org/z3986/2005/ncx/"}
           [:head
            [:meta {:content book-id :name "dtb:uid"}]
            [:meta {:content "0" :name "dtb:totalPageCount"}]
            [:meta {:content "1" :name "dtb:depth"}]
            [:meta {:content "0" :name "dtb:maxPageNumber"}]]
           ;; dummy
           [:docTitle
            [:text "dummy"]]        
           [:navMap
            (for [sec section_titles]
              [:navPoint {:id (:ncx sec) :playOrder (str (inc (find-at sec section_titles)))}
               [:navLabel
                [:text (:label sec)]]
               [:content {:src (:src sec)}]])
            ]])))