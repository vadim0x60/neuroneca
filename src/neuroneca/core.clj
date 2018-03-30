(ns neuroneca.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [neuroneca.brain :as brain]
            [twitter.oauth :as tw-auth]
            [twitter.api.restful :as tw-rest]))

(defn load-text [file]
  (->    
    (slurp file)
    (str/replace #"\n-+\n" "\n")  
    (str/replace #"\n[Tt][Hh][Ee] [Ee][Nn][Dd].+" "")  
    (str/replace #"\n[Pp][Aa][Rr][Tt].+\n" "")  
    (str/replace #"\n[Cc][Hh][Aa][Pp][Tt][Ee][Rr].+\n" "\n")  
    (str/replace #"\n[Bb][Oo][Oo][Kk].+\n" "\n")   
    (str/replace #"\n\n" ". ")
    (str/replace #"\n" " ")))

(defn load-texts []
  (map load-text (.listFiles (io/as-file (io/resource "texts")))))

(defn get-creds []
  (tw-auth/make-oauth-creds
    (System/getenv "APP_CONSUMER_KEY")
    (System/getenv "APP_CONSUMER_SECRET")
    (System/getenv "USER_ACESS_TOKEN")
    (System/getenv "USER_ACESS_TOKEN_SECRET")))

(def character-limit 280)

(defn split-tweet [text]
  (as-> text $
    (map (partial apply str) (partition-all (- character-limit 6) $))
    (concat [(first $)] (map #(str "..." %) (rest $)))
    (concat (map #(str % "...") (butlast $)) [(last $)])
    (reverse $)))

(defn tweet [account text]
  (doseq [status (if (< (count text) character-limit) [text] (split-tweet text))]
      (tw-rest/statuses-update :oauth-creds account :params {:status status})))

(defn gen-tweet [model]
  (as-> model $
    (read-string $)
    (brain/continue-sentence $ [:start])
    (brain/detokenize $)
    (brain/trim-punctuation $)))

(defn -main [& args]
  (if (some (partial = "--learn") args) 
    (spit "model.clj" (brain/create-model (load-texts)) :append false)
    (try
      (let [tweet-text (gen-tweet (slurp "model.clj"))]
        (if (some (partial = "--local") args)
          (println tweet-text)
          (tweet (get-creds) tweet-text)))
      (catch java.io.FileNotFoundException e "You have to --learn before tweeting"))))