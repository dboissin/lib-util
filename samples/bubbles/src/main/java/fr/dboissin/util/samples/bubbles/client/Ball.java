package fr.dboissin.util.samples.bubbles.client;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;


public class Ball extends SpringObject {
	  CssColor color;
	  double posZ, velZ, goalZ;
	  double radius;
	  Vector startPos;
	  double startPosZ;
	  double startRadius;

	  public Ball(Vector start, double startPosZ, double radius, CssColor color) {
	    super(start);
	    this.color = color;
	    this.posZ = startPosZ;
	    this.velZ = 0;
	    this.goalZ = startPosZ;
	    this.radius = radius;
	    this.startPos = new Vector(start);
	    this.startPosZ = startPosZ;
	    this.startRadius = radius;
	  }
	  
	  public Ball(double x, double y, double z, double radius, String color) {
	    this(new Vector(x, y), z, radius, CssColor.make(color));
	  }
	  
	  public void update() {
	    super.update();
	    
	    Vector dh = Vector.sub(startPos, pos);
	    double dist = dh.mag();
	    goalZ = dist / 100.0 + 1.0;
	    double dgZ = goalZ - posZ;
	    double aZ = dgZ * springStrength;
	    velZ += aZ;
	    velZ *= friction;
	    posZ += velZ;
	    
	    radius = startRadius * posZ;
	    radius = radius < 1 ? 1 : radius;
	  }
	  
	  public void draw(Context2d context) {
	    context.setFillStyle(color);
	    context.beginPath();
	    context.arc(pos.x, pos.y, radius, 0, Math.PI * 2.0, true);
	    context.closePath();
	    context.fill();
	  }
	}
