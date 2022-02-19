const fs = require("fs");
const kml = require('gtran-kml');

// from https://stackoverflow.com/a/9493060
/**
 * Converts an HSL color value to RGB. Conversion formula
 * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
 * Assumes h, s, and l are contained in the set [0, 1] and
 * returns r, g, and b in the set [0, 255].
 *
 * @param   {number}  h       The hue
 * @param   {number}  s       The saturation
 * @param   {number}  l       The lightness
 * @return  {Array}           The RGB representation
 */
function hslToRgb(h, s, l){
    var r, g, b;

    if(s == 0){
        r = g = b = l; // achromatic
    }else{
        var hue2rgb = function hue2rgb(p, q, t){
            if(t < 0) t += 1;
            if(t > 1) t -= 1;
            if(t < 1/6) return p + (q - p) * 6 * t;
            if(t < 1/2) return q;
            if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
            return p;
        }

        var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        var p = 2 * l - q;
        r = hue2rgb(p, q, h + 1/3);
        g = hue2rgb(p, q, h);
        b = hue2rgb(p, q, h - 1/3);
    }

    return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)];
}


function dec2hex(d, padding) {
    var hex = Number(d).toString(16);
    padding = typeof (padding) === "undefined" || padding === null ? padding = 2 : padding;

    while (hex.length < padding) {
        hex = "0" + hex;
    }

    return hex;
}

function rgb2hex(r, g, b) {
    return "#" + dec2hex(r) + dec2hex(g) + dec2hex(b);
}

function getColorForDistrict(district) {
    let color = hslToRgb(district / 24.0, 0.8, 0.6)
    return rgb2hex(color[0], color[1], color[2])
}

function areaStyle(feature) {
    let district = feature.properties.BEZNR
    return {
        color: getColorForDistrict(district),
        alpha: 170
    }
}

function tourStyle(feature) {
    return {
        color: "#000000",
        alpha: 200,
        width: 6.0
    }
}

function vertexStyle(count, isTourVertex) {
    return function(feature) {
        if (count < 100) {
            scale = 1.0
        } else {
            scale = 0.6
        }
        if (isTourVertex) {
            icon = 'https://maps.google.com/mapfiles/kml/paddle/red-stars.png'
        } else {
            icon = 'https://maps.google.com/mapfiles/kml/paddle/orange-blank.png'
        }
        return {
            color: '#2dcd86',
            alpha: 255 * Math.random(),
            scale: scale,
            icon: icon
        }
    }
}

function featureStyle(feature) {
    if (feature.geometry.type == "Point") {
        return vertexStyle(1, true)(feature);
    } else if (feature.geometry.type == "LineString") {
        return tourStyle(feature);
    } else {
        return areaStyle(feature);
    }
}

const args = process.argv.slice(2);

if (args.length !== 1) {
    throw "Expected one argument: outputDirectory"
}

let outputDirectory = args[0];

fs.readFile(outputDirectory + "/areas.json", (err, data) => {
    kml.fromGeoJson(JSON.parse(data), outputDirectory + "/areas.kml", {
        symbol: areaStyle,
        documentName: "areas"
    });
})

fs.readFile(outputDirectory + "/tour.json", (err, data) => {
    kml.fromGeoJson(JSON.parse(data), outputDirectory + "/tour.kml", {
        symbol: featureStyle,
        documentName: "tour"
    });
})

fs.readFile(outputDirectory + "/vertices.json", (err, data) => {
    if (err == null) {
        let parsed = JSON.parse(data)
        kml.fromGeoJson(parsed, outputDirectory + "/vertices.kml", {
            symbol: vertexStyle(parsed.features.length, false),
            documentName: "vertices"
        });
    }
})