package com.example.opengltest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class GLES20samplers extends Activity {
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
	int vertexShader;
	int fragmentShader;
	int program;
    float[] verticesArray = {0f,0f,0f,0.7f,0f,0f,0.7f,0.7f,0f,0f,0.7f,0f};
    FloatBuffer verticesFloatBuffer;
    float[] txMapArray={0f,1f,1f,1f,1f,0f,0f,0f};
    FloatBuffer txMapFloatBuffer;
    short[] indicesArray={0,1,2,2,3,0};
    ShortBuffer indicesShortBuffer;
    float[] rMatrix={0.70710677f,0.70710677f,0f,0f,-0.70710677f,0.70710677f,0f,0f,0f,0f,1f,0f,0f,0f,0f,1f};
    int rMatrixHandle;
    Bitmap bitmap;
    int textureId;
    int samplerHandle;
    String vertexSharderSource = 
            "attribute vec4 attributePosition;    \n"
          + "uniform mat4 rMatrix;                 \n" 
          + "attribute vec2 attributeTextureCoord;\n"
          + "varying vec2 varyingTextureCoord;    \n"
          + "void main()                          \n"
          + "{                                    \n"
          + "   gl_Position = rMatrix*attributePosition;  \n"
          + "   varyingTextureCoord = attributeTextureCoord;    \n"
          + "}                            \n";
    String fragmentShaderSource = 
            "precision mediump float;					  \n"
          + "varying vec2 varyingTextureCoord; 			   \n"
          + "uniform sampler2D samplerTexture;					\n" 
          + "void main()                                  \n"
          + "{                                            \n"
          + "  gl_FragColor = texture2D(samplerTexture, varyingTextureCoord); \n"
          + "}                                            \n";

    public SurfaceRenderer(Context context)
    {//create Buffers
    //allocate num of vertices*3 members XYZ*4bytes each float
    verticesFloatBuffer = ByteBuffer.allocateDirect(verticesArray.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    verticesFloatBuffer.put(verticesArray).position(0);
    //allocate number of indices*2bytes each short
    indicesShortBuffer = ByteBuffer.allocateDirect(indicesArray.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
    indicesShortBuffer.put(indicesArray).position(0);
    //allocate number of txMap coords*4bytes
    txMapFloatBuffer = ByteBuffer.allocateDirect(txMapArray.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    txMapFloatBuffer.put(txMapArray).position(0);
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
    //Bind Position to attribute location 0
    GLES20.glBindAttribLocation(prog, 0, "attributePosition");
    //Bind Texture Coord to attribute location 1
    GLES20.glBindAttribLocation(prog, 1, "attributeTextureCoord");
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
    //open asset to bitmap
    try {
		 AssetManager assetManager = getAssets();
	 	 InputStream inputStream = assetManager.open("caveman128x128.png");
	     bitmap = BitmapFactory.decodeStream(inputStream);
	     inputStream.close();
	     } catch (IOException e) {}
    //get the texture handle
    int[] textures = new int[1];
    //generate a texture on client mem, return handle
    GLES20.glGenTextures(1, textures, 0);
    textureId = textures[0];
    //bind texture for next operations
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
    //set minification filter
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST_MIPMAP_NEAREST);
    //set magnification filter
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    //set texture wrap for X texture coord(S)
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
    //set texture wrap for Y texture coord(T)
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
    //copy pixel data from bitmap to bound texture
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    //delete bitmap
    bitmap.recycle();
    //un bind texture
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    
    }

    public void onDrawFrame(GL10 gl)
    {// Clear the color 
     GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
     //indicate which program to use on draw
     GLES20.glUseProgram(program);
     //set attribPointer(location 0,3membersXYZ,float,norm=false,stride=0,buffer)
     GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, verticesFloatBuffer);
     //enable attrib on (location 0)
     GLES20.glEnableVertexAttribArray(0);
     //set attribPointer(location 1,2membersTxTy,float,norm=false,stride=0,buffer)
     GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, txMapFloatBuffer);
     //enable attribute array(loc 1)
     GLES20.glEnableVertexAttribArray(1);
//get max texture Units
     //int[] maxt=new int[1];
     //GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, maxt, 0);
     //Log.d("max texture image units", ""+maxt[0]);
//get max texture Size
     //int[] maxNum = new int[1];
     //GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxNum, 0);
     //Log.d("max texture size", ""+maxNum[0]);
     //set active texture unit
     GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
     //bind a (2Dtexture,Id) to the active texture unit 
     GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
     //get handle to Sampler Uniform location
     samplerHandle=GLES20.glGetUniformLocation(program, "samplerTexture");
     //if Sampler Uniform location not found handle is returned -1
     if (samplerHandle == -1) {
         throw new RuntimeException("Could not get samplerTexture location");
     }
     //set the texture unit to fetch from 0 to max Texture units
     GLES20.glUniform1i(samplerHandle, 0);
     //get Uniform Handle(program,uniformName) 
     rMatrixHandle = GLES20.glGetUniformLocation(program, "rMatrix");
     //if Uniform location not found handle is returned -1
     if (rMatrixHandle == -1) {
         throw new RuntimeException("Could not get rMatrix location");
     }
     //set the Uniform data source(Handle,elements to read,false,source,offset)
     GLES20.glUniformMatrix4fv(rMatrixHandle, 1, false, rMatrix, 0);
     //drawelements(type,number of indices,dataType, dataBuffer)
     GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesArray.length, GLES20.GL_UNSIGNED_SHORT, indicesShortBuffer);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height)
    {//set viewport to whole surface
    GLES20.glViewport(0, 0, width, height);
    }
}
}

