(ns neuroneca.brain
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [clojure.java.io :as io])
  (:require [opennlp.nlp :as nlp]))

; Tunables
(def stop-bias 5) ; Bias towards ending a sentence. Makes sentences shorter

(def ngram-n 5) ; N-gram size. 
; Big N -> take sentences from books. 
; Small N -> generate completely novel sentences

(def tokenize (comp 
  #(concat [:start] % [:stop])
  (nlp/make-tokenizer (io/resource "en-token.bin")) ))

(def detokenize (comp (nlp/make-detokenizer (io/resource "english-detokenizer.xml")) rest butlast))

(def get-sentences (nlp/make-sentence-detector (io/resource "en-sent.bin")))

; This function introduces artificial biases towards certain tokens
; By slanting the bot towards :stop, we make him more concise.
(defn ngram-bias [ngram]
  (if (= (last ngram) :stop) stop-bias 1))

(defn get-submodel-for-ngram [model ngram]
  (if (empty? ngram)
    model
    (get-submodel-for-ngram (get model (first ngram)) (rest ngram))))

(defn inc-ngram-count [model ngram weight]
  (->
    (if (not (empty? ngram))
      (let [submodel (get model (first ngram))]
        (assoc model (first ngram) ((fnil inc-ngram-count {}) submodel (rest ngram) weight)))
      model)
    (update :count (fnil (partial + weight) 0))))

(defn get-random-submodel [model]
  (let [min (rand-int (:count model))]
    (loop [value 0 variants (dissoc model :count)]
      (let [variant (first variants)
            others (rest variants)
            num (+ value (:count (last variant)))]
        (if (< num min) 
          (recur num others)
          variant))))) 

(defn get-random-ngram [model]
  (if (empty? (dissoc model :count)) 
    '()
    (let [[token variants] (get-random-submodel model)]
      (if (nil? token) 
        '()
        (conj (get-random-ngram variants) token)))))

(defn count-sentence-ngrams [model sentence]
  (->> 
    (tokenize sentence)
    (partition ngram-n 1)
    (reduce (fn [model ngram] (inc-ngram-count model ngram (ngram-bias ngram))) model)))

(defn create-model [texts]
  (reduce count-sentence-ngrams {} (apply concat (map get-sentences texts))))

(defn continue-sentence [model oldsentence]
  (->> 
    (take-last (dec ngram-n) oldsentence) 
    (iterate rest  ) 
    (keep (partial get-submodel-for-ngram model)  ) 
    (first  )
    (get-random-ngram  )
    (concat oldsentence  )
    ((fn [sentence] (if (= :stop (last sentence)) sentence (continue-sentence model sentence)))  )))

(load "trim") ;(defn trim-punctuation)