(ns clj-epub.markup
  ""
  (:use [clojure.contrib.io :only (reader)]
        [hiccup.core])
  (:import [java.util UUID]
           [com.petebevin.markdown MarkdownProcessor]))



(def meta-tag
     {:chapter "^!!"
      :title   "^!title!"})

(defn slice-easy-text
  "簡単なマークアップで目次を切り分ける"
  [_ text]
  (for [sec (.split text (meta-tag :chapter))]
    (let [ncx  (.. sec (replaceAll "\n.*" "\n") trim)
          text (.. sec (replaceFirst "^[^\n]*\n" ""))]
      {:ncx ncx, :text text})))



(defn markdown->html
  "Markdown記法で書かれたテキストをHTMLに変換し、それを返す"
  [markdown]
  (let [mp (MarkdownProcessor.)]
    (.markdown mp markdown)))


(defn slice-html
  "ファイルを開いてePubのページごとに切り分ける(<h*>で切り分ける)"
  [title html]
  (let [prelude (re-find #"(?si)^(.*?)(?=(?:<h\d>|$))" html)
        sections (for [section (re-seq #"(?si)<h(\d)>(.*?)</h\1>(.*?)(?=(?:<h\d>|\s*$))" html)]
                   (let [[all level value text] section]
                     {:ncx value :text text}))]
;    (if prelude
;      (cons {:ncx title :text (get prelude 1)} sections)
      sections))


(defn no-slice-text
  "プレインテキストをそのまま切り分けず返す "
  [title text]
  (list {:ncx title :text text}))

; 以下、マルチメソッドで切り替える
; 修飾記法の切り替え
(def markup-types
     {:markdown markdown->html
      :default  (fn [text] text)
      :plain    (fn [text] text)})

; ePub切り分け方法を切り替える
(def slice-types
     {:markdown slice-html
      :default  slice-easy-text
      :plain    no-slice-text})


(defn normalize-text
  "テキストからePub表示に不都合なHTMLタグ、改行を取り除く"
  [text]
  (.. text
      (replaceAll "([^(<[^>]+>)\n]*)\n" "<p>$1</p>")
      (replaceAll "<br>" "<br/>")
      (replaceAll "<img([^>]*)>" "<img$1/>")))


(defn text->xhtml
  "title,textをつなげたXHTMLを返す"
  [title text]
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
       "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
       (html [:html {:xmlns "http://www.w3.org/1999/xhtml"}
              [:head
               [:title title]
               [:meta {:http-equiv "Content-Type" :content "application/xhtml+xml; charset=utf-8"}]]
              [:body (str "<p><b>" title "</b></p>"
                          (normalize-text text))]
              ])))


(defn epub-text
  "ePubのページ構成要素を作成し、返す"
  [title text]
  {:name (str "OEBPS/" title ".html")
   :text (text->xhtml title text)})


(defn easy-markup-type [title text]
  {:markup-type :easy-markup
   :title title
   :text text})
(defn plain-type [title text]
  {:markup-type :plain
   :title title
   :text text})
(defn markdown-type [title text]
  {:markup-type :markdown:
   :title title
   :text text})


(defmulti cut-by-chapter :markup-type)
(defmulti markup-text :markup-type)


;; 簡易記法、章立て切り分け
(defmethod cut-by-chapter :easy-markup
  "簡単なマークアップで目次を切り分ける"
  [easy-type]
  (let [text (:text easy-type)]
    (for [sec (.split text (:chapter meta-tag))]
      (let [ncx  (.. sec (replaceAll "\n.*" "\n") trim)
            text (.. sec (replaceFirst "^[^\n]*\n" ""))]
        {:ncx (:title easy-markup), :text text}))))


;; プレインテキスト用、章立て切り分け（切り分けない）
(defmethod cut-by-chapter :plain
  "プレインテキストをそのまま切り分けず返す "
  [plain-type]
  (list {:ncx (:title plain) :text (:text plain)}))


;; Markdown記法用、章立て切り分け
(defmethod cut-by-chapter :markdown
  "ファイルを開いてePubのページごとに切り分ける(<h*>で切り分ける)"
  [md-type]
  (let [html (:text md-type)
        prelude (re-find #"(?si)^(.*?)(?=(?:<h\d>|$))" html)
        sections (for [section (re-seq #"(?si)<h(\d)>(.*?)</h\1>(.*?)(?=(?:<h\d>|\s*$))" html)]
                   (let [[all level value text] section]
                     {:ncx value :text text}))]
      sections))


(defmethod markup-text :easy-markup
  [easy-type]
  (escape-html (:text easy-type)))

(defmethod markup-text :plain
  [plain-type]
  (escape-html (:text plain-type)))

(defmethod markup-text :markdown
  [md-type]
  (markdown->html (:text md-type)))


