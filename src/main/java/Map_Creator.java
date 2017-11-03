
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
	private String mapName = "map";
	private float tileDim = 256;
	private int maxZoom = 5;
	private int slice = 1;
	private boolean useAll;
	private String directory = IJ.getDir("home");
	private Calibration cal;
	public String urlTemplate;
	public File configFile, configTilesFile;

	private int numExpectedTiles;
	private int numCreatedTiles = 0;

	public void run(String arg) {
		image = WindowManager.getCurrentImage();
		if (image == null) {
			IJ.error("You need to open an image first.");
			return;
		}
		width = image.getWidth();
		height = image.getHeight();
		numbSlice = image.getStackSize();
		cal = image.getCalibration();

		if (showDialog()) {
			//File p = new File(directory + File.separator + mapName);

			// create the map directory
			//p.mkdirs();

			if (useAll) {
				numExpectedTiles = numExpectedTiles(numbSlice, maxZoom);
				for (int s = 1; s <= numbSlice; s++) {
					createMap2d(s);
				}
			} else {
				numExpectedTiles = numExpectedTiles(1, maxZoom);
				createMap2d(slice);
			}
			if (useAll) {
				urlTemplate = "{level}/{z}/{x}/{y}.png";
			} else {
				urlTemplate = "{z}/{x}/{y}.png";
			}
			buildConfigurationFile();

		}
	}

	private void buildConfigurationFile() {
		try {
			configFile = new File(directory + File.separator + mapName + File.separator + mapName + ".json");
			double x_c = (cal.getX(width));
			double y_c = (cal.getY(width));
			double z_c = (cal.getZ(numbSlice));
			String unit = cal.getUnit();
			JSONObject obj = new JSONObject();
			JSONObject options = new JSONObject();
			obj.put("name", mapName);
			obj.put("type", "tileLayer");
			if (useAll) {
				obj.put("multiLevel", true);
				options.put("minLevel", 1);
				options.put("maxLevel", numbSlice);
			}
			JSONArray tileSize = new JSONArray();
			int dim = Math.max(width, height);
			tileSize.add(tileDim * (float)width / dim);
			tileSize.add(tileDim * (float)height / dim);
			options.put("tileSize", tileSize);
			options.put("date", "" + (new Date()));
			options.put("maxNativeZoom", maxZoom);
            options.put("minNativeZoom", 0);            
			JSONArray bounds = new JSONArray();
			JSONArray b0 = new JSONArray();
			JSONArray b1 = new JSONArray();
			b0.add(-tileDim);
			b0.add(0);
			b1.add(0);
			b1.add(tileDim);
			bounds.add(b0);
			bounds.add(b1);
			options.put("bounds", bounds);
			options.put("nSlice", numbSlice);
			options.put("sizeCal", Math.max(x_c, y_c));
			options.put("depthCal", z_c);
			options.put("unitCal", unit);
			obj.put("url", "" + urlTemplate);
			obj.put("baseLayer", true);
			obj.put("options", options);

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(configFile, false)));
			out.println(obj.toJSONString());
			out.close();

		} catch (IOException e) {

		}
	}

	public void createMap2d(int slice) {
		image.setSlice(slice);
		ip = image.getProcessor();
		File p;
		if (useAll) {
			p = new File(directory + File.separator + mapName  + File.separator + slice);
		} else {
			p = new File(directory + File.separator + mapName);
		}

		// create the map directory
		p.mkdirs();

		int dim = Math.max(height, width);
		int dimX = 256 * width / dim;
		int dimY = 256 * height / dim;

		for (int z = 0; z <= maxZoom; z++) {
			File pZ = new File(p.toString() + File.separator + Integer.toString(z));
			double N = Math.pow(2, z);
			// create zoom directory
			pZ.mkdir();

			for (int x = 0; x < Math.pow(2, z); x++) {

				File pX = new File(pZ.toString() + File.separator + Integer.toString(x));
				// create X directory
				pX.mkdir();

				for (int y = 0; y < Math.pow(2, z); y++) {
					ip.setRoi((int) Math.floor(x * width / N), (int) Math.floor(y * height / N),
							(int) Math.floor(width / N), (int) Math.floor(height / N));
					ImageProcessor cropped = ip.resize(dimX, dimY, true);
					ImagePlus toSave = new ij.ImagePlus(Integer.toString(y), cropped);
					new FileSaver(toSave).saveAsPng(pX.toString() + File.separator + Integer.toString(y) + ".png");
					IJ.showStatus("Zoom:" + z + " X:" + x + " Y:" + y);
					IJ.showProgress(y, (int) Math.pow(2, z));
					numCreatedTiles++;
					IJ.log(numCreatedTiles + "/" + numExpectedTiles);
				}
			}

		}

	}

	private boolean showDialog() {
		GenericDialog gd = new GenericDialog("Map creator options");

		gd.addStringField("map name:", "map");
		gd.addNumericField("pixel tiles dimension", 256, 0);
		gd.addSlider("Maximum zoom", 1, 20, 5);
		gd.addSlider("slice to be used", 1, numbSlice, 1);
		gd.addCheckbox("Use all slice", false);
		gd.addMessage("next the user is asked to choose a directory where the map directory will be created");
		gd.showDialog();

		if (gd.wasCanceled())
			return false;

		mapName = gd.getNextString();
		tileDim = (int) gd.getNextNumber();
		maxZoom = (int) gd.getNextNumber();
		slice = (int) gd.getNextNumber();
		useAll = (boolean) gd.getNextBoolean();
		directory = new DirectoryChooser("choose a directory to save the map").getDirectory();
		return true;
	}

	private int numExpectedTiles(int numSlices, int zoom) {
		int numExpectedTiles = 0;

		for (int i = 0; i <= zoom; i++) {
			numExpectedTiles += Math.pow(Math.pow(2, i), 2);
		}

		numExpectedTiles *= numSlices;
		return numExpectedTiles;
	}
}
