package fr.dboissin.util.samples.bubbles.client;

import java.util.List;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import fr.dboissin.util.gwt.html5.client.BlobDetector;
import fr.dboissin.util.gwt.html5.client.BlobDetector.Blob;


public class Bubbles implements EntryPoint {
	
	private Canvas canvas;

	public void onModuleLoad() {
	    canvas = Canvas.createIfSupported();
	    if (canvas == null) {
	      RootPanel.get().add(new Label("Your browser does not support the HTML5 Canvas."));
	      return;
	    }
	    
		loadImage("img/test.jpg");
		
		RootPanel.get().add(canvas);
	}

	private void loadImage(String path) {
		final Image img = new Image(path);
		RootPanel.get().add(img);
		//img.setVisible(false);
		
		img.addLoadHandler(new LoadHandler(){
			@Override
			public void onLoad(LoadEvent event) {
				Canvas tmp = Canvas.createIfSupported();
				Context2d context = tmp.getContext2d();
				ImageElement imgEl = ImageElement.as(img.getElement()); 
				context.drawImage(imgEl, 0d, 0d);
				ImageData imgData = context.getImageData(0, 0, (double) img.getWidth(), (double) img.getHeight());
				init(imgData);
			}
		});
	}

	private void init(ImageData imgData) {
		CanvasPixelArray cpa = imgData.getData();
		byte [] data = new byte[cpa.getLength()];
		RootPanel.get().add(new Label(">>>" + cpa.getLength() + " : " +imgData.getWidth() + "," + imgData.getHeight()));
		for (int i = 0; i < cpa.getLength(); i++) {
			data[i] = (byte) cpa.get(i);
		}
		List<Blob> blobs = BlobDetector.detectBlobs(data, imgData.getWidth(), imgData.getHeight());
		draw(blobs, imgData.getWidth(), imgData.getHeight());
	}
	
	private void draw(List<Blob> blobs, int width, int height) {
		canvas.setWidth(width + "px");
	    canvas.setHeight(height + "px");
	    canvas.setCoordinateSpaceWidth(width);
	    canvas.setCoordinateSpaceHeight(height);
	    
	    Context2d context = canvas.getContext2d();
	    CssColor green = CssColor.make("#00CC00");
	    for (Blob b: blobs) {
	    	int [] pos = b.getFakeCenter();
	    	RootPanel.get().add(new Label(b.toString()));
		    context.setFillStyle(green);
		    context.beginPath();
		    context.arc(pos[0], pos[1], 9, 0, Math.PI * 2.0, true);
		    context.closePath();
		    context.fill();
	    }
	}

}
