(ns clj-epub.test.markup
  (:use [clj-epub.markup] :reload-all)
  (:use [clojure.test]))


;(deftest test-slice-easy-text
;  (is (= '({:ncx "test" :text "test body"})
;         (slice-easy-text "" "!!test\ntest body")))
;  (is (= '({:ncx "test1" :text "body1\nbody2"} {:ncx "test2" :text "body3\nbody4"})
;         (slice-easy-text "" "!!test1\nbody1\nbody2\n!!test2\nbody3\nbody4"))))

(deftest test-normalize-text
  (is (= "<br/>" (normalize-text "<br>")))
  (is (= "<img src=\"test\"/>" (normalize-text "<img src=\"test\">"))))

(deftest test-markdown->html ; todo write more
  (is (= "<h1>test</h1>\n" (markdown->html "# test\n"))))

(deftest test-text->xhtml
  (is (= (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
;              "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
              "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
              "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
              "<head>"
              "<title>title</title>"
              "<meta content=\"application/xhtml+xml; charset=utf-8\" http-equiv=\"Content-Type\" />"
              "</head>"
              "<body>body</body></html>")
         (text->xhtml {:title "title" :text "body"}))))

(deftest test-epub-text
  (is false))

(deftest test-files->epub-texts
  (is false))


;; todo test markup type