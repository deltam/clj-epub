(defproject clj-epub "0.1.1"
  :description "Library for generate EPUB, on Clojure"
  :url "http://github.com/deltam/clj-epub"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [hiccup "0.3.0"]
                 [org.markdownj/markdownj "0.3.0-1.0.2b4"]]
  :dev-dependencies [[org.clojars.deltam/epubcheck "1.2"]
                     [swank-clojure "1.2.1"]
;                     [lein-clojars "0.7.0"]
                     ]
  :test-resources "test-resources"
  :repositories {"markdownj" "http://scala-tools.org/repo-releases"})
