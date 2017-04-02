(ns omakes.net.download
  (:require
    [clojure.zip :as zip]
    [clj-http.client :as client]
    [hickory.core :as h]
    [hickory.zip :as z]
    [hickory.select :as s]))

(defonce page-url-prefix "https://forums.sufficientvelocity.com/threads/we-stand-in-awe-bleach-quest.32342/page-")

(defn url-for-page [n]
  (str page-url-prefix n))

(defn author-from-message [message]
  (get-in message [:attrs :data-author])
  )

(defn all-nodes [z]
  (take-while (complement zip/end?) ;take until the :end
              (iterate zip/next z)))

(defn leaf-nodes [z]
  (filter (complement zip/branch?) ;filter only non-branch nodes
          (take-while (complement zip/end?) ;take until the :end
                      (iterate zip/next z))))

(defn content-from-message [message]
  (let [message-content
        (first (s/select (s/class :messageText) message))
        msg-zip (z/hickory-zip message-content)
        leaves (map zip/node (leaf-nodes msg-zip))]
    (clojure.string/join " " leaves)))

(defn word-count-for-message [message]
  (->
    (content-from-message message)
    (clojure.string/split  #"\s")
    count))

(defn download-thread-page [page]
  (let [page-html (:body (client/get (url-for-page 1128)))
        parsed-html (-> page-html h/parse h/as-hickory)]
    parsed-html))

(defn download-thread-pages [first-page]
  (let [page-html (:body (client/get (url-for-page 1128)))
        parsed-html (-> page-html h/parse h/as-hickory)
        messages (s/select (s/class :message) parsed-html)
        last-page (->>
                   (s/select (s/class :gt999) parsed-html)
                   (map (fn [v] (-> v :content first)))
                   (map read-string) ; this is technically code execution vulnerability lolz
                   (apply max))]
    (println (word-count-for-message (nth messages 1)))

    )

  )
