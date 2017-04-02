(ns omakes.core
  (:require
    [omakes.net.download :as dl]
    )
  )

(defn -main []
  (dl/download-thread-pages 1128))

