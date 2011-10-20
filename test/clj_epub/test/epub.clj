(ns clj-epub.test.epub
  (:use [clj-epub.epub] :reload-all)
  (:use [clojure.test]))


(deftest test-content-opf
  (let [test-metadata {:title "test-title"
                       :author "Tester"
                       :id "test-book-id"
                       :language "test-lang"
                       :sectons nil}
        test-opf (content-opf test-metadata)]
    (is (not (nil? test-opf)))
    (is (= "OEBPS/content.opf" (:name test-opf)))
    (is (not (nil? (re-find #"test-title" (:text test-opf)))))
    (is (not (nil? (re-find #"Tester" (:text test-opf)))))
    (is (not (nil? (re-find #"test-book-id" (:text test-opf)))))
    (is (not (nil? (re-find #"test-lang" (:text test-opf)))))
    ))

(deftest test-toc-ncx
  (let [sections [{:label "test section"
                   :ncx "test-id"
                   :src "test-id.html"
                   :name "OEBPS/test-id.html"
                   :text "test text."}]
        test-ncx (toc-ncx "test-book-id" sections)]
    (is (= "OEBPS/toc.ncx" (:name test-ncx)))
    (is (not (nil? (re-find #"test-book-id" (:text test-ncx)))))
    (is (not (nil? (re-find #"test section" (:text test-ncx)))))    
    (is (not (nil? (re-find #"test-id.html" (:text test-ncx)))))
;    (is (not (nil? (re-find #"OEBPS/test-id.html" (:text test-ncx)))))
    ))