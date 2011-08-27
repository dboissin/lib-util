package fr.dboissin.util.gwt.html5.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public final class BlobDetector {

	public static class Blob {

		private int x1;
		private int x2;
		private int y1;
		private int y2;
		private int mass;
		
		public Blob() {
		}
		
		public Blob(int x1, int x2, int y1, int y2, int mass) {
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			this.mass = mass;
		}
		
		@Override
		public String toString() {
			int [] p = getFakeCenter();
			return "Blob [x1=" + x1 + ", x2=" + x2 + ", y1=" + y1 + ", y2="
					+ y2 + ", mass=" + mass + ", fakeCenter=" + p[0] + "," + p[1] + "]";
		}

		public int getX1() {
			return x1;
		}

		public void setX1(int x1) {
			this.x1 = x1;
		}

		public int getX2() {
			return x2;
		}

		public void setX2(int x2) {
			this.x2 = x2;
		}

		public int getY1() {
			return y1;
		}

		public void setY1(int y1) {
			this.y1 = y1;
		}

		public int getY2() {
			return y2;
		}

		public void setY2(int y2) {
			this.y2 = y2;
		}

		public int getMass() {
			return mass;
		}

		public void setMass(int mass) {
			this.mass = mass;
		}
		
		public int [] getCenter() {
			throw new RuntimeException("Not implemented yet.");
		}

		public int [] getFakeCenter() {
			int [] point = {(x2 - x1) / 2 + x1, (y2 - y1) / 2 + y1};
			return point;
		}
	}

	private static class Label {
		private int a;
		private int b;
		private int c;
		private int d;
		private int x;
		private int[] buffer;
		private List<Integer> table;
		private List<Blob> blobs;
		private int num;
		private int width;

		Label(int width, int height) {
			a = -width - 1;
			b = -width;
			c = -width + 1;
			d = -1;
			x = 0;
			num = 0;
			buffer = new int[width * height];
			table = new ArrayList<Integer>();
			table.add(0);
			blobs = new ArrayList<BlobDetector.Blob>();
			blobs.add(new Blob());
			this.width = width;
		}

		void incrKernel() {
			a++;
			b++;
			c++;
			d++;
			x++;
		}

		void check() {
			int min = Integer.MAX_VALUE;
			int [] t = {a, b, c, d};
			for (int idx: t) {
				if (idx >= 0 &&  buffer[idx] > 0 && (buffer[idx]&0xFF) < min) {
					min = buffer[idx];
				}
			}
			int posX = x % width;
			int posY = x / width;
			if (min == Integer.MAX_VALUE) {
				num++;
				buffer[x] = num;
				table.add(num);
				blobs.add(new Blob(posX, posX, posY, posY, 1));
			} else {
				buffer[x] = min;
				Blob blob = blobs.get(min);
				blob.setY2(posY);
				if (blob.getX1() > posX) {
					blob.setX1(posX);
				}
				if (blob.getX2() < posX) {
					blob.setX2(posX);
				}
				blob.setMass(blob.getMass() + 1);
				for (int idx: t) {
					if (idx >= 0 && buffer[idx] > 0) {
						table.set(buffer[idx], min);
					}
				}
			}
		}

		void updateTable() {
			for (int i = 0; i < table.size(); i++) {
				int tmp = table.get(i);
				if (tmp != i) {
					table.set(i, table.get(tmp));
				}
			}
		}
		
		List<Blob> updatedBlobs() {
			List<Blob> tmp = new ArrayList<Blob>();
			HashMap<Integer, List<Integer>> matchVals = new HashMap<Integer, List<Integer>>();
			for (int i = 1; i < table.size(); i++) {
				Integer val = table.get(i);
				if (matchVals.containsKey(val)) {
					matchVals.get(val).add(i);
				} else {
					List<Integer> l = new ArrayList<Integer>();
					l.add(i);
					matchVals.put(val, l);
				}
			}
			for (Entry<Integer, List<Integer>> e: matchVals.entrySet()) {
				Blob b = new Blob();
				b.x1 = Integer.MAX_VALUE;
				b.y1 = Integer.MAX_VALUE;
				for (Integer i: e.getValue()) {
					Blob bi = blobs.get(i);
					if (b.x1 > bi.x1) {
						b.x1 = bi.x1;
					}
					if (b.x2 < bi.x2) {
						b.x2 = bi.x2;
					}
					if (b.y1 > bi.y1) {
						b.y1 = bi.y1;
					}
					if (b.y2 < bi.y2) {
						b.y2 = bi.y2;
					}
					b.mass += bi.mass;
				}
				tmp.add(b);
			}
			return tmp;
		}
	}

	public static List<BlobDetector.Blob> detectBlobs(byte[] image, int width, int height) {
		final Label label = new Label(width, height);
		byte[] tmp = thresholding(image, width, height);

		for (int i = 0; i < height * width; i++) {
			if (!(tmp[label.x] != 0)) { // if is blob
				label.check();
			}
			label.incrKernel();
		}

		label.updateTable();
		
		return label.updatedBlobs();
	}	


	/**
	 * Balanced histogram thresholding method
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return binary image
	 */
	public static byte[] thresholding(byte[] image, int width, int height) {
		byte[] tmp = image;
		if(!isValideImage(image, width, height)) {
			tmp = grayscale(image, width, height);
		}

		int threshold = averageHistogram(histogram(tmp, width, height));
		byte [] binaryImage = new byte[width * height];
		for (int i = 0; i < tmp.length; i++) {
			binaryImage[i] = ((tmp[i]&0xFF) < threshold) ? 0 : (byte) 0xFF;
		}
		return binaryImage;
	}

	public static int[] histogram(byte[] image, int width, int height) {
		byte[] tmp = image;
		if(!isValideImage(image, width, height)) {
			tmp = grayscale(image, width, height);
		}

		int [] histogram = new int[256];
		for (byte b: tmp) {
			histogram[b&0xFF]++;
		}

		return histogram;
	}

	public static byte[] grayscale(byte[] image, int width, int height) {
		final int pixels = width * height;
		final int dataSize = image.length; 
		int incr = 3;
		final float r = 0.3f;
		final float g = 0.59f;
		final float b = 0.11f;

		if (dataSize == pixels) {
			return image;
		} else if (dataSize == pixels * 4) {
			// image with an alpha value
			incr++;
		} else if (dataSize != pixels * 3) {
			throw new RuntimeException("Unexpected image data size.");
		}

		byte [] grayscaleImage = new byte[pixels];
		for (int i = 0, j = 0; i < dataSize; i += incr, j++) {
			grayscaleImage[j] = (byte) ((float) (image[i]&0xFF) * r +
					(float) (image[i+1]&0xFF) * g + (float) (image[i+2]&0xFF) * b);
		}

		return grayscaleImage;
	}

	private static int averageHistogram(int [] histogram) {
		int value = 0;
		int pixels = 0;
		int res = 0;

		for (int n: histogram) {
			res += n*value;
			pixels += n;
			value++;
		}

		return res / pixels; 
	}

	private static boolean isValideImage(byte[] img, int width, int height) {
		int pixels = width * height;
		int dataSize = img.length;
		if (dataSize == pixels) {
			return true;
		} else if ((dataSize == pixels * 3) || (dataSize == pixels * 4)) {
			return false;
		}
		throw new RuntimeException("Unexpected image data size.");
	}

}
