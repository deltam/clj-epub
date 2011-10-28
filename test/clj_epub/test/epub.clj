(ns clj-epub.test.epub
  (:use [clj-epub.epub] :reload-all)
  (:use [clojure.test]))

(defn- str-find
  "正規表現でなく文字列そのままを検索"
  [s body]
  (let [re (re-pattern (java.util.regex.Pattern/quote s))]
    (re-find re body)))

(deftest test-content-opf
  (let [test-metadata {:title "test-title"
                       :author "Tester"
                       :id "test-book-id"
                       :language "test-lang"}
        sections [{:label "test section"
                   :ncx "test-id"
                   :src "test-id.html"
                   :name "OEBPS/test-id.html"
                   :text "test text."}]
        test-opf (content-opf test-metadata sections)]
    (is (not (nil? test-opf)))
    (is (= "OEBPS/content.opf" (:name test-opf)))
    (is (not (nil? (str-find "test-title" (:text test-opf)))))
    (is (not (nil? (str-find "Tester" (:text test-opf)))))
    (is (not (nil? (str-find "test-book-id" (:text test-opf)))))
    (is (not (nil? (str-find "test-lang" (:text test-opf)))))
    (is (not (nil? (str-find "id=\"test-id\"" (:text test-opf)))))
    (is (not (nil? (str-find "href=\"test-id.html\"" (:text test-opf)))))
    ))

(deftest test-toc-ncx
  (let [sections [{:label "test section"
                   :ncx "test-id"
                   :src "test-id.html"
                   :name "OEBPS/test-id.html"
                   :text "test text."}]
        test-ncx (toc-ncx "test-book-id" "test-book-title" sections)]
    (is (= "OEBPS/toc.ncx" (:name test-ncx)))
    (is (not (nil? (str-find "test-book-id" (:text test-ncx)))))
    (is (not (nil? (str-find "test-book-title" (:text test-ncx)))))
    (is (not (nil? (str-find "test section" (:text test-ncx)))))    
    (is (not (nil? (str-find "test-id.html" (:text test-ncx)))))
;    (is (not (nil? (str-find #"OEBPS/test-id.html" (:text test-ncx)))))
    ))