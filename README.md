# Map_tools v1.0.0
 An imageJ plugin to create and visualize maps (tileLayers).
- Author: Gherardo Varando <gherardo.varando@gmail.com>

### Description
This imageJ plugin add two tool into process>Map tool menu in FIJI:
- Map Creator: a simple tool to create a map directory structure from any image size.
- Map visualizer: an html-based display of maps based on the leaflet js library.

### Layer configuration

The plugin create, starting form an image, a tiled map structure (`{z}/{x}/{y}.png` or `{level}/{z}/{x}/{y}.png`) and a configuration object that can be loaded by leaflet through the [leaflet-map-builder](http://github.com/gherardovarando/leaflet-map-builder) plugin.

### Installation
As every imageJ plugin just add the .jar into the plugin directory. Moreover you need to copy the json-simple-1.1.1.jar file (available in the release page) in a folder called ``lib`` into the plugins folder.
