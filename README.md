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

+ Install VirtualBox v4.3.8 from [here](https://www.virtualbox.org/wiki/Downloads) or preferably via your OS's package manager.
+ Install Vagrant v. 1.4.3 from [here](http://www.vagrantup.com/) or preferably via your OS's package manager.
+ install the vbguest plugin so Virtual Box guest additions will updated
  for you ``vagrant plugin install vagrant-vbguest``
+ ``cd ${PROJECT_HOME}``
+ ``vagrant up`` (This will download stuff the first time and will be slow, after that it will be quicker)
+ You will now have all the services required running in a virtual machine with the ports forwarded for access from your local machine

### First Time Test Data

+ Log into the vagrant box using ``vagrant ssh``
+ Copy the contents of hecuba-schema.cql onto the box you've ssh'd
  into using vi or similar.
+ create the test schema ``cqlsh kixi-dev -f cql/hecuba-schema.cql``
* Start cqlsh with the right hostname ``cqlsh kixi-dev``

Note: You'll need to install Elastic Search in your vagrant box:
1) Follow instructions from https://www.digitalocean.com/community/tutorials/how-to-install-elasticsearch-on-an-ubuntu-vps
2) Change the name of the cluster in /etc/elasticsearch/elasticsearch.yml from test to hecuba



Back on your host machine do the following:

+ Start a repl in your favourite way.
+ Start the application with (go)
+ Add your user account: (kixi.hecuba.security/add-user! (:store kixi/system) "Username" "username@mastodonc.com" "password" :kixi.hecuba.security/super-admin {} {})
+ To populate Cassandra with data, run the following script:
  ```
  $ cd scripts
  $ ./contextual_data.sh
  ```

## Start an EC2 instance

+ make sure you have foreman installed
+ add the aws plugin ``vagrant plugin install vagrant-aws``

```
$ gem install foreman
```

+ copy env.example into .env and edit your AWS credentials in there
+ ``foreman run vagrant up --provider=aws``


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
