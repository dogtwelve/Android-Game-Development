package com.example.opengltest;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class DrawTest extends Activity{
	GLSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//no title bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//fullscreen
		
		glSurfaceView = new GLSurfaceView(this);//initialize GLSurfaceView
		glSurfaceView.setRenderer(new myRenderer());//set myRenderer class as renderer
		setContentView(glSurfaceView);//no xml layout, set glSurfaceView to full screen
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


	public myRenderer(){
	
	}

	@Override
	public void onDrawFrame(GL10 gl) {
	
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	
	}

}
	
}

