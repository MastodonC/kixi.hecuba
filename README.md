# Hecuba

A data platform built with [Clojure][CLJ], [ClojureScript][CLJS], [core.async][CORE.ASYNC], [Om][OM], Kafka, Cassandra and other technologies.

## Usage

Clone and install [Jig][JIG].

```
$ cd jig
jig/ $ lein install-all
```

The need to install is only due to the possibility that Hecuba is using a SNAPSHOT version of Jig which is gnot released to clojars.org.

Point Jig at Hecuba's config.

```
$ mkdir $HOME/.jig
$ ln -s kixi.hecuba/config.clj $HOME/.jig/config.clj
```

Start Jig

```
$ cd jig
jig/ $ lein repl
```

Navigate to http://localhost:8000

## Support

Hecuba is a new project which is being developed 'in the open'. Therefore it may not work exactly as described in these notes. Hecuba is not (yet) a released and supported code-base, rather it is being made available for others as a learning aid. Please bear this in mind when contacting the team, raising issues and submitting pull requests.

## References

[CLJ]: http://clojure.org "Clojure"
[CLJS]: https://github.com/clojure/clojurescript "ClojureScript"
[OM]: https://github.com/swannodette/om "Om"
[CORE.ASYNC]: https://github.com/clojure/core.async "core.async"
[JIG]: https://github.com/juxt/jig "Jig"
[AMON]: http://amee.github.io/AMON "AMON"
[AMON-API]: http://blog.amee.com/products/ameerealtime/amee-realtime-uploading-data-using-the-api/#h.sxcz95x9lvwy

[AMON-UPLOADING]: https://est.amee.com/pdfs/UploadingDataAPI.pdf

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
