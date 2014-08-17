package com.parabay.cinema.text;

import java.util.List;
import java.util.Vector;

import android.graphics.PointF;
import android.util.Log;

public class FontCurvePoints {

	public static final String TAG = "FontCurvePoints";
	
	public static final float QUAD_STEP_SIZE = 0.2f;
	
	private List<PointF> pointList = new Vector<PointF>();
	private float stepSize = QUAD_STEP_SIZE;
	private float lastStep = 0;

	public List<PointF> getPoints() {
		return this.pointList;
	}
	
	public List<PointF> quad(PointF p1, PointF p2, PointF p3)
	{
		this.pointList.clear();
		
		int i = (int)(this.lastStep / this.stepSize);
		for (; i <= (1.0f / this.stepSize); i++)
		{
			PointF r1, r2;
			float t = i * this.stepSize;

			r1 = this.evalCasteljauPoint(p1, p2, t);
			r2 = this.evalCasteljauPoint(p2, p3, t);
			r1 = this.evalCasteljauPoint(r1, r2, t);
			
			this.addPoint(r1);
		}
		this.lastStep = i*stepSize-1.0f;
		
		return this.pointList;
	}
	
	private PointF evalCasteljauPoint(PointF p1, PointF p2, float t) { 
	    	
	    return new PointF(
	    		(float) ((1.0 - t) * p1.x + t * p2.x), 
	    		(float) ((1.0 - t) * p1.y + t * p2.y));
	}

	protected void addPoint(PointF point)
	{	
		PointF lastPoint = null;
		if (!this.pointList.isEmpty()) {
			lastPoint = (PointF)this.pointList.get(this.pointList.size() - 1);
		}
		
		if ((null == lastPoint) || (lastPoint.x != point.x || lastPoint.y != point.y))
		{
			//Log.i(TAG, String.format("POINT: %f, %f", point.x, point.y));
			this.pointList.add(point);
		}
	}
}
