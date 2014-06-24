#!/bin/zsh

#upload a template
curl -vvv -X POST -u support@mastodonc.com:xxxxxxxxxxxx -F "name=this is a template" -F "template=@$HOME/my-template.csv" localhost:8010/4/templates/

#download a template
curl -O -J -L localhost:8010/4/templates/<id>
