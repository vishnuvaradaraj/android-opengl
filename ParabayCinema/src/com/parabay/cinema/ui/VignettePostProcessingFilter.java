package com.parabay.cinema.ui;

import android.opengl.GLES20;
import rajawali.filters.IPostProcessingFilter;
import rajawali.materials.AMaterial;


public class VignettePostProcessingFilter extends AMaterial implements IPostProcessingFilter {
	
	protected int muSizeHandle;
	protected int muAmountHandle;

	protected float mSize;
	protected float mAmount;

	protected static final String mVShader =
		"uniform mat4 uMVPMatrix;\n" +

		"attribute vec4 aPosition;\n" +
		"attribute vec2 aTextureCoord;\n" +
		"attribute vec4 aColor;\n" +

		"varying vec2 vTextureCoord;\n" +

		"void main() {\n" +
		"	gl_Position = uMVPMatrix * aPosition;\n" +
		"	vTextureCoord = aTextureCoord;\n" +
		"}\n";
	

			
	protected static final String mFShader = 
			
			"precision mediump float;\n" +
			"uniform float uSize;\n" +
			"uniform float uAmount;\n" +
			
			"varying vec2 vTextureCoord;\n" +
			"uniform sampler2D uFrameBufferTexture;\n" +
			"varying vec4 vColor;\n" +

			"void main() {\n" +
			"#ifdef TEXTURED\n" +
			" 	float amount = uAmount;\n" +
			" 	float size = uSize;\n" +
            "	vec4 color = texture2D(uFrameBufferTexture, vTextureCoord);\n" +
            "	float dist = distance(vTextureCoord, vec2(0.5, 0.5));\n" +
            "	color.rgb *= smoothstep(0.8, size * 0.799, dist * (amount + size));\n" +
			"	gl_FragColor = color;\n" +
			"#else\n" +
		    "	gl_FragColor = vColor;\n" +
		    "#endif\n" +
			"}\n";
	
	public VignettePostProcessingFilter() {
		super(mVShader, mFShader, false);
		
		mSize = 0.5f;
		mAmount = 0.5f;

		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public boolean usesDepthBuffer() {
		return false;
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		
		GLES20.glUniform1f(muSizeHandle, mSize);
		GLES20.glUniform1f(muAmountHandle, mAmount);

	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		
		muSizeHandle = getUniformLocation("uSize");
		muAmountHandle = getUniformLocation("uAmount");

	}
	
	public float getSize() {
		return mSize;
	}

	public void setSize(float radius) {
		this.mSize = radius;
	}

	public float getAmount() {
		return mAmount;
	}

	public void setAmount(float angle) {
		this.mAmount = angle;
	}
}
