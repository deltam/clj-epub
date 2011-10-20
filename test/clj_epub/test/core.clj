(ns clj-epub.test.core
  (:use [clj-epub.core] :reload-all)
  (:use [clojure.test])
  (:import [com.adobe.epubcheck.api EpubCheck]))

;user=> (epub->file (text->epub {:inputs ["README.md"] :title "test" :markup :plain})) "test.epub"))
;user=> (epub->file (text->epub {:inputs ["README.md"] :title "test" :markup :plain}) "test.epub")
;user=> (.validate (EpubCheck. (java.io.File. "test2.epub")))
;user=> (.validate (EpubCheck. (java.io.File. "test.epub")))true

(deftest replace-me ;; FIXME: write
  (is false))

