

import ij.IJ;
import ij.io.DirectoryChooser;
import ij.plugin.*;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class Map_Visualizer implements PlugIn {

	String tilesUrl="";
	String templateUrl="tiles/{z}/{x}/{y}.png";
	String mapName="map";
	
	
	@Override
	public void run(String arg0) {
		
		
		if (arg0==""){
		tilesUrl = new DirectoryChooser("choose a map directory").getDirectory();
		tilesUrl = tilesUrl + templateUrl;
		}
		else{
			tilesUrl = arg0;
		}
		
		try {
			createWebVisualizer();
			openWebpage(new URI("file:///home/cig-gherardo/ownCloud/Segmentation/Fiji.app/mapVisualizer/index.html"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
		
		} catch (Exception e) {
			// TODO Auto-generated catch block

		}
		
	}
	
	private static void openWebpage(URI uri) throws Exception {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	        } catch (Exception e) {
	        throw e;
	        }
	    }
	}
	
	public void createWebVisualizer(){
		File wD = new File(IJ.getDirectory("startup")+"mapVisualizer");
		wD.mkdirs();
		File index = new File(wD.toString()+File.separator+"index.html");
		//index.delete();
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(index, false)));
		    out.println("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><title>"+mapName+"</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"><link rel=\"stylesheet\" href=\"http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css\"><script src=\"http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js\"></script><script src='https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v0.0.4/Leaflet.fullscreen.min.js'></script><link href='https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v0.0.4/leaflet.fullscreen.css' rel='stylesheet' /><link rel='stylesheet' href='style/leaflet.draw.css'><script src='js/leaflet.draw.js'></script></head><body><div id=\"map\" style=\"z-index:0; width:100%; height:100%; position:absolute;\"></div><script>var map = L.map('map',{minZoom:0, maxZoom:18, crs: L.CRS.Simple, fullscreenControl: false}).setView([-100,100],1);var baseLayer=L.tileLayer(\""+tilesUrl+"\",{continuousWorld: true}); baseLayer.addTo(map);var drawnItems = new L.FeatureGroup();map.addLayer(drawnItems);var drawControl = new L.Control.Draw({ edit: {featureGroup: drawnItems}});map.addControl(drawControl);map.on('draw:created',function(a){var e=(a.layerType,a.layer);map.addLayer(e)});</script></body></html>");
		    out.close();
		} catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
	}
	
	
	
}
