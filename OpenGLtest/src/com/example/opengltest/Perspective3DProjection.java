package com.example.opengltest;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Perspective3DProjection extends Activity{
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
ByteBuffer byteBuffer;
FloatBuffer vertices;
FloatBuffer colors;
FloatBuffer texturemap;
ShortBuffer index;

float[] cube= {
		1f,-1f,1f,    
		1f,-1f,-1f,   
		1f,1f,-1f,    
		1f,1f,1f,     
		
		-1f,-1f,1f, 
		-1f,-1f,-1f,  
		-1f,1f,-1f,   
		-1f,1f,1f,    
		
		-1f,1f,1f,     
		1f,1f,1f,     
		1f,1f,-1f,     
		-1f,1f,-1f,    
		
		-1f,-1f,1f,    
		1f,-1f,1f,    
		1f,-1f,-1f,    
		-1f,-1f,-1f,   
		
		-1f,-1f,1f,    
		1f,-1f,1f,    
		1f,1f,1f,      
		-1f,1f,1f,     
		
		-1f,-1f,-1f,   
		1f,-1f,-1f,   
		1f,1f,-1f,    
		-1f,1f,-1f    
			
		};

short[] cubeIndices = { 0, 1, 2, 2, 3, 0,
		4,5,6,6,7,4,
		8,9,10,10,11,8,
		12,13,14,14,15,12,
		16,17,18,18,19,16,
		20,21,22,22,23,20
		};

	public myRenderer(){
		byteBuffer = ByteBuffer.allocateDirect(24*3*4);
		//24 vertices*3 members(x,y,z)*4bytes each float
		byteBuffer.order(ByteOrder.nativeOrder());//set byteorder
		vertices = byteBuffer.asFloatBuffer();//vertices as floatbuffer
		vertices.put(cube);//copy float array to float buffer
		vertices.flip();//set limits from 0 to number of floats
		byteBuffer = ByteBuffer.allocateDirect(36*2);
		//36 short indices*2bytes each short
		byteBuffer.order(ByteOrder.nativeOrder());
		index = byteBuffer.asShortBuffer();//index as shortbuffer
		index.put(cubeIndices);//copy array to buffer
		index.flip();//set limits
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		//set black as clear color
		gl.glClearColor(0,0,0,1); 
		//enable vertex array for vertex pointer
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
		//enable DepthTest
		gl.glEnable(GL10.GL_DEPTH_TEST); 
		//Blending enabled
		gl.glEnable(GL10.GL_BLEND);
		//set blend function
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	//set viewport to the whole GLSurfaceView size
	gl.glViewport(0, 0, width, height);
	//set projection matrix as active matrix
	gl.glMatrixMode(GL10.GL_PROJECTION);  
	//load identity matrix to multiply with next matrix operation
	gl.glLoadIdentity();     
	//set perspective projection(GL10,FovY,Aspect,zNear,zFar)
	GLU.gluPerspective(gl, 70,width/(float)height,0.1f, 10f);
	//set ModelView matrix as active
	gl.glMatrixMode(GL10.GL_MODELVIEW);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		//clear framebuffer and depth buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); 
		//gl.glDisable(GL10.GL_DEPTH_TEST); //to draw2D over3D
		//load identity
		gl.glLoadIdentity();
		//set transformation and color for first cube
		gl.glTranslatef(0, 0, -8);
		gl.glRotatef(30, 0, 1, 0);
		gl.glColor4f(0, 1, 0, 1);
		//set vertexpointer(3memberXYZ,FloatData,3Float*4Byte,dataBuffer)
		gl.glVertexPointer(3, GL10.GL_FLOAT, 12, vertices);
		//Draw first cube
		gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_SHORT, index);
		//reuse vertices for 2nd cube, no vertexpointer change needed
		gl.glLoadIdentity();
		//transform and color 2nd cube
		gl.glColor4f(0, 0, 1, 1);
		gl.glTranslatef(1.5f, 0, -9);
		gl.glRotatef(30, 0, 1, 0);
		gl.glScalef(0.5f, 0.5f, 0.5f);
		//draw reusing index from first cube
		gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_SHORT, index);
		//GL10.glDrawElements(DrawType,36 indices,shorData,dataBuffer)
	}

}

}
