#!/usr/bin/env bash

docker build -t gtsp-downloader -f docker/Dockerfile.downloader .
docker create -ti --name dummy gtsp-downloader bash
docker cp dummy:/vienna.osm.pbf data/vienna.osm.pbf
docker rm -f dummy
