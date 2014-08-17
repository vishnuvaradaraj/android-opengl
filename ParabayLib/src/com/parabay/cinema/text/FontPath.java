package com.parabay.cinema.text;

import java.util.List;

import com.parabay.cinema.text.ttf.GlyphData;

import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;

public class FontPath {

	public static final String TAG = "FontPath";
	
	private GlyphData glyphData;
	
	private Path path;
	private PointF lastPoint;
	
	private FontTesselator fontTesselator;
	private FontCurvePoints curve = new FontCurvePoints();
	
	public FontPath(GlyphData glyphData) {
		
		this.glyphData = glyphData;
		
		this.lastPoint = new PointF();
		this.path = new Path();   
		
		this.fontTesselator = new FontTesselator(glyphData);
	}
	
	public void quadTo (float x1, float y1, float x2, float y2) {
		
		//Log.i(TAG, String.format("quadTo: %f, %f, %f, %f", x1, y1, x2, y2));
		
		path.quadTo(x1, y1, x2, y2);
		fontTesselator.quadTo(x1, y1, x2, y2);
		
		this.lastPoint.x = x2;
		this.lastPoint.y = y2;

	}
	
	public void lineTo (float x, float y) {
		
		//Log.i(TAG, String.format("lineTo: %f, %f", x, y));
		
		path.lineTo(x, y);
		fontTesselator.lineTo(x, y);
		
		this.lastPoint.x = x;
		this.lastPoint.y = y;

	}
	
	public void moveTo (float x, float y) {
				
		//Log.i(TAG, String.format("moveTo: %f, %f", x, y));
		        
		path.moveTo(x, y);
		fontTesselator.moveTo(x, y);
		
		this.lastPoint.x = x;
		this.lastPoint.y = y;

	}

	public void close () {
		
		//Log.i(TAG, String.format("close"));
		
		path.close();
		fontTesselator.close();
	}
	
	public void shutdown() {
		//path
		fontTesselator.shutdown();
	}
	
	public Path getPath() {
		
		return path;
	}

	public FontTesselator getTesselator() {
		
		return this.fontTesselator;
	}

    public PointF getLastPoint() {
		return lastPoint;
	}

}
