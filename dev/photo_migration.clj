(ns photo-migration
  (:require [cheshire.core                    :as json]
            [clojure.java.io                  :as io]
            [clojure.string :as str]
            [clojure.tools.logging            :as log]
            [kixi.hecuba.data.entities        :as entities]
            [kixi.hecuba.data.entities.upload :as upload]
            [kixi.hecuba.storage.db           :as db]
            [kixi.hecuba.data.api             :refer (uuid)]
            [kixipipe.ioplus                  :as ioplus]
            [kixipipe.storage.s3              :as s3]
            [pantomime.mime             :refer [mime-type-of]]))

;; TODO - need to look at the path things end up at in s3.
(defn get-item-from-old-embed-bucket [s3-session key]
  (let [outfile (ioplus/mk-temp-file! "photo-migration" "")]
    (.deleteOnExit outfile)
    (log/info "looking for key " key " in " (:file-bucket s3-session))
    (let [s3-filename (last (str/split key #"/"))]
      (when (s3/item-exists? s3-session key)
        (with-open [in (s3/get-object-by-metadata s3-session {:key key})]
          (io/copy in outfile))
        {:src-name  "media-resources"
         :feed-name "images"
         :uuid      (uuid)
         :dir       (.getParent outfile)
         :filename  (.getName outfile)    ; this is the local filename
         :metadata {:filename s3-filename ; this is what we want it to be in s3
                    :content-type (mime-type-of s3-filename)}}))))

(defn photo-exists? [s3-session photo]
  (if (s3/item-exists? s3-session (:path photo))
    :photos-that-exist
    :photos-that-dont-exist))

(defn migrate-photos [{:keys [store]}]
  (let [s3      (:s3 store)
        s3-old  (assoc s3 :file-bucket "get-embed-data")]
    (db/with-session [session (:hecuba-session store)]
      (doseq [{:keys [entity_id photos]} (entities/get-all session)]
        (log/info "migrating photos for " entity_id)
        (entities/update session entity_id {:photos nil :user_id "support@mastodonc.com"})
        (let [{:keys [photos-that-exist photos-that-dont-exist]} (group-by (partial photo-exists? s3-old) photos)]
          (doseq [{:keys [path] :as p} photos-that-exist]
            (when path
              (let [old-item (-> (get-item-from-old-embed-bucket s3-old path)
                                 (assoc :entity_id entity_id))]
                (log/info "migrating path for " path)
                (upload/image-upload store  old-item))))
          (doseq [photo photos-that-dont-exist]
            (entities/add-image session entity_id photo)))))))

(comment
  ;; from 'user ns after (go)
  (require '[photo-migration :refer (migrate-photos)])
  (migrate-photos system)

  )
