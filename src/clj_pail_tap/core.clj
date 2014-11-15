(ns clj-pail-tap.core
  "Defines clj-pail-tap core functionality.
   clj-pail.core is integrated
   into this namespace."
  (:require [potemkin :as pt]
            [clj-pail.core])
  (:import (com.backtype.hadoop.pail Pail PailSpec PailStructure)))

; pull in clj-pail core functionality.
(pt/import-vars [clj-pail.core
                    object-seq
                    spec
                    pail
                    with-snapshot
                    create])


(defn pail-structure
  "Given a pail return the PailStructure."
  [pail]
  (-> pail (.getSpec) (.getStructure)))

(defn get-structure
  "Given a PailStructure or a Pail return a PailStructure"
  [pail-or-structure]
  (if (instance? PailStructure pail-or-structure)
        pail-or-structure
        (pail-structure pail-or-structure)))

(defn get-schema-or-type
  [pail-struct]
  (if-let [res (.getSchema pail-struct)]
    res
    (.getType pail-struct)))

(defn tap-map
  "Get the tap map for a Pail or Pail Structure"
  [pail-or-struct]
  (let [pail-struct (get-structure pail-or-struct)
        tapmapper (.getTapMapper pail-struct)
        path-generator (.getPropertyPathGenerator pail-struct)
        type (get-schema-or-type pail-struct)]
    (into {} (map tapmapper (path-generator type)))))

(defn list-taps
  "Give a list of the Tap keys available for a Pail or Pail Structure"
  [pail-or-struct]
  (let [pail-struct (get-structure pail-or-struct)]
    (keys (tap-map pail-struct))))


;;;; TODO
(defn validate
  "Validate that a pail connection matches a pail structure. This is basically an implementation
   of the validation code in dfs-datastores pail create(). The specs are only compared if
   .getName is not nil. Otherwise it's just a check to make sure the PailStructure types match."
  [pail-connection structure]
  (let [conn-spec (.getSpec pail-connection)
        conn-struct (.getStructure conn-spec)
        struct-spec (spec structure)]
    (cond (and (.getName struct-spec) (not (.equals conn-spec struct-spec))) false
          (not (= (type structure) (type conn-struct))) false
          :else true)))

(defn move-append
  "Move contents of pail and append to another pail.
   Rename if necessary. "
  [source-pail dest-pail]
  (.moveAppend dest-pail source-pail 1))

(defn copy-append
  "Move contents of pail and append to another pail. Rename
   if necessary. "
  [source-pail dest-pail]
  (.copyAppend dest-pail source-pail 1))

(defn absorb
  "Absorb one pail into another. Rename if necessary."
  [dest-pail source-pail]
  (.absorb dest-pail source-pail 1))

(defn consolidate
  "consolidate pail"
  [pail]
  (.consolidate pail))

(defn snapshot
  "Create snapshot of pail at path."
  [pail path]
  (.snapshot pail path))

(defn delete-snapshot
  "delete the snapshot of a pail."
  [pail snapshot]
  (.deleteSnapshot pail snapshot))

(defn pail-is-empty?
  "check pail for emptiness"
  [pail]
  (.isEmpty pail))

(defn pail-exists?
  "check to see if the pail path exists."
  [path]
  (.exists path))

(defn delete
  "delete pail path recursively"
  [path]
  (.delete path true))

;; these can go away when the clj-pail clojars is updated with the pull request.
(defn ^Pail create
  "Creates a Pail from a PailSpec at `path`."
  [spec-or-structure path & {:keys [filesystem fail-on-exists]
                             :or {fail-on-exists true}
                             :as opts}]
  (if (instance? PailStructure spec-or-structure)
    (apply create (spec spec-or-structure) path (mapcat identity opts))
    (if filesystem
      (Pail/create filesystem path spec-or-structure fail-on-exists)
      (Pail/create path spec-or-structure fail-on-exists))))

(defn find-or-create [pstruct path & {:as create-key-args}]
  "Get a pail from a path, or create one if not found"
  (try (pail path)
       (catch Exception e
         (apply create pstruct path (mapcat identity create-key-args)))))

(defn write-objects
  "Write a list of objects to a pail"
  [pail objects]
  (with-open [writer (.openWrite pail)]
    (doseq [o objects]
      (.writeObject writer o))))
