package com.parabay.cinema.text;

import java.util.List;
import java.util.Vector;

import com.parabay.cinema.text.tess.PGLU;
import com.parabay.cinema.text.tess.PGLUtessellator;
import com.parabay.cinema.text.tess.PGLUtessellatorCallbackAdapter;
import com.parabay.cinema.text.ttf.BoundingBox;
import com.parabay.cinema.text.ttf.GlyphData;

import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.FillType;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.util.Log;

public class FontTesselator {

	public static final String TAG = "FontTesselator";

	private GlyphData glyphData;
	
	private FontCurvePoints curve;
	private PGLUtessellator tobj;
	private PointF lastPoint;
	private PointF firstPoint;
	private int pointIndex;

	private Path debugPath;
	private Path debugInput;

	private boolean inContour = false;
	private boolean inPolygon = false;

	private Vector<FontContour> contours = new Vector<FontContour>();
	private FontContour contour;
	private int nextIndex = 0;
	
	public FontTesselator(GlyphData glyphData) {

		this.glyphData = glyphData;
		
		this.curve = new FontCurvePoints();
		this.lastPoint = new PointF();
		this.firstPoint = new PointF();

		this.debugPath = new Path();
		this.debugInput = new Path();

		this.tobj = PGLU.gluNewTess();

		TessCallback tessCallback = new TessCallback();

		PGLU.gluTessProperty(tobj, PGLU.GLU_TESS_TOLERANCE, 0);
		PGLU.gluTessProperty(tobj, PGLU.GLU_TESS_WINDING_RULE,
				PGLU.GLU_TESS_WINDING_NONZERO);
		PGLU.gluTessNormal(tobj, 0.0f, 0.0f, 1.0f);

		PGLU.gluTessCallback(tobj, PGLU.GLU_TESS_VERTEX, tessCallback);
		PGLU.gluTessCallback(tobj, PGLU.GLU_TESS_BEGIN, tessCallback);
		PGLU.gluTessCallback(tobj, PGLU.GLU_TESS_END, tessCallback);
		PGLU.gluTessCallback(tobj, PGLU.GLU_TESS_ERROR, tessCallback);
		PGLU.gluTessCallback(tobj, PGLU.GLU_TESS_EDGE_FLAG_DATA, tessCallback);

		PGLU.gluTessBeginPolygon(tobj, null);
		this.inPolygon = true;
	}

	public void tessBeginContour() {
		
		this.contour = new FontContour(this.nextIndex);
		
		PGLU.gluTessBeginContour(tobj);
		this.inContour = true;
		
	}
	
	public void tessEndContour() {
		
		this.contours.add(this.contour);
		this.nextIndex = this.contour.getNextIndex();
		this.contour = null;
		
		PGLU.gluTessEndContour(tobj);
		this.inContour = false;
	}
	
	public void tessVertex(float x, float y) {
		
		
		BoundingBox bbox = this.glyphData.getBoundingBox();
		float minX = bbox.getLowerLeftX();
		float minY = bbox.getLowerLeftY();
		float maxX = bbox.getUpperRightX();
		float maxY = bbox.getUpperRightY();
		
		//Log.i(TAG, String.format("Bounds: %f %f %f %f", bbox.getLowerLeftX(), bbox.getLowerLeftY(), bbox.getUpperRightX(), bbox.getUpperRightY()));
		
		float ux = 1.0f / (maxX - minX);
	    float vy = 1.0f / (maxY - minY);
    
		FontPoint fp = new FontPoint(this.pointIndex++, x, y);
	    fp.u = (x - minX) * ux;
	    fp.v = 1.0f - (y - minY) * vy;
	    fp.parent = this.contour;

	    this.contour.addContourPoint(fp);
	    
		PGLU.gluTessVertex(tobj, fp.toDoubleArray(), 0, fp);

	}

	public void quadTo(float x1, float y1, float x2, float y2) {

		//Log.i(TAG, String.format("quadTo: %f, %f, %f, %f", x1, y1, x2, y2));

		PointF p2 = new PointF(x1, y1);
		PointF p3 = new PointF(x2, y2);

		// this.moveTo(lastPoint.x, lastPoint.y);
		List<PointF> pointList = this.curve.quad(this.lastPoint, p2, p3);
		for (PointF p : pointList) {
			//Log.i(TAG, String.format("TESS QUAD lineTo: %f, %f", p.x, p.y));
			this.lineTo(p.x, p.y);

			this.lastPoint.x = x2;
			this.lastPoint.y = y2;
		}
		// this.lineTo(x2, y2);

	}

	public void lineTo(float x, float y) {

		if (!this.inContour) {
			//Log.i(TAG, "TESS NEW CONTOUR FOUND...");

			this.tessBeginContour();
		}

		this.tessVertex(x, y);
		
		//Log.i(TAG, String.format("TESS lineTo: %f, %f", x, y));
		this.debugInput.lineTo(x, y);

		this.lastPoint.x = x;
		this.lastPoint.y = y;

	}

	public void moveTo(float x, float y) {

		if (!this.inContour) {
			//Log.i(TAG, "TESS NEW CONTOUR FOUND...");

			this.tessBeginContour();
		}

		this.tessVertex(x, y);

		//Log.i(TAG, String.format("TESS moveTo: %f, %f", x, y));
		this.debugInput.moveTo(x, y);

		this.firstPoint.x = x;
		this.firstPoint.y = y;

		this.lastPoint.x = x;
		this.lastPoint.y = y;

	}

	public void close() {

		this.lineTo(this.firstPoint.x, this.firstPoint.y);
		this.debugInput.close();
		if (this.inContour) {
			this.tessEndContour();

			//Log.i(TAG, String.format("TESS close:"));
		}

	}

