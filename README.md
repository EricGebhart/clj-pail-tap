clj-pail-tap
============

Extension library to enable easier tapping of pails.

Built on top of David Cuddeback's [clj-pail](https://github.com/dcuddeback/clj-pail).

## Usage

Add `clj-pail` to your project's dependencies. If you're using Leiningen, your `project.clj` should look something like this:

~~~clojure
(defproject ...
  :dependencies [[clj-pail-tap VERSION]])
~~~

Where `VERSION` is the latest version on [Clojars](https://clojars.org/clj-pail-tap).

### Defining a `PailStructure`

You can generate classes that implement the `PailStructure` interface with the [`gen-structure` macro](src/main/clojure/clj_pail_tap/structure.clj) from the `clj-pail-tap.structure` namespace. The `PailStructure` interface is used by Pail to serialize, deserialize, and keep organized your data.

~~~clojure
(ns ...
  (:require [clj-pail-tap.structure :as s]))

(s/gen-structure com.example.pail.DefaultPailStructure)

~~~

`gen-structure` uses `gen-class`. So any namespace that uses `gen-structure` needs to be AOT-compiled. In Leiningen, add your namespace to the `:aot` key in `project.clj`:

~~~clojure
(defproject ...
  :aot [myproj.ns.that.uses.clj-pail.structure])
~~~

#### Options

By default, a `PailStructure` class generated with `gen-structure` will do nothing. It will be defined to handle `byte[]`; serialization and deserialization will do nothing (because your data is already a `byte` array); and no vertical partitioning will be defined.

These behaviors can be specified with options to `gen-structure`:

~~~clojure
(s/gen-structure com.example.pail.CustomPailStructure
                 :type DataUnit-A-Thrift-Class.
                 :schema DataUnit-Some-Prismatic-Schema-definition.
                 :serializer (CustomDateSerializer. DataUnit-A-Thrift-Class)
                 :partitioner (DailyDatePartitioner.)
                 :tapmapper (DataUnit-tapmapper)
                 :property-path-generator (DataUnit-property-paths)
                 :prefix "date-")
~~~

Type and schema are mutually exclusive. Type should be used for objects, whereas Schema is
for the situation when the serializer is Fressian instead of thrift and the schema is Prismatic schema
rather than graph schema.

Property-path-generator should be a function which takes whichever is being used, type or schema, and returns
a list of property path vectors.

Tap Mapper should be a function that can be mapped to output of the property-path-generator and provides property
path which correlates to the path the partitioner would create.

The [Pail Graph](https://github.com/EricGebhart/pail-graph) library uses graph schema and thrift. 
Where the [Pail Schema](https://github.com/EricGebhart/pail-schema) library uses Prismatic Schema and Fressian.

Both libraries provide full tap mapping features.


## License

Copyright © 2014 Eric Gebhart

Distributed under the [MIT License](LICENSE).
