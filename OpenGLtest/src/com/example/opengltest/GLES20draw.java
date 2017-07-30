package com.example.opengltest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class GLES20draw extends Activity {
	SurfaceRenderer render;
	GLSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		glSurfaceView = new GLSurfaceView(this); //initialize surface
		render=new SurfaceRenderer(this); //initialize renderer 
		glSurfaceView.setEGLContextClientVersion(2);//set EGLcontext
		glSurfaceView.setRenderer(render);//set renderer
		Log.d("openGL ES 2.0 Support: ",""+ detectOpenGLES20());//checkfor 2.0support
		setContentView(glSurfaceView);//set surface as content
	}
	
	@Override
	public void onResume() {
	super.onResume();
	glSurfaceView.onResume();//resume renderer of glsurfaceview

	}

	@Override
	public void onPause() {
	super.onPause();
	glSurfaceView.onPause();//pause renderer of glsurfaceview
	}

	 private boolean detectOpenGLES20() 
	 {//detect if device support GLES2.0
	 ActivityManager activityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	 ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
	 return (configurationInfo.reqGlEsVersion >= 0x20000);
	 }

public class SurfaceRenderer implements GLSurfaceView.Renderer{
    private int program;
    private FloatBuffer vertices;
    private int vertexShader;
    private int fragmentShader;
    private final float[] triangle = { 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f };
    String vertexSharderSource = 
            "attribute vec4 vPosition;    \n"
          + "void main()                  \n"
          + "{                            \n"
          + "   gl_Position = vPosition;  \n"
          + "}                            \n";
    String fragmentShaderSource = 
            "precision mediump float;					  \n"
          + "void main()                                  \n"
          + "{                                            \n"
          + "  gl_FragColor = vec4 ( 0.0, 0.0, 1.0, 1.0 );\n"
          + "}                                            \n";
  
    public SurfaceRenderer(Context context)
    {//create vertices floatbuffer
    vertices = ByteBuffer.allocateDirect(triangle.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    vertices.put(triangle).position(0);
    }
 
    //load shader method
    private int loadShader(int shaderType, String source) {
    //create shader
    int shader = GLES20.glCreateShader(shaderType);
    if (shader != 0) {
    //load source code string
    GLES20.glShaderSource(shader, source);
    //compile shader
    GLES20.glCompileShader(shader);
    int[] compiled = new int[1];
    //get compile status
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
    if (compiled[0] == 0) {
    Log.e("ShaderLoader", "Could not compile shader " + shaderType + ":");
    //if error get error log
    Log.e("ShaderLoader", GLES20.glGetShaderInfoLog(shader));
    //if error delete shader
    GLES20.glDeleteShader(shader);
    shader = 0;
    }
    }
    return shader;
    }
    
    //create program method
    private int createProgram(){
	int prog;
    int[] linked = new int[1];
    //load shader(type vertex, source code string)
    vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSharderSource);
    //load shader(type fragment, source code string)
    fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);
    //create GLES2.0 program
    prog = GLES20.glCreateProgram();
    //if failed return
    if (prog == 0){return 0;}
    //attach vertex shader
    GLES20.glAttachShader(prog, vertexShader);
    //attach fragment shader
    GLES20.glAttachShader(prog, fragmentShader);
    //Bind vPosition to attribute location 0
    GLES20.glBindAttribLocation(prog, 0, "vPosition");
    //link program
    GLES20.glLinkProgram(prog);
    // Check the link 
    GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, linked, 0);
    if (linked[0] == 0)
    {//if error get error log
        Log.e("Linker", "Error linking program:");
        Log.e("Linker", GLES20.glGetProgramInfoLog(prog));
        //if error delete program
        GLES20.glDeleteProgram(prog);
        return 0;
    }
    return prog;
    }
    
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {//create program
    program = createProgram();
    //set clear color
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public void onDrawFrame(GL10 gl)
    {// Clear the color 
     GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
     //indicate which program to use on draw
     GLES20.glUseProgram(program);
     //set attribPointer(location 0,3membersXYZ,float,norm=false,stride=0,buffer)
     GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, vertices);
     //enable attrib on (location 0)
     GLES20.glEnableVertexAttribArray(0);
     //draw (triangles, offset 0, 3 vertices)
     GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height)
    {//set viewport to whole surface
    GLES20.glViewport(0, 0, width, height);
    }

}
}