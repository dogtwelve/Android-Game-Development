package com.example.opengltest;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class IndexTest extends Activity{
	GLSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//no title bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//fullscreen
		glSurfaceView = new GLSurfaceView(this);//initialize GLSurfaceView
		glSurfaceView.setRenderer(new myRenderer());//set renderer
		setContentView(glSurfaceView);//set surface as content
	}


@Override
public void onResume() {
super.onResume();
glSurfaceView.onResume();//resume drawing if activity onResume

}

@Override
public void onPause() {
super.onPause();
glSurfaceView.onPause();//stop drawing if activity onPause
}



class myRenderer implements Renderer {
float Triangle[]={0.0f,0.0f,50.0f,0.0f,50.0f,50.0f,0.0f,50.0f};
short triangleIndex[]={0,1,2,2,3,0};
ByteBuffer byteBuffer;
FloatBuffer vertices;
ShortBuffer index;

	public myRenderer(){
		byteBuffer = ByteBuffer.allocateDirect(4*2*4);
		//4 vertices*2 coordinates eachV(x,y) *4bytes each float
		byteBuffer.order(ByteOrder.nativeOrder());//set byteorder
		vertices = byteBuffer.asFloatBuffer();//vertices as floatbuffer
		vertices.put(Triangle);//copy float array to float buffer
		vertices.flip();//set limits from 0 to number of floats
		byteBuffer = ByteBuffer.allocateDirect(6*2);
		//6 short indices*2bytes each short
		byteBuffer.order(ByteOrder.nativeOrder());
		index = byteBuffer.asShortBuffer();//index as shortbuffer
		index.put(triangleIndex);//copy array to buffer
		index.flip();//set limits
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glMatrixMode(GL10.GL_PROJECTION);//set projection matrix as active matrix
		gl.glLoadIdentity();//load identity matrix to multiply with next matrix operation
		gl.glOrthof(0, 800, 0, 480, 1, -1);//set parallel projection 800x480 with Zaxis 3 pixel width(1to-1)
		gl.glMatrixMode(GL10.GL_MODELVIEW);//set ModelView as active matrix
		gl.glClearColor(0,0,0,1); //set black as clear color
		gl.glColor4f(1, 0, 0, 1); //set default color for render
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); //enable vertex array
		gl.glVertexPointer(2,GL10.GL_FLOAT,8,vertices);//glVertexPointer(num_of_coordinates_composing_each_point,data_type,stride,databuffer)
		//stride can be 0, if the positions are tightly packed [vertex 1 (x,y), vertex 2 (x,y), and so on]
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	gl.glViewport(0, 0, width, height);//set viewport to the whole GLSurfaceView size
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT); //clear framebuffer with clearcolor
		gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, index);
		//glDrawElements(TYPE_OF_DRAW,num of indices, TYPE_OF_DATA, BUFFER with index data)
	}

}

}

