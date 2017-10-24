

import ij.IJ;
import ij.io.DirectoryChooser;
import ij.plugin.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;


public class Map_Visualizer implements PlugIn {

	String tilesUrl="";
	String templateUrl="{z}/{x}/{y}.png";
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
			openWebpage(new URI("file://"+IJ.getDirectory("startup")+"mapVisualizer"+File.separator+"index.html"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
            IJ.log(e.toString());		
		} catch (Exception e) {
			// TODO Auto-generated catch block
            IJ.log(e.toString());		
		}
		
	}
	
	private static void openWebpage(URI uri) throws Exception {
		java.awt.Desktop.getDesktop().browse(uri);
	}
	
	public File createWebVisualizer(){
		File wD = new File(IJ.getDirectory("startup")+"mapVisualizer");
		wD.mkdirs();
		File index = new File(wD.toString()+File.separator+"index.html");
		//index.delete();
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(index, false)));
		    out.println("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><title>"+mapName+"</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"><link rel=\"stylesheet\" href=\"http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.css\"><script src=\"http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js\"></script></head><body><div id=\"map\" style=\"z-index:0; width:100%; height:100%; position:absolute;\"></div><script>var map = L.map('map',{minZoom:0, maxZoom:18, crs: L.CRS.Simple}).setView([-100,100],1);var baseLayer=L.tileLayer(\""+tilesUrl+"\",{continuousWorld: true}); baseLayer.addTo(map);</script></body></html>");
		    out.close();
		} catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
	    return wD;
	}
	
	
	
}
