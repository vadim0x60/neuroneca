(defproject neuroneca "1.0.1."
  :description "I do Stoicism"
  :url "https://twitter.com/Neuroneca"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clojure-opennlp "0.3.3"]
                 [twitter-api "1.8.0"]]
  :main ^:skip-aot neuroneca.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
