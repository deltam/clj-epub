# clj-epub

Library for generating EPUB on Clojure

Clojureで書かれたEPUBを生成するためのライブラリです。


EPUB created by this tool, checked open it by these EPUB readers.

このライブラリで作成したepubは、これらのEPUBリーダーで開けることをチェックしています。

  iBooks

  Stanza http://www.lexcycle.com/


## Install

Add to your `project.clj`

    :dependencies [...
                   [clj-epub "0.1.0"]
                   ...]


## Usage

Convert Markdown text to EPUB on repl.

    user> (use 'clj-epub.core)
    nil
    user> (def epub (text->epub {:inputs ["README.md"] :title "README.md" :author "deltam" :markup :markdown}))
    #'user/epub
    user> (epub->file epub "test.epub")


## License

Copyright (c) 2011 Masaru MISUMI(deltam@gmail.com).

Licensed under the MIT License (http://www.opensource.org/licenses/mit-license.php)
