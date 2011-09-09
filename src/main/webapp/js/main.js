var map = new OpenLayers.Map('map');
var wms = new OpenLayers.Layer.WMS("OpenLayers WMS",
        "http://labs.metacarta.com/wms/vmap0", {layers: 'basic'});
map.addLayer(wms);
map.zoomToMaxExtent();

OpenLayers.loadURL('rest/flooding', '', this, function(response) {
    alert("Call to rest/flooding, response: " + response.responseText);
});