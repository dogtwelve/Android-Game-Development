package com.example.ndktest;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



public class MainActivity extends Activity {

	GLSurfaceView glSurfaceView;
	SurfaceRenderer render;
    

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView=new GLSurfaceView(this);
        render=new SurfaceRenderer();
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(render);
		setContentView(glSurfaceView);
    }

    @Override protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
            }

    @Override protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
           }
    
    
    public class SurfaceRenderer implements GLSurfaceView.Renderer{

		public SurfaceRenderer() {
			super();
			// TODO Auto-generated constructor stub
			}

		@Override
		public void onDrawFrame(GL10 gl) {
			// TODO Auto-generated method stub
			JNItest.drawFrame();
			}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			// TODO Auto-generated method stub
			JNItest.surfaceChanged(width, height);
			}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			// TODO Auto-generated method stub
			JNItest.surfaceCreated();
			}
		}
}
