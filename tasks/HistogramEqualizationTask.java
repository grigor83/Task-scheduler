package tasks;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class HistogramEqualizationTask extends MyTask {
	public final static int MAX_GREY_LEVEL = 256;
	private final static String GRAY_FILE="grayImage.jpeg", SHARPENED_FILE="sharpened_image.jpeg";
	private String imageName;
	
	public HistogramEqualizationTask(String name) {
		super(name);
	}
	
	public HistogramEqualizationTask(String name, String startingTime) {
		super(name, startingTime);
	}
    
	public HistogramEqualizationTask(String name, String startingDateTime, String deadLine) {
		super(name, startingDateTime, deadLine);
	}
	
	public HistogramEqualizationTask(String name, int... values) {
		super(name, values);
	}   
	
	public HistogramEqualizationTask(String name, String startingDateTime, int... values) {
		super(name, startingDateTime, values);
	}  
	
	public HistogramEqualizationTask(String name, String startingDateTime, String deadLine, int... values) {
		super(name, startingDateTime, deadLine, values);
	} 
	
	public void equalize(File file) {
		BufferedImage rgbImage = loadRGBimage(file);
		BufferedImage grayImage = convertToGrayscale(rgbImage);
		BufferedImage equalizedGrayImage = equalizeHistogram(grayImage);
		saveNewImage(combineImages(rgbImage, equalizedGrayImage));
	}

	private BufferedImage loadRGBimage(File inputFile) {
		BufferedImage image=null;
		try {
			image = ImageIO.read(inputFile);
			imageName = "sharpened_" + inputFile.getName();
			System.out.println("RGB image has been loaded successfully!");
		} 
		catch (IOException e) {
			System.out.println("RGB image has not been loaded!");
			System.exit(1);
		};
       
		return image;
	}

	private BufferedImage convertToGrayscale(BufferedImage rgbImage) {
		int width = rgbImage.getWidth();
        int height = rgbImage.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = grayImage.createGraphics();
		g.drawImage(rgbImage, 0, 0, null);
		g.dispose();
        
        try {
			ImageIO.write(grayImage, "jpeg", new File(GRAY_FILE));
	        System.out.println("Image converted to grayscale successfully and saved.");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

        return grayImage;
	}
	
	private BufferedImage equalizeHistogram(BufferedImage grayImage) {
		int width = grayImage.getWidth();
        int height = grayImage.getHeight();
		// Perform histogram equalization ensures that the intensity values in the gray scale image are evenly distributed.
		float totalPixels = width * height;
		int[] histogram = new int[MAX_GREY_LEVEL];
				
		// Calculate the histogram, which represents the frequency distribution of intensity values in the image. 
		// It shows the number of pixels at each intensity level.
		for (int i=0; i < width; i++)
			for(int j=0; j < height; j++)
				histogram[grayImage.getRaster().getSample(i, j, 0)]++;	
				
		// Calculate cumulative distribution function (CDF). It represents the cumulative probability of occurrence for each intensity level.
		int[] cdf = new int[MAX_GREY_LEVEL];
		cdf[0] = histogram[0];
		for (int i = 1; i < MAX_GREY_LEVEL; i++)
			cdf[i] = cdf[i - 1] + histogram[i];
				
		// Normalize CDF
		float[] normalizedCDF = new float[MAX_GREY_LEVEL];
		for (int i = 0; i < MAX_GREY_LEVEL; i++) 
			normalizedCDF[i] = cdf[i] / totalPixels;
				
		// Create new, equalized histogram
		for (int i = 0; i < MAX_GREY_LEVEL; i++)
			histogram[i] = Math.round(normalizedCDF[i] * (MAX_GREY_LEVEL-1));
				
		// Perform equalization of the grayImage: mapping each original intensity value to its corresponding normalized CDF value.
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				int newValue = histogram[grayImage.getRaster().getSample(x, y, 0)];
			    grayImage.getRaster().setSample(x, y, 0, newValue);
			}
				
		try {
			ImageIO.write(grayImage, "jpeg", new File(SHARPENED_FILE));
			System.out.println("Image equalized and saved successfully.");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		return grayImage;
	}

	private BufferedImage combineImages(BufferedImage rgbImage, BufferedImage grayscaleImage) {
        int width = rgbImage.getWidth();
        int height = rgbImage.getHeight();
        BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = combinedImage.createGraphics();
        g2d.drawImage(rgbImage, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.drawImage(grayscaleImage, 0, 0, null);
        g2d.dispose();
        
        return combinedImage;
    }
	
	private void saveNewImage(BufferedImage combinedImage) {
		try {
			ImageIO.write(combinedImage, "jpeg", new File(outputFolder + File.separator + imageName));
			System.out.println("Sharpened image has been painted.");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	@Override
	public void logic() {
		if (i == total) {
        	System.out.println(name+" complete");
    		pause = true;
    		terminated = true;
    		return;
    	}
		
		File image = getInputFiles().get(i);
    	equalize(image);
	}

}
