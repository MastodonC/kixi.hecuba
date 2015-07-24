# Hecuba

A data platform built with [Clojure][CLJ], [ClojureScript][CLJS], [core.async][CORE.ASYNC], [Om][OM], Cassandra and other technologies.

## Build Status

[![Build Status](https://travis-ci.org/MastodonC/kixi.hecuba.png?branch=master)](https://travis-ci.org/MastodonC/kixi.hecuba)

## Usage

To start the server, clone the repo, and then lein repl or cider-jack-in as you prefer

```
(go)
```

When you make changes just reset.

```
(reset)
```

Then you'll need to compile the clojurescipt. That can be done with
cljsbuild like this

```
lein cljsbuild auto hecuba
```

This will recompile your clojurescript each time you save. The server
app needs to be bounced by doing ```reset``` as above.

If you pull from github, you'll still probably want to restart your
nrepl session though.

To clean the build do:

```
lein clean
```

This will delete both clj and cljs from `target` and `out` directories.

You can also use figwheel:
Instead of running ``` lein cljsbuild auto hecuba``` and reloading the
website, update ``` project.clj ``` to have this:

```
:env {:is-dev true}

```
and run:

```
lein figwheel hecuba
```

Each time you save your cljs files, figwheel will recompile and
refresh the website for you.

Environment should be set to ``` {:is-dev false} ``` before
deployment to the server.
For production deployment please compile once:

```
lein cljsbuild once hecuba
```

To run Clojure tests do:

```
lein test
```

To run ClojureScript tests:

```
lein cljsbuild test
```

## Dev environment

We are using Vagrant to manage dev environments.

The goal of the Vagrant setup is to provide, in a simple 'black box' that just works™, all the services that you need to run hecuba.

You shouldn't need to login to the vagrant box at all, everything is forwarded outside the box.

You will still run your hecuba instance local to your machine (not in the vagrant box).


+ Install VirtualBox v4.3.28 (correct as of 10th June 2015) from [here](https://www.virtualbox.org/wiki/Downloads) or preferably via your OS's package manager.
+ Install Vagrant v. 1.7.2 (correct as of 10th June 2015) from [here](http://www.vagrantup.com/) or preferably via your OS's package manager.
+ install the vbguest plugin so Virtual Box guest additions will updated
  for you ``vagrant plugin install vagrant-vbguest``
+ ``cd ${PROJECT_HOME}``
+ ``vagrant up`` (This will download stuff the first time and will be slow, after that it will be quicker)
+ You will now have all the services required running in a virtual machine with the ports forwarded for access from your local machine
+ You will need to install a cassandra _client_ only on your local machine. (You should not be running a cassandra locally as the ports will clash with those forwarded from the vagrant machine).

### First Time Test Data

+ Work out what the hostname you should use to connect to cassandra is. Look at the output of ``netstat -tln``, find the line that says something like ``aaa.bbb.ccc.ddd:9042``. (note this might be an ipv6 address depending on how your network is configured). This is the address to use in your hecuba config file and in the commands below. If the address is ``127.0.0.1`` you can omit the address from the commands below, since that's the default.

On your machine (not the vagrant box):

+ create the test schema ``cqlsh aaa.bbb.ccc.ddd -f cql/hecuba-schema.cql`` (this might show an error the first time you run it about the test namespace not existing, you can ignore that error)
+ At the shell prompt, create the Elastic Search schema by running ``scripts/build-es-schema.sh``
+ Create a file ``~/.hecuba.edn``` with the following contents:
```
{
 :cassandra-session {:keyspace :test}
 :hecuba-session {:keyspace :test}
 :search-session {:host "<the address you found above>" :name "hecuba"}
 :s3          {:access-key "<your personal AWS access key DO NOT SHARE KEYS!>"
               :secret-key "<your personal AWS secret key DO NOT SHARE KEYS!>"
               ;; you will need to create these buckets.
               :file-bucket "mc-<yourname>-hecuba-uploads"
               :status-bucket "mc-<yourname>-hecuba-status"
               :download-dir "/tmp"}
               
}
```

Back on your host machine do the following:

+ Start a repl in your favourite way.
+ Start the application with (go)
+ (require 'etl)
+ (kixi.hecuba.security/add-user! (:store kixipipe.application/system) "Mastodon" "support@mastodonc.com" "password" :kixi.hecuba.security/super-admin  #{} #{} )
+ (etl/load-test-data)

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

Copyright © 2014 Mastodon C Ltd

Distributed under the Eclipse Public License version 1.0.
