package com.parabay.cinema.text;

import java.util.Arrays;
import java.util.Vector;

import android.content.Intent;
import android.graphics.Path;
import android.util.Log;

import com.parabay.cinema.text.ttf.BoundingBox;
import com.parabay.cinema.text.ttf.Glyph2D;
import com.parabay.cinema.text.ttf.GlyphData;

import rajawali.BaseObject3D;

public class FontChar extends BaseObject3D {

	public static final String TAG = "FontChar";
	
	private char c;
	private boolean f;
	private Path outline;
	private GlyphData glyphData;
	
	private Vector<FontPoint> points = new Vector<FontPoint>();
	private Vector<Integer> indexes = new Vector<Integer>();

	
	public FontChar(char cc, FontInfo fontInfo, boolean flag) {
		
		super();
		this.c = cc;
		this.f = flag;
		
		this.init(fontInfo);
	}

	protected boolean isSharpEdge(float cosAngle)
	{
	    if (cosAngle >= -0.7071068 && cosAngle <= 1.0)
	    {
	        return true;
	    }
	    else
	    {
	        return false;
	    }
	}
	
	private void updateGlobals(Vector<FontPoint> ptv, Vector<Integer> iv, int indexOffset) {
				
		for(FontPoint fp : ptv) {
			
			fp.index += indexOffset;
			this.points.add(new FontPoint(fp));
		}
		
		for(Integer n:iv) {
			
			this.indexes.add(n + indexOffset);
		}
	}
	
	private void preProcessPointsForSides(Vector<FontContour> contours) {
		
		//assumes half the points are for front & back faces respectively.
		
		int index = 0;
		int indexOffset = this.points.size();
		
		for(FontContour fc : contours) {
		
			Vector<FontPoint> ptv = new Vector<FontPoint>();
			Vector<Integer> iv = new Vector<Integer>();

			int numPoints = fc.getContourPoints().size();
	        for(int i=0; i<numPoints; i++) {
	        	
	        	FontPoint fp1 = fc.getContourPoint(i);
	        	FontPoint fp2 = new FontPoint(fp1);
	        	
	        	fp2.z -= 100;
	        	fp2.nz *= -1;
	        	 
	        	if (isSharpEdge(fc.vertexCosAngle(i))) {
	        		
	        		FontPoint fpNew = new FontPoint(fp1);
	        		fpNew.index = index++;
	                Vector2D normal = fc.edgeNormal(i - 1);
	                fpNew.nx = normal.x;
	                fpNew.ny = normal.y;
	                fpNew.nz = 0.0;
	                ptv.add(fpNew);

	                fpNew = new FontPoint(fp2);
	                fpNew.index = index++;
	                fpNew.nx = normal.x;
	                fpNew.ny = normal.y;
	                fpNew.nz = 0.0;
	                ptv.add(fpNew);
	            }
	        	
	        	FontPoint fpNew = new FontPoint(fp1);
	        	fpNew.index = index++;
                Vector2D normal = fc.vertexNormal(i);
                if (isSharpEdge(fc.vertexCosAngle(i))) {
                	
                	normal = fc.edgeNormal(i);
                }
                fpNew.nx = normal.x;
                fpNew.ny = normal.y;
                fpNew.nz = 0.0;
                ptv.add(fpNew);
                
                fpNew = new FontPoint(fp2);
                fpNew.index = index++;
                fpNew.nx = normal.x;
                fpNew.ny = normal.y;
                fpNew.nz = 0.0;
                ptv.add(fpNew);

                int thisPoint = ptv.size() - 2; // first_contour_vertex + this_contour_vertex_offset;
                int nextPoint = (i < numPoints - 1) ?
                    thisPoint + 2
                    : 0;

                if (i < numPoints - 1) {
                iv.add(thisPoint);
                iv.add(nextPoint);
                iv.add(thisPoint + 1);
                
                iv.add(nextPoint);
                iv.add(nextPoint + 1);
                iv.add(thisPoint + 1);

                //add back face indexes
                iv.add(thisPoint);
                iv.add(thisPoint + 1);
                iv.add(nextPoint);
                
                iv.add(nextPoint);
                iv.add(thisPoint + 1);
                iv.add(nextPoint + 1);
                }

	        }
	       
			//Log.i("DEBUG", String.format("Side Vertices: %s", Arrays.toString(ptv.toArray())));
			//Log.i("DEBUG", String.format("Side Indices: %s", Arrays.toString(iv.toArray())));
			
			this.updateGlobals(ptv, iv, indexOffset);

		}

	}

