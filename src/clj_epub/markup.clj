(ns clj-epub.markup
  "make EPUB text from some markuped text"
  (:use [hiccup.core :only (html escape-html)]
        [hiccup.page-helpers :only (doctype xml-declaration)])
  (:import [com.petebevin.markdown MarkdownProcessor]))



(defrecord Chapter [title text markup])

;; 章立ての切り分け
(defmulti cut-by-chapter :markup)
;; 各記法による修飾
(defmulti markup-text :markup)

(defmethod cut-by-chapter :default
  [{title :title text :text markup :markup}]
  (Chapter. title, text, markup))

(defmethod markup-text :default
  [{title :title text :text markup :markup}]
  (Chapter. title, text, markup))


(defn normalize-text
  "テキストからEPUB表示に不都合なHTMLタグ、改行を取り除く"
  [text]
  (.. text
      (replaceAll "<br>" "<br/>")
      (replaceAll "<img([^>]*)>" "<img$1/>")))


(defn text->xhtml
  "title,textをつなげたXHTMLを返す"
  [{title :title text :text}]
  (html
   (xml-declaration "UTF-8")
   (doctype :xhtml-transitional)
   [:html {:xmlns "http://www.w3.org/1999/xhtml"}
    [:head
     [:title title]
     [:meta {:http-equiv "Content-Type" :content "application/xhtml+xml; charset=utf-8"}]]
    [:body (normalize-text text)]]))


(defn epub-text
  "EPUBのページ構成要素を作成し、返す"
  [id chapter]
  {:label (:title chapter)
   :ncx  id
   :src  (str id ".html")
   :name (str "OEBPS/" id ".html")
   :text (text->xhtml chapter)})


(defn files->epub-texts
  "ファイルの内容をEPUB用HTMLに変換して返す"
  [markup-type filenames]
  (let [map-flatten (fn [f coll] (flatten (map f coll)))
        files    (map #(Chapter. %, (slurp %), markup-type) filenames)
        chapters (map-flatten cut-by-chapter files)
        markups  (map-flatten markup-text    chapters)]
    (map-indexed (fn [index chapter] (epub-text (str "chapter-" index) chapter))
                   markups)))



;; EPUB簡易記法

; 簡易記法タグ
(def meta-tag
     {:chapter "[\\^\n]!!" ; !!
      :title   "!title!"})

; 簡単なマークアップで目次を切り分ける
(defmethod cut-by-chapter :easy-markup
  [{title :title text :text}]
  (for [section (re-seq #"(?si)!!\s*(.*?)\n(.*?)(?=(?:!!|\s*$))" text)]
    (let [[all value body] section]
      (Chapter. value, body, :easy-markup))))

(defmethod markup-text :easy-markup
  [{title :title text :text}]
  (let [html (str "<b>" title "</b>"
                  (. text replaceAll "([^(<[^>]+>)\n]*)\n*" "<p>$1</p>"))]
    (Chapter. title, html, nil)))



;; プレインテキスト用

(defmethod markup-text :plain
  [{title :title text :text}]
  (Chapter. title, (str "<pre>" (escape-html text) "</pre>"), nil))



;; Markdown記法

(defn markdown->html
  "Markdown記法で書かれたテキストをHTMLに変換し、それを返す"
  [markdown]
  (let [mp (MarkdownProcessor.)]
    (.markdown mp markdown)))

; HTMLに変換してから章ごとに切り分け
(defmethod cut-by-chapter :markdown
  [{title :title text :text}]
  (let [html (markdown->html text)]
    (for [section (re-seq #"(?si)<h(\d)>(.*?)</h\1>(.*?)(?=(?:<h\d>|\s*$))" html)]
      (let [[all level value body] section]
        (Chapter. value, all, :markdown)))))