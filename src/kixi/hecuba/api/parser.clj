(ns kixi.hecuba.api.parser
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]))

(defn attribute-type [attr]
  (if (keyword? attr)
    :attribute
    (:type attr)))

(defn explode-nested-item [association item-string]
  "Explodes a nested item, that is represented in the object coming from
  the datastore as a json encoded string. Returns a list of vectors of
  two elements, the first being the attribute key, and the second the value.
  The key is expanded to <nested item name>_<attribute name>"
  (let [item (json/decode item-string)
        association-name   (:name   association)
        association-schema (:schema association)]
    (map
     (fn [attr]
       [(str (name association-name) "_" (name attr)) (item (name attr))])
     association-schema)))

(defn explode-associated-items [association items]
  "Explodes the elements of a (one to many) association, that is represented
  in the object coming from the datastore as a list of json encoded strings.
  Returns a list of vectors of two elements, the first being the attribute key,
  and the second the value.
  The keys are expanded like <association name>_<associated item index>_<attribute name>"
  (let [association-name   (name (:name association))
        association-schema (:schema association)]
    (apply concat
    (map-indexed
      (fn [index item-string]
         (let [item-name         (str association-name "_" index)
               named-association (assoc association :name item-name)]
           (if (empty? association-schema)
             [item-name item-string]
             (explode-nested-item named-association item-string))))
      items))))

(defn explode-and-sort-by-schema [item schema]
  "Take a (profile) item from the datastore and converts into a list
  of pairs (represented as a vector) where the first element is the
  exploded key for the attribute and the second is the value"
  (let [exploded-item
         (mapcat
           (fn [attr]
             (let [t (attribute-type attr)]
               (case t
                 :attribute          (list [(name attr) (item attr)])
                 :nested-item        (explode-nested-item attr (item (:name attr)))
                 :associated-items   (explode-associated-items attr (item (:name attr))))))
           schema)]
    exploded-item))

(defn extract-attribute [attr-key input]
  "Extracts a hash-map containing a single key and its value from the input.
  The key is expected to be a keyword, while input is supposed to be a hash-map
  with strings as keys"
  (let [attr-name (name attr-key)
        attr-value (input attr-name)]
    (if (seq attr-value)
      {attr-key attr-value}
      nil)))

(defn extract-nested-item [attr input]
  "Extracts a nested item from input, returning a hashmap with a single pair,
  where the key is the nested item association name, and the value is a json
  string representing all the attributes of the nested item.
  attr is expected to be a hash-map with at least :name and :schema keys,
  while input is expected to be a hash-map representing the profile, with strings as keys"
  (let [association-name   (:name   attr)
        association-schema (:schema attr)
        nested-item (reduce
                      (fn [nested-item nested-attr]
                        (let [nested-attr-name (name nested-attr)
                              exploded-attr-name (str (name association-name) "_" nested-attr-name)
                              exploded-attr-value (input exploded-attr-name)]
                          (if (seq exploded-attr-value)
                            (conj nested-item { nested-attr exploded-attr-value})
                            nested-item)))
                      {}
                      association-schema)]
    (when (seq nested-item)
      {association-name nested-item})))

(defn extract-associated-item [association-name association-schema input index]
  "Extracts the item belonging to a 'has many' association from input, at position index."
  (reduce
   (fn [associated-item associated-item-attr]
     (let [associated-item-attr-name (name associated-item-attr)
           exploded-attr-name (str (name association-name) "_" index "_" associated-item-attr-name)
           exploded-attr-value (input exploded-attr-name)]
       (if (seq exploded-attr-value)
         (conj associated-item {associated-item-attr exploded-attr-value})
         associated-item)))
   {}
   association-schema))

(defn extract-associated-items [attr input]
  "Extracts a collection representing a 'has many' association from input.
  It returns a list of hash-maps, each representing one of the items from
  the association.
  attr is expected to be a hash-map with at least :name and :schema keys,
  while input is expected to be a hash-map representing the whole profile, with strings as keys"
  (let [association-name   (:name attr)
        association-schema (:schema attr)
        attribute-names    (doall (keys input))
        items-id-pattern   (re-pattern (str (name association-name) "_(\\d+)_"))
        items-ids (into #{} (->> attribute-names
                                 (map #(when-some [x (re-find items-id-pattern %)] (last x)))
                                 (filter #(not (nil? %)))))
        associated-items (->> items-ids
                              (map #(extract-associated-item association-name association-schema input %))
                              (filter seq))]
    (when (seq associated-items)
      {association-name associated-items})))

(defn parse-by-schema [input schema]
  "Parses input according to schema, assuming it was shaped as a
  'tall' CSV profile.
  This means that the first column contains attribute names, and the
  second column contains values. Attribute names are presented in
  'exploded' format, in order to properly address associations and
  nesting.
  Example:

  attribute type | attribute name              | exploded name              |
  standard       | timestamp                   | timestamp                  |
  nested item    | profile_data, bedroom_count | profile_data_bedroom_count |
  association    | storeys, first storey_type  | storeys_0_storey_type      |"
  (try
    (let [parsed-data (reduce
                       (fn [item attr]
                         (let [t (attribute-type attr)
                               imploded-attribute (case t
                                                    :attribute               (extract-attribute attr input)
                                                    :nested-item             (extract-nested-item attr input)
                                                    :associated-items        (extract-associated-items attr input))]
                           (if (seq imploded-attribute)
                             (conj item imploded-attribute)
                             item)))
                       {}
                       schema)]
      parsed-data)
    (catch Throwable t
      (log/error t "Received malformed CSV.")
      (throw t))))

(defn rows->columns [rows]
  (apply map vector rows))

(defn data-header? [c]
  (or (.startsWith c "values")
      (.startsWith c "keys")))

(defn csv->maps [rows parse-schema]
  (try
    (let [[k & data] (->> rows
                          rows->columns
                          (filter #(-> % first string/lower-case data-header?)))
          dirty-maps (map #(zipmap k (if (nil? %) "" %)) data)
          maps       (map #(parse-by-schema % parse-schema) dirty-maps)]
      maps)
    (catch Throwable t
      (log/error t "Received malformed CSV.")
      nil)))

(defn temp-file->rows [file-data]
  (let [tempfile (:tempfile file-data)
        dir      (.getParent tempfile)
        filename (.getName tempfile)
        in-file  (io/file dir filename)]
    (with-open [in (io/reader in-file)]
      (try
        (-> (slurp in)
            csv/read-csv)
        (catch Throwable t
          (log/error t "IOError on csv temp file.")
          nil)))))
