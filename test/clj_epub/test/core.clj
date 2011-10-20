(ns clj-epub.test.core
  (:use [clj-epub.core] :reload-all)
  (:use [clojure.test])
  (:import [com.adobe.epubcheck.api EpubCheck]))

;user=> (epub->file (text->epub {:inputs ["README.md"] :title "test" :markup :plain})) "test.epub"))
;user=> (epub->file (text->epub {:inputs ["README.md"] :title "test" :markup :plain}) "test.epub")
;user=> (.validate (EpubCheck. (java.io.File. "test2.epub")))
;user=> (.validate (EpubCheck. (java.io.File. "test.epub")))true


(deftest test-text->epub
  (let [epub (text->epub {:inputs ["test-resources/hello.txt"] :title "hello(plain text)" :author "Tester" :markup :plain :language :en :id "test-book-id"})]
    (is (not (nil? epub)))
    (is (not (nil? (:mimetype epub))))
    (is (not (nil? (:meta-inf epub))))
    (is (not (nil? (:content-opf epub))))
    (is (not (nil? (:html epub))))))

(deftest test-epub->file
  (let [epub (text->epub {:inputs ["test-resources/hello.txt"] :title "hello(plain text)" :author "Tester" :markup :plain :language "en" :id "test-book-id"})
        epub-file (epub->file epub "test.epub")]
    (is (not (nil? epub-file)))
    (is (true? (.validate (EpubCheck. epub-file))))
    (.delete epub-file))
  (let [epub (text->epub {:inputs ["test-resources/hello.txt"] :markup :plain})
        epub-file (epub->file epub "test.epub")]
    (is (not (nil? epub-file)))
    (is (true? (.validate (EpubCheck. epub-file))))
    (.delete epub-file))
  )
