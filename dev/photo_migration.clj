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

;; temp hack - we should put this in the config as a separate
;; component, but this is a throwaway migration so fudge it for now.
;; you need to fill this in.
(def old-embed-s3-session (s3/mk-session {:access-key ""
                                          :secret-key ""
                                          :file-bucket "get-embed-data"
                                          :download-dir "/tmp"}))

(defn path-from [p]
  (-> p
      (json/parse-string keyword)
      :path))

;; TODO - need to look at the path things end up at in s3.
(defn get-item-from-old-embed-bucket [key]
  (let [outfile (ioplus/mk-temp-file! "photo-migration" "")]
    (.deleteOnExit outfile)
    (when (s3/item-exists? old-embed-s3-session key)
      (with-open [in (s3/get-object-by-metadata old-embed-s3-session {:key key})]
        (io/copy in outfile))
      {:src-name  "uploads"
       :feed-name "photo-migrations"
       :uuid      (uuid)
       :dir       (.getParent outfile)
       :filename  (.getName outfile)})))

(defn migrate-photos [{:keys [store]}]
  (db/with-session [session (:hecuba-session store)]
    (doseq [{:keys [id photos]} (entities/get-all session)]
      (log/info "migrating photos for " id)
      (entities/update session id {:photos []})
      (doseq [item (keep get-item-from-old-embed-bucket (map path-from photos))]
        (log/info "migrating path for " item)
        (upload/image-upload store  (assoc item :entity_id id))))))

(comment
  ;; from 'user ns after (go)
  (require '[photo-migration :refer (migrate-photos)])
  (migrate-photos system)

  )
