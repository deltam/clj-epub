(ns clj-epub.epub
  "make epub metadata"
  (:use [clojure.contrib.io :only (reader)]
        [hiccup.core]
        [clj-epub.markup])
  (:import [java.util UUID]
           [com.petebevin.markdown MarkdownProcessor]))


(defn- generate-uuid
  "generate uuid for ePub dc:identifier(BookID)"
  []
  (str (UUID/randomUUID)))


(defn- ftext [name text]
  "binding name and text"
  {:name name :text text})


(defn mimetype
  "body of mimetype file for ePub format"
  []
  (ftext "mimetype"
         "application/epub+zip"))


(defn meta-inf
  "container.xml for ePub format"
  []
  (ftext "META-INF/container.xml"
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              (html
               [:container {:version "1.0" :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
                [:rootfiles
                 [:rootfile {:full-path "OEBPS/content.opf" :media-type "application/oebps-package+xml"}]]]))))


(defn content-opf
  "content body & metadata(author, id, ...) on ePub format"
  [title author id sections]
  (ftext "OEBPS/content.opf"
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              (html
               [:package {:xmlns "http://www.idpf.org/2007/opf"
                          :unique-identifier "BookID"
                          :version "2.0"}
                [:metadata {:xmlns:dc "http://purl.org/dc/elements/1.1/"
                            :xmlns:opf "http://www.idpf.org/2007/opf"}
                 [:dc:title title]
                 [:dc:language "ja"]
                 [:dc:creator author]
                 [:dc:identifier {:id "BookID"} id]]
                [:manifest
                 [:item {:id "ncx" :href "toc.ncx" :media-type "application/x-dtbncx+xml"}]
                 (for [s sections]
                   [:item {:id s :href (str s ".html") :media-type "application/xhtml+xml"}])]
                [:spine {:toc "ncx"}
                 (for [s sections]
                   [:itemref {:idref s}])]]))))


(defn toc-ncx
  "index infomation on ePub format"
  [id section_titles]
  (ftext "OEBPS/toc.ncx"
         (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
              "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">"
              (html
               [:ncx {:version "2005-1" :xmlns "http://www.daisy.org/z3986/2005/ncx/"}
                [:head
                 [:meta {:content id :name "dtb:uid"}]
                 [:meta {:content "0" :name "dtb:totalPageCount"}]
                 [:meta {:content "1" :name "dtb:depth"}]
                 [:meta {:content "0" :name "dtb:maxPageNumber"}]]
                [:navMap
                 (for [sec section_titles]
                   [:navPoint {:id sec :playOrder (str (inc (count (take-while #(not (= sec %)) section_titles))))}; todo
                    [:navLabel
                     [:text sec]]
                    [:content {:src (str sec ".html")}]])
                 ]]))))



(defn text->epub
  "generate ePub file. args are epub filename, epub title of metadata, includes text files."
  [{output :output input-files :input title :title author :author marktype :markup}]
  (let [id       (generate-uuid)
        mfiles   (map #(ftext % ((markup-types marktype) (slurp %))) input-files) ; todo refactoring
        ptexts   (flatten (map #((slice-types marktype) (:name %) (:text %)) mfiles)) ; ePub page cut by files
        sections (map #(get % :ncx) ptexts)]
    {:mimetype    (mimetype)
     :meta-inf    (meta-inf)
     :content-opf (content-opf title (or author "Nobody") id sections)
     :toc-ncx     (toc-ncx id sections)
     :html        (for [s ptexts]
                    (epub-text (s :ncx) (s :text)))}))