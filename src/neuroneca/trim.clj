; This is in a separate file because my editor can't handle \"
; Please don't send me emails about how you use vim and have no problems
; I am really happy for you

(defn trim-punctuation 
  "Remove the period at the end of the sentence and garbage after it if any"
  ([sentence full-sentence]
      (let [tail (butlast sentence) char (last sentence)]
        (case char
          nil full-sentence
          \. tail                                    ; No one ends a one-sentence tweet with a period.
          (\! \? \‽) sentence                        ; Everyone forgets about the interrobang =( Not me
          \" (if (-> (partial = \") (filter tail) (count) (rem 2) (= 1)) sentence (trim-punctuation tail full-sentence))
          (trim-punctuation tail full-sentence))))
  ([full-sentence] (apply str (trim-punctuation full-sentence full-sentence))))