	private void preProcessPointsForBackFace(Vector<FontContour> contours) {
		
		int offset = 0;
		int indexOffset = this.points.size();
		
		for(FontContour fc : contours) {
			
			Vector<FontPoint> ptv = new Vector<FontPoint>();
			Vector<Integer> iv = new Vector<Integer>();

			int numPoints = fc.getPoints().size();
	        for(int i=0; i<numPoints; i++) {
	        	
	        	FontPoint fp = new FontPoint(fc.getPoint(i));
	        	fp.index = offset++;
	        	fp.z -= 100;
	        	fp.nz *= -1;
	        	
	        	ptv.add(fp);
	        }
	        
	        int numIndexes = fc.getIndices().size();
	        for(int j=0; j<numIndexes/3; j++) {
	        	
	        	int index = j*3;
	        	iv.add(fc.getIndices().get(index));
	        	iv.add(fc.getIndices().get(index+2));
	        	iv.add(fc.getIndices().get(index+1));
	        }
	        
			this.updateGlobals(ptv, iv, indexOffset);
		}

	}
	
	private void init(FontInfo fontInfo) {
		
		String s = "" + this.c;
    	glyphData = fontInfo.getCharGlyph(s);
    	Glyph2D g = new Glyph2D(glyphData, (short) 0, 0);
    	
    	FontPath fontPath = g.getFontPath();
    	FontTesselator tess = fontPath.getTesselator();
				
    	this.outline = tess.getDebugPath();
    	
		for(FontContour fc : tess.getContours()) {
			
			//Log.i(TAG, String.format("Raw Contour: %s", Arrays.toString(fc.getPoints().toArray())));
			//Log.i(TAG, String.format("Raw Indexes: %s", Arrays.toString(fc.getIndices().toArray())));

			this.updateGlobals(fc.getPoints(), fc.getIndices(), 0);
		}
		
		//Log.i("DEBUG", String.format("Combined Vertices: %s", Arrays.toString(points.toArray())));
		//Log.i("DEBUG", String.format("Combined Indices: %s", Arrays.toString(indexes.toArray())));

		this.preProcessPointsForBackFace(tess.getContours());		
		this.preProcessPointsForSides(tess.getContours());
		
		
		int numVertices = points.size();
        float[] vertices = new float[numVertices * 3];
        float[] textureCoords = new float[numVertices * 2];
        float[] normals = new float[numVertices * 3];
		float[] colors = new float[numVertices * 4];
        int[] indices = new int[indexes.size()];

        for(int i=0; i<numVertices; i++) {
        	
        	int index = 0;
        	
        	FontPoint fp = points.get(i);
        	//Log.i(TAG, fp.toString());
        	
        	index = i *3;
        	vertices[index+0] = (float)fp.x;
        	vertices[index+1] = (float)fp.y;
        	vertices[index+2] = (float)fp.z;
        	
        	index = i *2;
        	textureCoords[index+0] = (float)fp.u;
        	textureCoords[index+1] = (float)fp.v;
        	
        	index = i *3;
        	normals[index+0] = (float)fp.nx;
        	normals[index+1] = (float)fp.nz;
        	normals[index+2] = (float)fp.ny;

        	index = i *4;
        	colors[index+0] = (float)1;
        	colors[index+1] = (float)0;
        	colors[index+2] = (float)0;
        	colors[index+3] = (float)1;

        }
        
        for(int j=0; j<indexes.size(); j++) {
        	
        	indices[j] = indexes.get(j);
        	
        	//Log.i(TAG, String.format("i(%d: %d)", j, indexes.get(j)));
        }

        
		//Log.i("DEBUG", String.format("Vertices: %s", Arrays.toString(vertices)));
		//Log.i("DEBUG", String.format("Normals: %s", Arrays.toString(normals)));
		//Log.i("DEBUG", String.format("Texture: %s", Arrays.toString(textureCoords)));
		//Log.i("DEBUG", String.format("Indices: %s", Arrays.toString(indices)));
		
        
        setData(vertices, normals, textureCoords, colors, indices);
	}

	
	public char getChar() {
		return c;
	}

	public Path getOutline() {
		return outline;
	}

	public GlyphData getGlyphData() {
		return this.glyphData;
	}
}
