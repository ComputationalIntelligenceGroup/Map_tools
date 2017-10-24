# Map_tools
 An imageJ plugin to create and visualize maps (tileLayers). (beta)
- Author: Gherardo Varando <gherardo.varando@gmail.com>

### Description
This imageJ plugin add two tool into process>Map tool menu in FIJI:
- Map Creator: a simple tool to create a map directory structure from (just for now) a square image.
- Map visualizer: an html-based display of maps based on the leaflet js library.

### Layer configuration

The plugin create, starting form an image, a tiled map structure (`{z}/{x}/{y}.png` or `{level}/{z}/{x}/{y}.png`) and a configuration object that can be loaded by leaflet through the [leaflet-map-builder](http://github.com/gherardovarando/leaflet-map-builder) plugin.

### Installation
As every imageJ plugin just add the .jar into the plugin directory.
