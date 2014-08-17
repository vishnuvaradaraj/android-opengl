package com.parabay.cinema.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import rajawali.BaseObject3D;
import rajawali.BufferInfo;
import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.Geometry3D.BufferType;
import rajawali.math.Number3D;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

/**
 * This example shows how you can create a large number of textured planes efficiently.
 * The slow way is creating 2000 Plane objects and 16 separate textures. The optimized way
 * is to create one BaseObject3D with the vertex data for 2000 planes in one buffer (and
 * the same for tex coord data, normal data, etc). Each single plane is given the same position
 * at (0, 0, 0). Extra buffers are created for each plane's position and rotation.
 * 
 * Only one texture is used. It's a 1024*1024 bitmap containing 16 256*256 images. This is
 * called a 'texture atlas'. Each plane is assigned a specific portion of this texture.
 * 
 * This is much faster than creating separate object and textures because the shader program 
 * needs to be created once, only one texture has to be uploaded, matrix transformations need
 * to be done only once on the cpu, etc.
 * 
 * @author dennis.ippel
 *
 */
public class PlanesGalore extends BaseObject3D {
	
	protected FloatBuffer mPlanePositions;
	protected FloatBuffer mRotationSpeeds;
	protected BufferInfo mPlanePositionsBufferInfo;
	protected BufferInfo mRotationSpeedsBufferInfo;
	protected PlanesGaloreMaterial mGaloreMat;
	
	protected int staticIndex = 100;
	protected Number3D staticPosition;
	
	public int getStaticIndex() {
		return this.staticIndex;
	}
	
	public Number3D getStaticPosition() {
		return this.staticPosition;
	}
	
	public PlanesGalore() {
		super();
		mPlanePositionsBufferInfo = new BufferInfo();
		mRotationSpeedsBufferInfo = new BufferInfo();
		init();
	}

	public void init() {
		mMaterial = mGaloreMat = new PlanesGaloreMaterial();
		final int numPlanes = 2000;
		final float planeSize = .8f;

		int numVertices = numPlanes * 4;
		float[] vertices = new float[numVertices * 3];
		float[] textureCoords = new float[numVertices * 2];
		float[] normals = new float[numVertices * 3];
		float[] planePositions = new float[numVertices * 3];
		float[] rotationSpeeds = new float[numVertices];
		float[] colors = new float[numVertices * 4];
		int[] indices = new int[numPlanes * 6];

		for (int i = 0; i < numPlanes; ++i) {
			Number3D r = new Number3D(-10f + (Math.random() * 20f), -10 + (Math.random() * 20f), (Math.random() * 80f));
			
			int randColor = 0xff000000 + (int) (0xffffff * Math.random());

			int vIndex = i * 4 * 3;
			vertices[vIndex + 0] = -planeSize;
			vertices[vIndex + 1] = planeSize;
			vertices[vIndex + 2] = 0;
			vertices[vIndex + 3] = planeSize;
			vertices[vIndex + 4] = planeSize;
			vertices[vIndex + 5] = 0;
			vertices[vIndex + 6] = planeSize;
			vertices[vIndex + 7] = -planeSize;
			vertices[vIndex + 8] = 0;
			vertices[vIndex + 9] = -planeSize;
			vertices[vIndex + 10] = -planeSize;
			vertices[vIndex + 11] = 0;

			for (int j = 0; j < 12; j += 3) {
				normals[vIndex + j] = 0;
				normals[vIndex + j + 1] = 0;
				normals[vIndex + j + 2] = 1;

				planePositions[vIndex + j] = r.x;
				planePositions[vIndex + j + 1] = r.y;
				planePositions[vIndex + j + 2] = r.z;
			}

			vIndex = i * 4 * 4;

			for (int j = 0; j < 16; j += 4) {
				colors[vIndex + j] = Color.red(randColor) / 255f;
				colors[vIndex + j + 1] = Color.green(randColor) / 255f;
				colors[vIndex + j + 2] = Color.blue(randColor) / 255f;
				colors[vIndex + j + 3] = 1.0f;
			}

			vIndex = i * 4 * 2;

			float u1 = .25f * (int) Math.floor(Math.random() * 4f);
			float v1 = .25f * (int) Math.floor(Math.random() * 4f);
			float u2 = u1 + .25f;
			float v2 = v1 + .25f;

			textureCoords[vIndex + 0] = u2;
			textureCoords[vIndex + 1] = v1;
			textureCoords[vIndex + 2] = u1;
			textureCoords[vIndex + 3] = v1;
			textureCoords[vIndex + 4] = u1;
			textureCoords[vIndex + 5] = v2;
			textureCoords[vIndex + 6] = u2;
			textureCoords[vIndex + 7] = v2;

			vIndex = i * 4;
			int iindex = i * 6;
			indices[iindex + 0] = (short) (vIndex + 0);
			indices[iindex + 1] = (short) (vIndex + 1);
			indices[iindex + 2] = (short) (vIndex + 3);
			indices[iindex + 3] = (short) (vIndex + 1);
			indices[iindex + 4] = (short) (vIndex + 2);
			indices[iindex + 5] = (short) (vIndex + 3);

			float rotationSpeed = -1f + (float) (Math.random() * 2f);
			
			if (this.staticIndex != i) {
				rotationSpeed = 0f;
				this.staticPosition = r.clone();
				//Log.i("TAG", String.format("static pos: %f, %f, %f", r.x, r.y, r.z));
			}
			
			rotationSpeeds[vIndex + 0] = rotationSpeed;
			rotationSpeeds[vIndex + 1] = rotationSpeed;
			rotationSpeeds[vIndex + 2] = rotationSpeed;
			rotationSpeeds[vIndex + 3] = rotationSpeed;
		}

		setData(vertices, normals, textureCoords, colors, indices);

		mPlanePositions = ByteBuffer.allocateDirect(planePositions.length * Geometry3D.FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mPlanePositions.put(planePositions).position(0);

		mRotationSpeeds = ByteBuffer.allocateDirect(rotationSpeeds.length * Geometry3D.FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mRotationSpeeds.put(rotationSpeeds).position(0);

		createBuffers();
	}
	
	private void createBuffers() {
		mGeometry.createBuffer(mPlanePositionsBufferInfo, BufferType.FLOAT_BUFFER, mPlanePositions, GLES20.GL_ARRAY_BUFFER);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

		mGeometry.createBuffer(mRotationSpeedsBufferInfo, BufferType.FLOAT_BUFFER, mRotationSpeeds, GLES20.GL_ARRAY_BUFFER);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	public void reload() {
		super.reload();
		createBuffers();
	}

	protected void setShaderParams(Camera camera) {
		super.setShaderParams(camera);
		mGaloreMat.setPlanePositions(mPlanePositionsBufferInfo.bufferHandle);
		mGaloreMat.setRotationSpeeds(mRotationSpeedsBufferInfo.bufferHandle);
	}
}