	public void shutdown() {

		if (this.inPolygon) {
			PGLU.gluTessEndPolygon(tobj);
			this.inPolygon = false;
		}

		PGLU.gluDeleteTess(tobj);
	}

	public PointF getLastPoint() {
		return lastPoint;
	}

	public Path getDebugPath() {
		return debugPath;
	}

	public Path getDebugInput() {
		return this.debugInput;
	}

	public Vector<FontContour> getContours() {
		return this.contours;
	}

	public GlyphData getGlyphData() {
		return this.glyphData;
	}

	public class TessCallback extends PGLUtessellatorCallbackAdapter {

		private int typeOf;
		private FontPoint p1;
		private FontPoint p2;

		public TessCallback() {

		};

		public void begin(int type) {

			//Log.i(TAG, String.format("Begin: Type = %d", type));
			this.typeOf = type;

			this.p1 = null;
			this.p2 = null;
		}

		public void end() {

			//Log.i(TAG, String.format("End: Type = %d", typeOf));
		}

		public void vertex(Object data) {

			if (data instanceof FontPoint) {
				FontPoint fp = (FontPoint) data;

				if (FontTesselator.this.getDebugPath() != null) {

					Path path = FontTesselator.this.getDebugPath();
					path.addCircle((float) fp.x, (float) fp.y, 10, Direction.CW);

					if (this.typeOf == GLES20.GL_TRIANGLE_FAN) {

						if (this.p1 == null) {

							this.p1 = new FontPoint(fp);
						} else if (this.p2 == null) {

							this.p2 = new FontPoint(fp);
						} else {

							/*Log.i(TAG,
									String.format(
											"TESSOUT Triangle Fan: %f, %f; %f, %f; %f, %f;",
											p1.x, p1.y, p2.x, p2.y,
											(float) fp.x, (float) fp.y)); */

							path.moveTo((float) p1.x, (float) p1.y);
							path.lineTo((float) p2.x, (float) p2.y);
							path.lineTo((float) fp.x, (float) fp.y);
							path.lineTo((float) p1.x, (float) p1.y);

							fp.parent.addIndex(p1.index);
							fp.parent.addIndex(p2.index);
							fp.parent.addIndex(fp.index);
							path.close();

							p2 = new FontPoint(fp);
						}

					} else if (this.typeOf == GLES20.GL_TRIANGLE_STRIP) {

						if (this.p1 == null) {

							this.p1 = new FontPoint(fp);

						} else if (this.p2 == null) {

							this.p2 = new FontPoint(fp);
						} else {

							/*Log.i(TAG,
									String.format(
											"TESSOUT Triangle Strip: %f, %f; %f, %f; %f, %f;",
											p1.x, p1.y, p2.x, p2.y,
											(float) fp.x, (float) fp.y)); */

							path.moveTo((float) p1.x, (float) p1.y);
							path.lineTo((float) p2.x, (float) p2.y);
							path.lineTo((float) fp.x, (float) fp.y);
							path.lineTo((float) p1.x, (float) p1.y);

							fp.parent.addIndex(p1.index);
							fp.parent.addIndex(p2.index);
							fp.parent.addIndex(fp.index);

							path.close();

							p1 = new FontPoint(p2);
							p2 = new FontPoint(fp);
						}

					} else if (this.typeOf == GLES20.GL_TRIANGLES) {

						if (this.p1 == null) {

							this.p1 = new FontPoint(fp);
							this.p2 = null;
						} else if (this.p2 == null) {

							this.p2 = new FontPoint(fp);
						} else {
							
							/*
							Log.i(TAG,
									String.format("TESSOUT Triangles:[%d] %f, %f; [%d] %f, %f; [%d] %f, %f;",
											p1.index, p1.x, p1.y, p2.index, p2.x, p2.y, fp.index, (float)fp.x,
											(float)fp.y)); */

							path.moveTo((float) p1.x, (float) p1.y);
							path.lineTo((float) p2.x, (float) p2.y);
							path.lineTo((float) fp.x, (float) fp.y);
							path.lineTo((float) p1.x, (float) p1.y);

							fp.parent.addIndex(p1.index);
							fp.parent.addIndex(p2.index);
							fp.parent.addIndex(fp.index);

							path.close();

							this.p1 = null;
						}

					} else {

						throw new RuntimeException();
					}

				}

				//Log.i(TAG, String.format("VERTEX: %f, %f, %f", fp.x, fp.y, d[2]));
				
			} else {
				Log.i(TAG, "Invalid data: " + data.toString());
				throw new RuntimeException();
			}
		}

		public void error(int errnum) {
			System.out.println("Tessellation Error: " + errnum);
			throw new RuntimeException();
		}

		public void combine(double[] coords, Object[] data, float[] weight,
				Object[] outData) {

			FontPoint[] fps = (FontPoint[]) data;

			FontPoint fp = new FontPoint(0, (float) coords[0],
					(float) coords[1], (float) coords[2]);
			fp.parent = fps[0].parent;
			
			FontPoint vert0 = fps[0];
			FontPoint vert1 = fps[1];
			FontPoint vert2 = fps[2];
			FontPoint vert3 = fps[3];

			fp.u = vert0.u * weight[0] + vert1.u * weight[1] + vert2.u
					* weight[2] + vert3.u * weight[3];
			fp.v = vert0.v * weight[0] + vert1.v * weight[1] + vert2.v
					* weight[2] + vert3.v * weight[3];

			FontTesselator.this.contour.addPoint(fp);

			outData[0] = fp;

		}
	}

}
