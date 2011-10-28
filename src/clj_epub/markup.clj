(ns clj-epub.markup
  "make EPUB text from some markuped text"
  (:use [hiccup.core :only (html escape-html)]
        [hiccup.page-helpers :only (doctype xml-declaration)])
  (:import [com.petebevin.markdown MarkdownProcessor]))


;; Chapter text
(defrecord Chapter [^String title, ^String text, ^clojure.lang.Keyword markup])
;; EPUB Section
(defrecord Section [label ncx src name text])


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

(defn make-section
  "Return EPUB Section data."
  ([id title xhtml]
     (Section. title
               id
               (str id ".html")
               (str "OEBPS/" id ".html")
               xhtml))
  ([id ^Chapter chapter]
     (make-section id (:title chapter) (text->xhtml chapter))))


(defn- map-flatten [f coll] (flatten (map f coll)))

(defn text->sections
  "テキストをセクションのベクタに変える。章を切り分けるマークアップが会った場合、それに応じてセクションも増える"
  ([name text markup-type]
     (let [text      (vector (Chapter. name text markup-type))
           chapters  (map-flatten cut-by-chapter text)
           markupped (map-flatten markup-text    chapters)]
       (map-indexed (fn [index chapter] (make-section (str "chapter-" index) chapter))
                    markupped)))
  ([^Chapter chapter]
     (text->sections (:title chapter) (:text chapter) (:markup chapter))))

(defn files->sections
  "ファイルの内容をEPUB用HTMLに変換して返す"
  [filenames markup-type]
  (map-flatten #(text->sections % (slurp %) markup-type)
               filenames))

;;TODOあとでセクションを追加する場合、IDが連番にならない。ファイル名も被るから上書きされてしまう。
;; 追加でセクションを生成し、合成する場合はどうするか？
(defn concat-sections
  ""
  [sections-a sections-b]
  (map-indexed (fn [index section] (make-section (str "chapter-" index)
                                                 (:label section)
                                                 (:text  section)))
                 (concat sections-a sections-b)))


;; EPUB easy markup

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



;; Plain Text

(defmethod markup-text :plain
  [{title :title text :text}]
  (Chapter. title, (str "<pre>" (escape-html text) "</pre>"), nil))



;; Markdown format

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