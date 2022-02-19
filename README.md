# Hatschen
> How many roads must a man walk down in order to cover each set once?

Hatschen, from the colloquial austrian verb for walking, uses OpenStreetMaps data and a GTSP solver
to answer the question: what is the shortest walking route through the city of Vienna which visits each of the
23 districts at least once.

![Vienna Tour](/screenshots/boundary-footpath-medium.jpg)

The answer is somewhere around 28 kilometers.

## Running
```
# setup NPM
$ npm install

# download OSM data (optional)
$ ./download-data.sh

# generate KML files
$ ./run.sh --areas /path/to/data/districts-edited.json -o /tmp --osmFile /path/to/data/vienna.osm.pbf -s boundary -d footpath

# options for -s: centroid, center-bias, grid, boundary
# options for -d: haversine, manhattan, footpath
```

## Tools
Several great applications and libraries are used in this project:
- GLNS (https://github.com/stephenlsmith/GLNS.jl)
  - GTSP solver
- Graphhopper (https://github.com/graphhopper/graphhopper)
  - OSM routing engine
  - Used to provide precise distances between vertices and compute walk segments in the final tour
- gtran-kml (https://github.com/haoliangyu/gtran-kml)
  - For generating the final KML files which can be viewed in Google Earth 

## Details
To Follow