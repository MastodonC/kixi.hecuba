(ns kixi.hecuba.data.entities.upload
  (:require [clojure.tools.logging :as log]
            [kixi.hecuba.storage.db :as db]
            [kixipipe.storage.s3 :as s3]
            [kixi.hecuba.data.entities :as entities]
            [kixipipe.ioplus :as ioplus]
            [clojure.java.io :as io]
            [kixi.hecuba.data.entities.search :as search]))

(defn- upload [store {:keys [dir filename entity_id public?] :as item} update-fn]
  (db/with-session [session (:hecuba-session store)]
   (try
     (s3/store-file (:s3 store) item)
     (log/info "item:" item)
     (update-fn session entity_id {:path (s3/s3-key-from item) :public? public?}) ;; TODO call to s3-key-from is an abstraction leak.
     (-> (search/searchable-entity-by-id entity_id session)
         (search/->elasticsearch (:search-session store)))
     (finally
       (ioplus/delete! (io/file dir filename))))))

(defn image-upload [store item]
  (upload store item entities/add-image))

(defn document-upload [store item]
  (upload store item entities/add-document))
