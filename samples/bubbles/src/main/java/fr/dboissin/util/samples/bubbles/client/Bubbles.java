package fr.dboissin.util.samples.bubbles.client;

import java.util.List;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.GestureStartEvent;
import com.google.gwt.event.dom.client.GestureStartHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import fr.dboissin.util.gwt.html5.client.BlobDetector;
import fr.dboissin.util.gwt.html5.client.BlobDetector.Blob;


public class Bubbles implements EntryPoint {

	private Canvas canvas;
	private Canvas txtCanvas;
	private Context2d context;
	private Context2d txtContext;
	private BallGroup ballGroup;

	private final CssColor redrawColor = CssColor.make("rgba(255,255,255,0.6)");
	private static final CssColor GREEN = CssColor.make("#00CC00");
	private static final CssColor WHITE = CssColor.make("#FFFFFF");

	private int mouseX, mouseY, width, height;
	private static final int REFRESH_RATE = 25;
	private boolean loaded = false;

	private TextBox tb;

	public void onModuleLoad() {
		canvas = Canvas.createIfSupported();
		if (canvas == null) {
			RootPanel.get().add(new Label("Your browser does not support the HTML5 Canvas."));
			return;
		}
		context = canvas.getContext2d();
		txtCanvas = Canvas.createIfSupported();
		txtContext = txtCanvas.getContext2d();
		txtCanvas.setHeight("200px");
		txtCanvas.setCoordinateSpaceHeight(220);

		//loadImage("img/test.jpg");

		tb = new TextBox();
		VerticalPanel vp = new VerticalPanel();
		vp.add(tb);
		vp.add(txtCanvas);
		vp.add(canvas);
		RootPanel.get().add(vp);

		initHandlers();

		final Timer timer = new Timer() {
			@Override
			public void run() {
				if (loaded) {
					doUpdate();
				}
			}
		};
		timer.scheduleRepeating(REFRESH_RATE);
	}

	private void loadImage(String path) {
		final Image img = new Image(path);
		RootPanel.get().add(img);
		img.setVisible(false);

		img.addLoadHandler(new LoadHandler(){
			@Override
			public void onLoad(LoadEvent event) {
				Canvas tmp = Canvas.createIfSupported();
				Context2d tmpContext = tmp.getContext2d();
				ImageElement imgEl = ImageElement.as(img.getElement()); 
				tmpContext.drawImage(imgEl, 0d, 0d);
				width =  img.getWidth();
				height = img.getHeight();
				ImageData imgData = tmpContext.getImageData(0, 0, width, height);
				init(imgData);
			}
		});
	}

	private void init(ImageData imgData) {
		CanvasPixelArray cpa = imgData.getData();
		byte [] data = new byte[cpa.getLength()];
		for (int i = 0; i < cpa.getLength(); i++) {
			data[i] = (byte) cpa.get(i);
		}
		List<Blob> blobs = BlobDetector.detectBlobs(data, imgData.getWidth(), imgData.getHeight());

		ballGroup = new BallGroup(blobs);

		canvas.setWidth(width + "px");
		canvas.setHeight(height + "px");
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);

		loaded = true;
	}

	private void doUpdate() {
		context.setFillStyle(redrawColor);
		context.fillRect(0, 0, width, height);
		ballGroup.update(mouseX, mouseY);
		ballGroup.draw(context);
	}

	private void initHandlers() {
		canvas.addMouseMoveHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent event) {
				mouseX = event.getRelativeX(canvas.getElement());
				mouseY = event.getRelativeY(canvas.getElement());
			}
		});

		canvas.addMouseOutHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				mouseX = -200;
				mouseY = -200;
			}
		});

		canvas.addTouchMoveHandler(new TouchMoveHandler() {
			public void onTouchMove(TouchMoveEvent event) {
				event.preventDefault();
				if (event.getTouches().length() > 0) {
					Touch touch = event.getTouches().get(0);
					mouseX = touch.getRelativeX(canvas.getElement());
					mouseY = touch.getRelativeY(canvas.getElement());
				}
				event.preventDefault();
			}
		});

		canvas.addTouchEndHandler(new TouchEndHandler() {
			public void onTouchEnd(TouchEndEvent event) {
				event.preventDefault();
				mouseX = -200;
				mouseY = -200;
			}
		});

		canvas.addGestureStartHandler(new GestureStartHandler() {
			public void onGestureStart(GestureStartEvent event) {
				event.preventDefault();
			}
		});

		tb.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				String txt = tb.getText();
				int tmpWidth = txt.length() * 120;
				txtCanvas.setWidth(tmpWidth + "px");
				txtCanvas.setCoordinateSpaceWidth(tmpWidth);
				txtContext.setFillStyle(WHITE);
				txtContext.fillRect(0, 0, width, height);
				txtContext.setFillStyle(GREEN);
				txtContext.setFont("72pt superpoint");
				txtContext.fillText(txt, 20, 140);
			}
		});

		tb.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if (event.getNativeEvent().getCharCode() == KeyCodes.KEY_ENTER) {
					CanvasElement tmp =  txtContext.getCanvas(); 
					width = tmp.getWidth();
					height = tmp.getHeight();
					init(txtContext.getImageData(0, 0, width, height));
				}
			}
		});
	}
}
