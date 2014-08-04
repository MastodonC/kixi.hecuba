/**
 * Created with IntelliJ IDEA.
 * User: annapawlicka
 * Date: 02/07/2013
 * Time: 14:34
 * Functions to process csv and json data and add info and legend to the map.
 */

var mergedFeatureLayer = function mergedFeatureLayer(map, csvDir, jsonDir, joinFieldKey, style, onEachFeature, pointToLayer, featureObject) {

    var buildingData = $.Deferred();

    d3.csv(csvDir, function (csv) {

        if (csv) {
            $.ajax(
                {
                    url: jsonDir,
                    async: false,
                    data: 'json',

                    success: function (data) {
                        var pcts = topojson.feature(data, data.objects[featureObject])
                        features = pcts.features;
                        data.features = processData(csv, features, joinFieldKey);
                        buildingData.resolve(data);
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        console.log(xhr.status + " - " + thrownError);
                    }
                });
        }
        else {
            console.log("Error loading CSV data");
        }
    });

    buildingData.done(function (d) {
        var mergedLayer = L.geoJson(d, {style: style, onEachFeature: onEachFeature, pointToLayer: pointToLayer}).addTo(map);
        console.log("Loading merged data: "+csvDir+" and "+jsonDir);
        mergedLayer.bringToFront();

    });
};

var mergedClusteredMarkers = function mergedClusteredMarkers(map, csvDir, jsonDir, joinFieldKey, style, onEachFeature, pointToLayer, featureObject, iconFeature, addPopup, getCustomIcon) {

    var buildingData = $.Deferred();

    d3.csv(csvDir, function (csv) {

        if (csv) {
            $.ajax(
                {
                    url: jsonDir,
                    async: false,
                    data: 'json',

                    success: function (data) {
                        var pcts = topojson.feature(data, data.objects[featureObject])
                        features = pcts.features;
                        data.features = processData(csv, features, joinFieldKey);
                        buildingData.resolve(data);
                    },
                    error: function (xhr, ajaxOptions, thrownError) {
                        console.log(xhr.status + " - " + thrownError);
                    }
                });
        }
        else {
            console.log("Error loading CSV data");
        }
    });

    buildingData.done(function (d) {
        var markers = new L.MarkerClusterGroup({
            maxClusterRadius: 25,
            disableClusteringAtZoom: 10,
            iconCreateFunction: function (cluster) {
                return L.divIcon({
                    html: '<span style="display:inline-block; vertical-align:middle">'+ cluster.getChildCount()+' </span>',
                    className: 'mycluster',
                    iconSize: null
                });
            },
            spiderfyOnMaxZoom: true,
            showCoverageOnHover: false,
            zoomToBoundsOnClick: true
        });

        for (var i = 0; i < d.features.length; i++) {
            var a = d.features[i].geometry.coordinates;
            var properties = d.features[i].properties;
            var marker = new L.Marker(new L.LatLng(a[1], a[0]), {
                icon: getCustomIcon(properties[iconFeature]) });
            marker.bindPopup(addPopup(properties));
            markers.addLayer(marker);
        }
        markers.addTo(map);
    });
};


function processData(csvData, features, joinKey) {

    var joinFieldObject = {};

    $.each(features, function (index, object) {

        joinFieldObject[joinKey] = object.properties[joinKey];

        var csv_data = _.findWhere(csvData, joinFieldObject);
        $.extend(object.properties, csv_data);
    });
    return features;
};

var featureLayer = function featureLayer(map, jsonDir, defaultStyle, featureObject) {
    var layer = L.geoJson(null, { style: defaultStyle});
    console.log("Loading feature data: "+jsonDir);
    map.addLayer(layer);
    d3.json(jsonDir, function (error, data) {
        var pcts = topojson.feature(data, data.objects[featureObject]);
        var geojsonLayer = L.geoJson(pcts, {style: defaultStyle}).addTo(map);
        geojsonLayer.bringToBack();
    });
};

function addInfo(map, callback) {

    var info = L.control();

    info.onAdd = function (map) {

        this._div = L.DomUtil.create('div', 'info');
        this.update();
        return this._div;
    };

    info.update = function (props) {
        if (props) {
            this._div.innerHTML = callback(props);
        } else {
            this._div.innerHTML = "Hover over map";
        }
    };

    info.addTo(map);
    map.info = info;
};


function addLegend(gradesParam, map, color) {

    var legend = L.control({position: 'bottomright'});

    legend.onAdd = function (map) {

        this._div = L.DomUtil.create('div', 'info legend'),
            grades = gradesParam,
            labels = [];

        // loop through our density intervals and generate a label with a colored square for each interval
        for (var i = 0; i < grades.length - 1; i++) {
            this._div.innerHTML +=
                '<i style="background:' + color(grades[i] + 1) + '"></i> ' +
                    grades[i] + ' &ndash; ' + grades[i + 1] + '<br>';
        }
        return this._div;
    };

    legend.addTo(map);
};


function addCategoricalLegend(categories, map, categoricalColor) {
    var legend = L.control({position: 'bottomright'});

    legend.onAdd = function (map) {

        this._div = L.DomUtil.create('div', 'info legend'),
            grades = categories,
            labels = [];

        // loop through categories and generate a label with a colored square for each category
        for (var i = 0; i < grades.length; i++) {
            this._div.innerHTML +=
                '<i style="background:' + categoricalColor(grades[i]) + '"></i> ' +
                    grades[i] + '<br>';
        }
        return this._div;
    };

    legend.addTo(map);
};

/* Function that is used to estimate the width of the custom tooltip */
function measureText(pText, pFontSize, pStyle) {
    var lDiv = document.createElement('lDiv');

    document.body.appendChild(lDiv);

    if (pStyle != null) {
        lDiv.style = pStyle;
    }
    lDiv.style.fontSize = "" + pFontSize + "px";
    lDiv.style.position = "absolute";
    lDiv.style.left = -1000;
    lDiv.style.top = -1000;

    lDiv.innerHTML = pText;

    var lResult = {
        width: lDiv.clientWidth,
        height: lDiv.clientHeight
    };

    document.body.removeChild(lDiv);
    lDiv = null;

    return lResult;
}
