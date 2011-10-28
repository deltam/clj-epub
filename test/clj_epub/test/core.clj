(ns clj-epub.test.core
  (:use [clj-epub.core] :reload-all)
  (:use [clojure.test])
  (:import [com.adobe.epubcheck.api EpubCheck]))

;user=> (epub->file (text->epub {:inputs ["README.md"] :title "test" :markup :plain})) "test.epub"))
;user=> (epub->file (text->epub {:inputs ["README.md"] :title "test" :markup :plain}) "test.epub")
;user=> (.validate (EpubCheck. (java.io.File. "test2.epub")))
;user=> (.validate (EpubCheck. (java.io.File. "test.epub")))true


(deftest test-textfile->epub
  (let [epub (textfile->epub {:inputs ["test-resources/hello.txt"] :title "hello(plain text)" :author "Tester" :markup :plain :language :en :id "test-book-id"})]
    (is (not (nil? epub)))
    (is (not (nil? (:mimetype epub))))
    (is (not (nil? (:meta-inf epub))))
    (is (not (nil? (:content-opf epub))))
    (is (not (nil? (:toc-ncx epub))))
    (is (not (nil? (:sections epub)))))
    )

(deftest test-epub->file
  (let [epub (textfile->epub {:inputs ["test-resources/hello.txt"] :title "hello(plain text)" :author "Tester" :markup :plain :language "en" :id "test-book-id"})
        epub-file (epub->file epub "test.epub")]
    (is (not (nil? epub-file)))
    (is (true? (.validate (EpubCheck. epub-file))))
    (.delete epub-file))
  (let [epub (textfile->epub {:inputs ["test-resources/hello.txt"] :markup :plain})
        epub-file (epub->file epub "test.epub")]
    (is (not (nil? epub-file)))
    (is (true? (.validate (EpubCheck. epub-file))))
    (.delete epub-file))
  (let [epub (textfile->epub {:inputs ["test-resources/hello.md"] :title "hello(markdown)" :author "Tester" :markup :markdown :language "en" :id "test-book-id"})
        epub-file (epub->file epub "test.epub")]
    (is (not (nil? epub-file)))
    (is (true? (.validate (EpubCheck. epub-file))))
    (.delete epub-file))
  )

(defn- str-find
  "正規表現でなく文字列そのままを検索"
  [s body]
  (let [re (re-pattern (java.util.regex.Pattern/quote s))]
    (re-find re body)))

(deftest test-to-sections
  ; by plain text
  (let [s1 (to-sections [{:chapter "test1" :text "hello" :type :plain}])
        s2 (first s1)]
    (is (= 1 (count s1)))
    (is (= "test1" (:label s2))))
  ; by html
  (let [s1 (to-sections [{:chapter "test2" :html "<b>hello<\b>"}])
        s2 (first s1)]
    (is (= 1 (count s1)))
    (is (= "test2" (:label s2)))
    (is (str-find "<b>hello<\b>" (:text s2)))
    (is (not (str-find "<b>goodby<\b>" (:text s2)))))
  ; by file, markdown format
  (let [s1 (to-sections [{:chapter "test3" :file "test-resources/hello.md" :type :markdown}])]
    (is (= 3 (count s1)))
    (is (= "chapter 1" (:label (nth s1 0))))
    (is (= "chap 2"    (:label (nth s1 1))))
    (is (= "end"       (:label (nth s1 2)))))
  (let [s1 (to-sections [{:chapter "test1" :text "hello" :type :plain}
                         {:chapter "test2" :html "<b>hello<\b>"}
                         {:chapter "test3" :file "test-resources/hello.md" :type :markdown}])]
    (is (= 5 (count s1)))
    (is (= "test1"     (:label (nth s1 0))))
    (is (= "test2"     (:label (nth s1 1))))
    (is (= "chapter 1" (:label (nth s1 2))))
    (is (= "chap 2"    (:label (nth s1 3))))
    (is (= "end"       (:label (nth s1 4))))
    )
  )

(deftest test-make-epub
  (let [epub (make-epub {:title "test title"
                         :author "Tester"
                         :book-id :random
                         :language "en"
                         :sections [{:chapter "test1"
                                     :text "first chapter"}
                                    {:chapter "html chapter"
                                     :html "<pre>test section</pre>"}
                                    {:chapter "file"
                                     :file "test-resources/hello.md"
                                     :type :plain}
                                    ]})
        epub-file (epub->file epub "test.epub")]
    (is (not (nil? epub)))
    (is (not (nil? (:mimetype epub))))
    (is (not (nil? (:meta-inf epub))))
    (is (not (nil? (:content-opf epub))))
    (is (not (nil? (:toc-ncx epub))))
    (is (not (nil? (:sections epub))))
    (is (not (nil? epub-file)))
    (is (true? (.validate (EpubCheck. epub-file))))
;    (.delete epub-file)
    )
  )
