# Test instance deploy.

## AWS instances

Start up

+ 1 appserver box (runs kixi.hecuba).
+ 1 server box (runs cassandra and elasticsearch)

They should be in a VPC.

## Prerequisites (on each box)

### docker

``` sh
sudo apt-get update
sudo apt-get -y upgrade
sudo apt-get install docker.io
```

### Weave

``` sh
sudo wget -O /usr/local/bin/weave https://github.com/zettio/weave/releases/download/latest_release/weave
sudo chmod a+x /usr/local/bin/weave
```

### building kixi.hecuba

``` sh
lein clean; lein uberimage
```

### deploying kixi.hecuba

you will need to have you hub.docker.com account set up to do this: [look here](https://hub.docker.com/account/signup/)

``` sh
docker push mastodonc/kixi.hecuba
```

## Starting things up

On each box:

``` sh
sudo weave launch
```

On one of the boxes:

``` sh
sudo weave connect <private ip of other box>
```

Then run the start script. It should pull the docker images and start things up.


## Installing the elastic search schema.

copy the ``bin/build-es-schema.sh`` and ``elasticsearch`` directory to the appropriate host.

modify the script to have the correct port (see ``docker port`` below). You can then run the script and load the schema into the es in the container.


## Installing the cql schema.

This is A PITA.

Need to have the CQL client available somewhere (and that CQL client required python2.7).

The easiest way is to enter the container and run ``/apache-cassandara/bin/cqlsh -f <the  hecuba-schema.cql file> 10.87.0.10``

Getting into the container is awkward. I use [docker-enter](https://github.com/jpetazzo/nsenter)


## What's happening?

``` sh
weave ps # show's the ip address of the containers.

docker ps # show docker info (including ports)

docker port es01 9200 # Show which host port the elasticsearch container port 9200 is mapped to.

# eg:
ubuntu@ip-10-224-1-253:~$ docker port es01 9200
0.0.0.0:49155
ubuntu@ip-10-224-1-253:~$ curl http://localhost:49155/
{
  "status" : 200,
  "name" : "Jebediah Guthrie",
  "cluster_name" : "hecuba",
  "version" : {
    "number" : "1.5.0",
    "build_hash" : "544816042d40151d3ce4ba4f95399d7860dc2e92",
    "build_timestamp" : "2015-03-23T14:30:58Z",
    "build_snapshot" : false,
    "lucene_version" : "4.10.4"
  },
  "tagline" : "You Know, for Search"
}
```
