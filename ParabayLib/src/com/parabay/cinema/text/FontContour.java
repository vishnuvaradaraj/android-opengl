package com.parabay.cinema.text;

import java.util.Vector;


public class FontContour {

	private int nextIndex = 0;
	
	private Vector<FontPoint> contourPoints = new Vector<FontPoint>();
	private Vector<FontPoint> points = new Vector<FontPoint>();
	private Vector<Integer> indices = new Vector<Integer>();
	
	public FontContour(int counter) {
		this.nextIndex = counter;
	}

	public void addContourPoint(FontPoint fp) {

		this.contourPoints.add(fp);
		this.addPoint(fp);
	}
	
	public void addPoint(FontPoint fp) {

		fp.index = this.nextIndex++;
		this.points.add(fp);
	}
	
	public void addIndex(int n) {

		this.indices.add(n);
	}

    Vector2D edgeNormal(int vertexNum) {
    	    	
    	FontPoint fp1 = this.getContourPoint(vertexNum); 
    	FontPoint fp2 = this.getContourPoint(vertexNum + 1);  
    	
    	return new Vector2D(fp1, fp2).normal().normalize();
    }

    Vector2D vertexNormal(int vertexNum){

    	FontPoint fp1 = this.getContourPoint(vertexNum - 1);  
    	FontPoint fp2 = this.getContourPoint(vertexNum); 
    	FontPoint fp3 = this.getContourPoint(vertexNum + 1);  

    	Vector2D v1 = new Vector2D(fp2, fp1);
    	Vector2D v2 = new Vector2D(fp2, fp3);
    	
    	Vector2D v3 = v1.normal().normalize();
    	Vector2D v4 = v2.normal().normalize().mul(-1.0f);

    	Vector2D v6 = v3.add(v4);
    	Vector2D v7 = v6.normalize().mul(-1.0f);

    	return v7;
    }

    float vertexCosAngle(int vertexNum) {
    	
    	FontPoint fp1 = this.getContourPoint(vertexNum - 1);  
    	FontPoint fp2 = this.getContourPoint(vertexNum); 
    	FontPoint fp3 = this.getContourPoint(vertexNum + 1);  

    	Vector2D v1 = new Vector2D(fp2, fp1);
    	Vector2D v2 = new Vector2D(fp2, fp3);
    	
    	return v1.cosAngle(v2);
    }
    
	public FontPoint getContourPoint(int index) {
		
		if (index < 0)
	    {
	        index = this.getContourPoints().size() - (-index % this.getContourPoints().size());
	    }
		return this.contourPoints.get(index % this.getContourPoints().size());
	}

	public Vector<FontPoint> getContourPoints() {
		return contourPoints;
	}

	public FontPoint getPoint(int index) {
		
		if (index < 0)
	    {
	        index = this.getPoints().size() - (-index % this.getPoints().size());
	    }
		return this.points.get(index % this.getPoints().size());
	}
	
	public Vector<FontPoint> getPoints() {
		return points;
	}

	public Vector<Integer> getIndices() {
		return indices;
	}

	public int getNextIndex() {
		return nextIndex;
	}
}
