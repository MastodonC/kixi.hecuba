(ns kixi.hecuba.data.entities.upload
  (:require [clojure.tools.logging :as log]
            [kixi.hecuba.storage.db :as db]
            [kixipipe.storage.s3 :as s3]
            [kixi.hecuba.data.entities :as entities]
            [kixipipe.ioplus :as ioplus]
            [clojure.java.io :as io]))

(defn- upload [store {:keys [dir filename entity_id] :as item} update-fn]
  (db/with-session [session (:hecuba-session store)]
   (try
     (let [item  (-> item
                     (assoc :uuid (str entity_id "/" (-> item :metadata :filename))))]
       (s3/store-file (:s3 store) item)
       (update-fn session entity_id (s3/s3-key-from item)))
     (finally
       (ioplus/delete! (io/file dir filename))))))

(defn image-upload [store item]
  (upload store item entities/add-image))

(defn document-upload [store item]
  (upload store item entities/add-document))
