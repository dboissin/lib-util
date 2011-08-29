package fr.dboissin.util.samples.bubbles.client;

import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;

import fr.dboissin.util.gwt.html5.client.BlobDetector.Blob;

public class BallGroup {

	final Ball[] balls;

	public BallGroup(List<Blob> blobs) {
		balls = new Ball[blobs.size()];

		int i = 0;
		for (Blob blob: blobs) {
			int [] center = blob.getFakeCenter();
			Ball ball = new Ball(center[0], center[1], 0, blob.getMass()/15, "#00CC00");
			balls[i++] = ball;
		}
	}

	public void update(double mouseX, double mouseY) {
		Vector d = new Vector(0, 0);
		for (int i = balls.length - 1; i >= 0; i--) {
			Ball ball = balls[i];
			d.x = mouseX - ball.pos.x;
			d.y = mouseY - ball.pos.y;
			if (d.magSquared() < 100*100) {
				ball.goal = Vector.sub(ball.pos, d);
			} else {
				ball.goal.set(ball.startPos);
			}

			ball.update();
		}
	}

	public void draw(Context2d context) {
		for(int i = balls.length - 1; i >= 0; i--) {
			balls[i].draw(context);
		}
	}
}
