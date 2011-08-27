package fr.dboissin.util.gwt.html5.client;

import static org.fest.assertions.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.dboissin.util.gwt.html5.client.BlobDetector.Blob;

public class BlobDetectorTest {
	
	private static final String IMG = "blob.jpg"; 
	private static byte[] testImage;
	private static int testWidth;
	private static int testHeight;

	@BeforeClass
	public static void loadTestImage() throws IOException {
		InputStream in = BlobDetectorTest.class.getClassLoader().getResourceAsStream(IMG);
		BufferedImage buffImage = ImageIO.read(in);
		testWidth = buffImage.getWidth();
		testHeight = buffImage.getHeight();
		DataBuffer dataBuff = buffImage.getData().getDataBuffer();
		if (dataBuff.getDataType() != DataBuffer.TYPE_BYTE) {
			throw new RuntimeException("Bad data type.");
		}
		testImage = ((DataBufferByte) dataBuff).getData();
	}
	
	@Test
	public void testDetectBlobs() {
		List<BlobDetector.Blob> blobs = BlobDetector.detectBlobs(testImage, testWidth, testHeight);
//		System.out.println(blobs);		
		assertThat(blobs).hasSize(3);
	}
	
	@Test
	public void testThresholding() {
		byte [] binaryImg = BlobDetector.thresholding(testImage, testWidth, testHeight);
		assertThat(binaryImg).hasSize(testWidth*testHeight).containsOnly((byte) 255, (byte) 0);
	}
	
	@Test
	public void testHistogram() {
		int [] histogram = BlobDetector.histogram(testImage, testWidth, testHeight);
		assertThat(histogram).hasSize(256);
	}
	
	@Test
	public void testGrayscale() {
		byte[] grayscaleImage = BlobDetector.grayscale(testImage, testWidth, testHeight);
		assertThat(grayscaleImage).hasSize(testImage.length / 3);
	}
	
	@Test(expected=RuntimeException.class)
	public void testBlobCenter() {
		Blob blop = new BlobDetector.Blob(100, 300, 300, 400, 42);
		blop.getCenter();
	}

	@Test
	public void testBlobFakeCenter() {
		Blob blop = new BlobDetector.Blob(100, 300, 300, 400, 42);
		int [] point = blop.getFakeCenter();
		assertThat(point).hasSize(2).containsOnly(200, 350);
	}
}
