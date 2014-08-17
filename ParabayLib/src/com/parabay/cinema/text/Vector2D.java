package com.parabay.cinema.text;

import android.util.FloatMath;

import com.parabay.cinema.text.FontPoint;

public class Vector2D {
	public float x;
	public float y;
	
	public Vector2D() {
		
	}
	
	public Vector2D(FontPoint fp) {
		this.x = (float)fp.x;
		this.y = (float)fp.y;
	}

	public Vector2D(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2D(String[] vals) {
		this.x = Float.parseFloat(vals[0]);
		this.y = Float.parseFloat(vals[1]);
	}

	public Vector2D(FontPoint p1, FontPoint p2) {
		
		Vector2D vp1 = new Vector2D(p1);
		Vector2D vp2 = new Vector2D(p2);
		
		Vector2D temp = vp2.sub(vp1);
		this.x = temp.x;
		this.y = temp.y;
	}
	
    public float length() {
    	
        return FloatMath.sqrt(x * x + y * y);
    }

    public Vector2D normal() {
    	
        return new Vector2D(y, -x);
    }

    public Vector2D normalize() {
    	
        double length = length();
        if (length == 0)
        {
            return new Vector2D();
        }
        else
        {
            return this.div(length());
        }
    }

    public Vector2D add(Vector2D v) {
    	
    	return new Vector2D(x + v.x, y + v.y);
    }
    
    public Vector2D sub(Vector2D v) {
    	
    	return new Vector2D(x - v.x, y - v.y);
    }
    
    public float mul(Vector2D v) {
    	
    	return this.x  * v.x +  y * v.y;
    }
    
    public Vector2D mul(float d) {
    	
    	return new Vector2D(x * d, y * d);
    }
    
    public Vector2D div(float d) {
    	
    	return new Vector2D(x / d, y / d);
    }
    
    public float cosAngle(Vector2D v2)
    {
         double divisor = this.length() * v2.length();
        if (divisor == 0.0)
        {
            return 0.0f;
        }
        else
        {
            return (float)((this.mul(v2)) / divisor);
        }
    }
    
    @Override public String toString() {
    	
    	String ret = String.format("Vector2D: (%f, %f)", x, y);
		return ret;
    }
}
