# Hecuba

A data platform built with [Clojure][CLJ], [ClojureScript][CLJS], [core.async][CORE.ASYNC], [Om][OM], Cassandra and other technologies.

## Build Status

[![Build Status](https://travis-ci.org/MastodonC/kixi.hecuba.png?branch=master)](https://travis-ci.org/MastodonC/kixi.hecuba)

## Usage

Clone the repo, and then lein repl or cider-jack-in as you prefer

```
(go)
```

When you make changes just reset.

```
(reset)
```

If you pull from github, you'll still probably want to restart your
nrepl session though.

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
+ Copy the contents of hecuba-schema.sql onto the box you've ssh'd
  into using vi or similar.
+ create the test schema ``cqlsh kixi-dev -f hecuba-schema.sql``
* Start cqlsh with the right hostname ``cqlsh kixi-dev``

Back on your host machine do the following:

+ Start a repl in your favourite way.
+ Start the application with (go)
+ (require 'etl)
+ (etl/load-user-data) to create the users
+ (etl/load-csv system) to add some test programmes, projects, etc

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
