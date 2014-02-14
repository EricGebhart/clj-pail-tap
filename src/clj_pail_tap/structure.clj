(ns clj-pail-tap.structure
  "Utilities for defining Pail structures."
  (:require [clj-pail.serializer :as serializer]
            [clj-pail.partitioner :as partitioner]
            [clj-pail-tap.tapmapper :as tapmapper]))

;; ## Generating Pail Structures

(defmacro gen-structure
"Generates a class that implements `PailStructure`. The `PailStructure`'s behavior can be
customized by providing a `Serializer` or `VerticalPartitioner`. The class will be named whatever
is provided for the `name` parameter. (It should be specified just as it would be `gen-class`.

The `Serializer` and `VerticalPartitioner` should be able to handle the object type specified by the
`:type` option or handle any type in the case that schema is being used instead of type. Type is
a java class like a thrift type, where as schema is a prismatic schema definition. The schema is
only used when creating property paths for the pail tap functions. If there is no schema, the
type is used.

### Options

`:type`: The object type that the `PailStructure` can serialize. (Defaults to a byte array.)

`:schema`: The Prismatic Schema which defines the objects being serialized, defaults to nil.

`:serializer`: An implementation of the `Serializer` protocol, which will be used to serialize and
deserialize object. (Defaults to `NullSerializer`.)

`:partitioner`: An implementation of the `VerticalPartitioner` protocol, which will be used to
vertically partition the data. (Defaults to `NullPartitioner`.)

`:tapmapper`: A function which will filter Data type property paths as generated by
pail-graph/type/property-paths returning a map of paths which correspond to the properties and
the paths they are partitioned into. Functionality is corrolated the behavior of the partitioner.
vertically partition the data. (Defaults to `NullPartitioner`.)

`:property-path-generator`: A function that will create a list of property paths for each property
in a data type or schema's property tree.

`:prefix`: Used to specify the prefix for the generated methods, just like with `gen-class`. A
prefix should be used to avoid name collisions when generating more than one class in the same
namespace. (Defaults to `\"-\"`.)

Any namespace that uses `gen-structure` should be configured to be AOT-compiled."
  [the-name & {:keys [type schema serializer partitioner tapmapper property-path-generator prefix]
               :or {type (class (byte-array 0))
                    schema nil
                    serializer `(serializer/null-serializer)
                    partitioner `(partitioner/null-partitioner)
                    tapmapper `(tapmapper/null-tapmapper)
                    property-path-generator `(tapmapper/null-path-generator)
                    prefix "-"}}]
  `(do
     (gen-class
       :name ~the-name
       :extends clj_pail.structure.AbstractPailStructure
       :prefix ~prefix
       :methods [[ "getTapMapper" [] Object] [ "getPropertyPathGenerator" [] Object] ["getSchema" [] Object]]
       :main false)

     (defn ~(symbol (str prefix "createSerializer")) [this#]
       ~serializer)

     (defn ~(symbol (str prefix "createPartitioner")) [this#]
       ~partitioner)

     (defn ~(symbol (str prefix "getType")) [this#]
       ~type)

     (defn ~(symbol (str prefix "getSchema")) [this#]
       ~schema)

     (defn ~(symbol (str prefix "serialize")) [this# object#]
       (serializer/serialize (.getSerializer this#) object#))

     (defn ~(symbol (str prefix "deserialize")) [this# buffer#]
       (serializer/deserialize (.getSerializer this#) buffer#))

     (defn ~(symbol (str prefix "getTarget")) [this# object#]
       (partitioner/vertical-partition (.getPartitioner this#) object#))

     (defn ~(symbol (str prefix "isValidTarget")) [this# dirs#]
       (partitioner/valid-partition? (.getPartitioner this#) dirs#))

     (defn ~(symbol (str prefix "getTapMapper")) [this#]
       ~tapmapper)

     (defn ~(symbol (str prefix "getPropertyPathGenerator")) [this#]
       ~property-path-generator)))
