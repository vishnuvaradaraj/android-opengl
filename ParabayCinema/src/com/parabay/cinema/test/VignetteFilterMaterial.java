package com.parabay.cinema.test;

import android.opengl.GLES20;
import rajawali.materials.SimpleMaterial;

public class VignetteFilterMaterial extends SimpleMaterial {
			
		protected int muSizeHandle;
		protected int muAmountHandle;
	
		protected static final String mFilterFShader = 
				
				"precision mediump float;\n" +
				"uniform float uSize;\n" +
				"uniform float uAmount;\n" +
				
				"varying vec2 vTextureCoord;\n" +
				"uniform sampler2D uDiffuseTexture;\n" +
				"varying vec4 vColor;\n" +

				"void main() {\n" +
				"#ifdef TEXTURED\n" +
				" 	float amount = uAmount;\n" +
				" 	float size = uSize;\n" +
	            "	vec4 color = texture2D(uDiffuseTexture, vTextureCoord);\n" +
	            "	float dist = distance(vTextureCoord, vec2(0.5, 0.5));\n" +
	            "	color.rgb *= smoothstep(0.8, size * 0.799, dist * (amount + size));\n" +
				"	gl_FragColor = color;\n" +
				"#else\n" +
			    "	gl_FragColor = vColor;\n" +
			    "#endif\n" +
				"}\n";
		
		
	public VignetteFilterMaterial() {
		
		super(mVShader, mFilterFShader);
		setShaders();
		
		muSizeHandle = GLES20.glGetUniformLocation(mProgram, "uSize");
		if(muSizeHandle == -1) {
			throw new RuntimeException("Could not get uniform location for muSizeHandle");
		}
		
		muAmountHandle = GLES20.glGetUniformLocation(mProgram, "uAmount");
		if(muAmountHandle == -1) {
			throw new RuntimeException("Could not get uniform location for muAmountHandle");
		}

	}
	
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		
	}
	
	public void setParams(float amount, float size) {
		
		GLES20.glUniform1f(muSizeHandle, size);
		GLES20.glUniform1f(muAmountHandle, amount);
	}
}
