package com.parabay.cinema.text;

public class FontPoint {
	
	public int index;
    public double x, y, z;     // coordinates
    public double nx, ny, nz;  // normal vector
    public double u, v;        // texture coordinates
    public FontContour parent;
    
	private double []coord = new double[3];
	
	public FontPoint(FontPoint fp) {

		this.copyFrom(fp);
	}
	
	public void copyFrom(FontPoint fp) {

		this.index = fp.index;
		this.x = fp.x;
		this.y = fp.y;
		this.z = fp.z;
		
		this.nx = fp.nx;
		this.ny = fp.ny;
		this.nz = fp.nz;
		this.u = fp.u;
		this.v = fp.v;
		
		this.parent = fp.parent;
	}
	
	public FontPoint(int index, float x, float y) {
		
		this(index, x, y, 0.0f);
	}

	public FontPoint(int index, float x, float y, float z) {
		
		this.x = x;
		this.y = y;
		this.z = z;
		
		nx = ny = 0.0;
        nz = 1.0;
	}
	
	public double []toDoubleArray() {
		
		this.coord[0] = this.x;
		this.coord[1] = this.y;
		this.coord[2] = this.z;
		return this.coord;
	}
	
	@Override public String toString() {
		
		String ret = String.format("FontPoint: %d- V(%f, %f, %f), UV(%f, %f), N(%f, %f, %f)", index, x, y, z, u, v, nx, ny, nz);
		return ret;
	}
}