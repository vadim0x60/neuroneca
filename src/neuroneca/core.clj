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

(defn split-tweet [text]
  (as-> text $
    (map (partial apply str) (partition-all 134 $))
    (concat [(first $)] (map #(str "..." %) (rest $)))
    (concat (map #(str % "...") (butlast $)) [(last $)])
    (reverse $)))

(defn tweet [account text]
  (doseq [status (if (< (count text) 140) [text] (split-tweet text))]
      (tw-rest/statuses-update :oauth-creds account :params {:status status})))

(defn -main [& args]
  (if (some (partial = "--learn") args) 
    (spit "model.clj" (brain/create-model (load-texts)) :append false)
    (try
      (as-> (slurp "model.clj") $
        (read-string $)
        (brain/continue-sentence $ [:start])
        (brain/detokenize $)
        (brain/trim-punctuation $)
        (tweet (get-creds) $))
      (catch java.io.FileNotFoundException e "You have to --learn before tweeting"))))