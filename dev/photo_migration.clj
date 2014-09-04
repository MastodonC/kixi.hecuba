(ns photo-migration
  (:require [kixi.hecuba.data.entities        :as entities]
            [kixi.hecuba.storage.db           :as db]
            [kixipipe.storage.s3              :as s3]
            [cheshire.core                    :as json]
            [clojure.java.io                  :as io]
            [kixipipe.ioplus                  :as ioplus]
            [kixi.hecuba.data.entities.upload :as upload]
            [clojure.tools.logging            :as log]
            [kixi.hecuba.webutil              :refer (uuid)]))


(defn path-from [p]
  (try
    (when (instance? String p) (-> p
                                   (json/parse-string keyword)
                                   :path))
    (catch Throwable t
      (log/error (str "parsing " p)))))

;; TODO - need to look at the path things end up at in s3.
(defn get-item-from-old-embed-bucket [s3-session key]
  (let [outfile (ioplus/mk-temp-file! "photo-migration" "")]
    (.deleteOnExit outfile)
    (println key)
    (when (s3/item-exists? s3-session key)
      (with-open [in (s3/get-object-by-metadata s3-session {:key key})]
        (io/copy in outfile))
      {:src-name  "uploads"
       :feed-name "images"
       :uuid      (uuid)
       :dir       (.getParent outfile)
       :filename  (.getName outfile)})))

(defn migrate-photos [{:keys [store]}]
  (let [s3 (:s3 store)
        get-old (partial get-item-from-old-embed-bucket (assoc s3 :file-bucket "get-embed-data"))
        ]
    (db/with-session [session (:hecuba-session store)]
      (doseq [{:keys [id photos]} (entities/get-all session)]
        (log/info "migrating photos for " id)
        (if-let [paths (not-empty (keep path-from photos))]
          (doseq [item (keep get-old paths)]
            (log/info "migrating path for " item)
            (upload/image-upload store  (assoc item :entity_id id))))))))

(comment
  ;; from 'user ns after (go)
  (require '[photo-migration :refer (migrate-photos)])
  (migrate-photos system)

  )
