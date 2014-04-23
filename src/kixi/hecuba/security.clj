(ns kixi.hecuba.security
  (:require
   [kixi.hecuba.protocols :refer (item items upsert!)])
  (:import
   java.security.SecureRandom
   javax.crypto.SecretKeyFactory
   javax.crypto.spec.PBEKeySpec
   (javax.xml.bind DatatypeConverter)))


;; Someone to review this block of code (remove this comment when reviewed)
;; START_BLOCK

(defn pbkdf2
  "Get a hash for the given string and optional salt. From
http://adambard.com/blog/3-wrong-ways-to-store-a-password/"
  ([password salt]
     (assert password "No password!")
     (assert salt "No salt!")
     (let [k (PBEKeySpec. (.toCharArray password) (.getBytes salt) 1000 192)
           f (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA1")]
       (format "%x"
               (java.math.BigInteger. (.getEncoded (.generateSecret f k)))))))

(defn make-salt
  "Make a base64 string representing a salt. Pass in a SecureRandom."
  [rng]
  (let [ba (byte-array 32)]
    (.nextBytes rng ba)
    (javax.xml.bind.DatatypeConverter/printBase64Binary ba)))

(defn create-hash [rng password]
  (let [salt (make-salt rng)
        hash (pbkdf2 password salt)]
    {:salt salt
     :hash hash}))

(defn verify-password [password {:keys [hash salt]}]
  (= (pbkdf2 password salt) hash))

(defn get-username-password
  [{headers :headers :as request} querier]
  (when-let [auth (get headers "authorization")]
    (when-let [basic-creds (second (re-matches #"\QBasic\E\s+(.*)" auth))]
      (->> (String. (DatatypeConverter/parseBase64Binary basic-creds) "UTF-8")
           (re-matches #"(.*):(.*)")
           rest))))

;; END_BLOCK

#_(create-hash (SecureRandom.) "secret")
#_(verify-password "secret" {:hash "32767a445d5a4dbadb153d2654b1505a7ac0f20947a45d38", :salt "usz0fFalxZcwdAVnRFrE9ZC+k1L4pz8u/MuS1w0FRjQ=", :username "bob"})

(defn authorized? [username password querier]
  (when-let [stored-user (item querier :user username)]
    (verify-password password stored-user)))

(defn authorized-with-basic-auth?
  [req querier]
  (when-let [[username password] (get-username-password req querier)]
           (authorized? username password querier)))

(def session-expiry-in-secs (* 6 60 60 1000))

(defn some-time-ago [secs]
  (java.util.Date. (- (.getTime (java.util.Date.)) (* secs 1000))))

(defn authorized-with-cookie? [{{{id :value} "session"} :cookies} querier]
  (when id
    (when-let [session (items querier :user-session [[= :id id] [> :timestamp (some-time-ago session-expiry-in-secs)]])]
      session)))

(defn create-session-cookie [username {:keys [commander]}]
  (let [id (str (java.util.UUID/randomUUID))]
    (upsert! commander :user-session {:id id :user username :timestamp (java.util.Date.)})
    ;; Adding 20 for good measure and testing
    {"session" {:value id :max-age (+ 20 session-expiry-in-secs)}}))

(defn make-rng []
  (SecureRandom.))
