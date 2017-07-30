package com.example.opengltest;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class TextureTest extends Activity{
	GLSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//no title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//fullscreen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//initialize GLSurfaceView
		glSurfaceView = new GLSurfaceView(this);
		//set renderer
		glSurfaceView.setRenderer(new myRenderer());
		//set surface as content
		setContentView(glSurfaceView);
	}


@Override
public void onResume() {
super.onResume();
//resume drawing if activity onResume
glSurfaceView.onResume();
}

@Override
public void onPause() {
super.onPause();
//stop drawing if activity onPause
glSurfaceView.onPause();
}



class myRenderer implements Renderer {
float Triangle[]={0.0f,0.0f,100.0f,0.0f,100.0f,100.0f,0.0f,100.0f};
short triangleIndex[]={0,1,2,2,3,0};
float triangleTexturemap[]={0.0f,1.0f,1.0f,1.0f,1.0f,0.0f,0.0f,0.0f};
ByteBuffer byteBuffer;
FloatBuffer vertices;
ShortBuffer index;
FloatBuffer texturemap;
Bitmap bitmap=null;
int textureId;
IntBuffer textureID;

	public myRenderer(){
		//allocate 4 vertices*2 coordinates eachV(x,y)*4bytes each float
		byteBuffer = ByteBuffer.allocateDirect(4*2*4);
		//set byteorder
		byteBuffer.order(ByteOrder.nativeOrder());
		//vertices as floatbuffer
		vertices = byteBuffer.asFloatBuffer();
		//copy float array to float buffer
		vertices.put(Triangle);
		//set limits from 0 to number of floats
		vertices.flip();
		//allocate 6 short indices*2bytes each short
		byteBuffer = ByteBuffer.allocateDirect(6*2);
		//byte order
		byteBuffer.order(ByteOrder.nativeOrder());
		//index as shortbuffer
		index = byteBuffer.asShortBuffer();
		//copy short array to buffer
		index.put(triangleIndex);
		//set limits 0 to number of shorts
		index.flip();
		//allocate 4 vertices*2 coordinates eachV(TEXx,TEXy)*4bytes each float
		byteBuffer = ByteBuffer.allocateDirect(4*2*4);
		//set byteorder
		byteBuffer.order(ByteOrder.nativeOrder());
		//texturemap as floatbuffer
		texturemap = byteBuffer.asFloatBuffer();
		//copy float array to float buffer
		texturemap.put(triangleTexturemap);
		//set limits from 0 to number of floats
		texturemap.flip();
		//open external image on resources
		bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.caveman128x128); 
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		//set projection matrix as active matrix
		gl.glMatrixMode(GL10.GL_PROJECTION);
		//load identity matrix 
		gl.glLoadIdentity();
		//set parallel projection 800x480 with Zaxis 3 pixel width(1to-1)
		gl.glOrthof(0, 800, 0, 480, 1, -1);
		//set ModelView as active matrix
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		//set black as clear color
		gl.glClearColor(0,0,0,1); 
		//enable vertex array
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
		//glVertexPointer(num_of_coordinates_composing_each_point,data_type,stride,databuffer)
		gl.glVertexPointer(2,GL10.GL_FLOAT,8,vertices);
		//glGentexture using intbuffer
		//initialize intbuffer position 1
		textureID=IntBuffer.allocate(1);
		//generate 1(n) texture buffer in video mem return handle to textureID
		gl.glGenTextures(1, textureID);
		//bind using intBuffer//bind texture video mem buffer(type, handle)
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID.get(0));
		//copy image pixels data to texture buffer on video mem
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		//filter for minif
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		//filter for magnif
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		//unbind texture buffer
		gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		//bitmap.recycle(); just if not used anymore 
		//enable texture coord array
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
		//pointer to text mapping(num_of_coordinates 2 xy , data type FLOAT, stride, array )
		gl.glTexCoordPointer(2, GL10.GL_FLOAT,8, texturemap);   
		//enable blending
		gl.glEnable(GL10.GL_BLEND);  
		//set blending source and function
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	//set viewport to the whole GLSurfaceView size
	gl.glViewport(0, 0, width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		//enable texture state
		gl.glEnable(GL10.GL_TEXTURE_2D);
		//bind texture buffer
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID.get(0));
		//clear framebuffer with clearcolor
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT); 
		//indexed draw
		gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, index);
		//glDrawElements(TYPE_OF_DRAW,num of indices, TYPE_OF_DATA, BUFFER with index data)
	}

}

}

