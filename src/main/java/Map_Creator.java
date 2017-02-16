
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.*;
import ij.WindowManager;
import ij.process.ImageProcessor;
import ij.io.DirectoryChooser;
import ij.io.FileSaver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Map_Creator implements PlugIn {


	protected ImagePlus image;
	protected ImageProcessor ip;

	// image property members
	private int width;
	private int height;
	private int numbSlice;
	private String mapName="map";
	private int tileDim=256;
	private int maxZoom=5;
	private int slice=1;
	private boolean useAll,global;
	private String directory=IJ.getDir("home");
	private Calibration cal;
	public String urlTemplate;
	public File configFile,configTilesFile;

	public void run(String arg) {
		image=WindowManager.getCurrentImage();
		if (image==null){
			IJ.error("You need to open an image first.");
			return;
		}
		width = image.getWidth();
		height = image.getHeight();
		numbSlice = image.getStackSize();
		cal=image.getCalibration();

		if (showDialog()){
			File p= new File(directory + File.separator+mapName+ File.separator+mapName+"_tiles");

			//create the map directory
			p.mkdirs();

            if (useAll){
            	for (int s=1;s<=numbSlice;s++){
            		createMap2d(s);
            	}
            }
            else{
    			createMap2d(slice);
            }
            if (useAll){
    			urlTemplate= "slice{t}/{z}/{x}/{y}.png";
            }
            else{
    			urlTemplate= "{z}/{x}/{y}.png";
            }
			buildConfigurationFile();



		}
	}



	private void buildConfigurationFile(){
		try {
			configFile =new File(directory+ File.separator+mapName+File.separator+mapName+".json");
			configTilesFile =new File(directory+File.separator+mapName+ File.separator+mapName+"_tiles"+File.separator+mapName+"_tiles.json");
			double x_c=(cal.getX(width));
			double y_c=(cal.getY(width));
			double z_c=(cal.getZ(numbSlice));
			String unit = cal.getUnit();
			JSONObject obj = new JSONObject();
			obj.put("name",mapName + "_tiles");
			obj.put("author", "unknown");
			if (useAll){
				obj.put("type","tilesLayer");
				JSONObject ck = new JSONObject();
				JSONArray tt = new JSONArray();
				for (int s=1;s<=numbSlice;s++){
            		tt.add(s);
            	}
				ck.put("t", tt);
				obj.put("customKeys", ck);
			}
			else{
				obj.put("type", "tilesLayer");
			}
			JSONArray tileSize = new JSONArray();
			int dim = Math.max(width,height);
			tileSize.add(tileDim * width/dim );
			tileSize.add(tileDim * height/dim);
			obj.put("tileSize", tileSize);
			obj.put("date", ""+(new Date()));
			obj.put("maxZoom", maxZoom);
			obj.put("maxNativeZoom", maxZoom);

			JSONArray bounds = new JSONArray();
			JSONArray b0 = new JSONArray();
			JSONArray b1 = new JSONArray();
			b0.add(-tileDim);
			b0.add(0);
			b1.add(0);
			b1.add(tileDim);
			bounds.add(b0);
			bounds.add(b1);
			obj.put("bounds", bounds);
			obj.put("nSlice", numbSlice);
			obj.put("sizeCal", Math.max(x_c,y_c));
			obj.put("depthCal", z_c);
			obj.put("unitCal", unit);
			obj.put("tilesUrlTemplate",""+urlTemplate);
			obj.put("baseLayer", true);

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(configTilesFile, false)));
			out.println(obj.toJSONString());
			out.close();

			if (global){
				JSONObject map = new JSONObject();
				map.put("type", "map");
				map.put("name", mapName);
				map.put("author", "unknown");
				JSONArray ly = new JSONArray();
				ly.add(mapName+"_tiles");
				map.put("layers", ly);
				PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter(configFile, false)));
				out2.println(map.toJSONString());
				out2.close();
			}


		} catch (IOException e) {

		}
	}


	public void createMap2d(int slice) {
		image.setSlice(slice);
		ip = image.getProcessor();
		File p;
        if (useAll){
    		 p= new File(directory + File.separator+mapName+ File.separator+mapName+"_tiles"+File.separator+ "slice"+slice);
        }else{
    	     p= new File(directory + File.separator+mapName+ File.separator+mapName+"_tiles");
        }

		//create the map directory
		p.mkdirs();
		
		int dim  = Math.max(height, width);
		int dimX = 256*width/dim;
		int dimY = 256*height/dim;



		for (int z = 0; z<=maxZoom; z++){
			File pZ = new File(p.toString() + File.separator+Integer.toString(z));
			double N = Math.pow(2, z);
			//create zoom directory
			pZ.mkdir();


			for (int x=0; x<Math.pow(2, z); x++){

				File pX = new File(pZ.toString() + File.separator +  Integer.toString(x) );
				//create X directory
				pX.mkdir();


				for (int y=0;y<Math.pow(2,z); y++){
					ip.setRoi((int) Math.floor(x*width/N), (int) Math.floor(y*height/N), (int) Math.floor(width/N), (int) Math.floor(height/N));
					ImageProcessor cropped=ip.resize(dimX,dimY,true);
					ImagePlus toSave = new ij.ImagePlus(Integer.toString(y),cropped);
					new FileSaver(toSave).saveAsPng(pX.toString()+File.separator + Integer.toString(y)+".png");
					IJ.showStatus("Zoom:"+z+" X:"+x+" Y:"+y);
					IJ.showProgress(y,(int) Math.pow(2,z));
				}
			}

		}

	}

	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("Map creator options");

		gd.addStringField("map name:", "map");
		gd.addNumericField("pixel tiles dimension", 256, 0);
		gd.addSlider("Maximum zoom", 1,20,5);
		gd.addSlider("slice to be used", 1,numbSlice,1);
		gd.addCheckbox("Use all slice", false);
		gd.addCheckbox("create basic global config", false);
		gd.addMessage("next the user is asked to choose a directory where the map directory will be created");
		gd.showDialog();

		if (gd.wasCanceled())
			return false;

		mapName=gd.getNextString();
		tileDim=(int) gd.getNextNumber();
		maxZoom=(int) gd.getNextNumber();
		slice=(int) gd.getNextNumber();
		useAll = (boolean) gd.getNextBoolean();
		global = (boolean) gd.getNextBoolean();
		directory = new DirectoryChooser("choose a directory to save the map").getDirectory();
		return true;
	}
}
