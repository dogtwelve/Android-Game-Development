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
import android.opengl.Matrix;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

public class GLES20cam2D extends Activity {
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
		glSurfaceView.setOnTouchListener(render);
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

public class SurfaceRenderer implements GLSurfaceView.Renderer,OnTouchListener{
	int vertexShader;
	int fragmentShader;
	int program;
    float[] verticesArray = {-100f,-100f,0f,100f,-100f,0f,100f,100f,0f,-100f,100f,0f};
    FloatBuffer verticesFloatBuffer;
    int verticesVBO[]=new int[1];
    int attributePositionLocation;
    float[] txMapArray={//anim to left direction
    0f,0.25f,		0.25f,0.25f,	0.25f,0f,		0f,0f,
    0.25f,0.25f,	0.5f,0.25f,		0.5f,0f,		0.25f,0f,
    0.5f,0.25f,		0.75f,0.25f,	0.75f,0f,		0.5f,0f,
    0.75f,0.25f,	1f,0.25f,		1f,0f,			0.75f,0f,
    0f,0.5f,		0.25f,0.5f,		0.25f,0.25f,	0f,0.25f,
    0.25f,0.5f,		0.5f,0.5f,		0.5f,0.25f,		0.25f,0.25f,
    0.5f,0.5f,		0.75f,0.5f,		0.75f,0.25f,	0.5f,0.25f,
    0f,1f,			0.25f,1f,		0.25f,0.75f,	0f,0.75f,
    0.25f,1f,		0.5f,1f,		0.5f,0.75f,		0.25f,0.75f,
    0.5f,1f,		0.75f,1f,		0.75f,0.75f,	0.5f,0.75f,
    0.75f,1f,		1f,1f,			1f,0.75f,		0.75f,0.75f,
    0f,0.75f,		0.25f,0.75f,	0.25f,0.5f,		0f,0.5f,
    0.25f,0.75f,	0.5f,0.75f,		0.5f,0.5f,		0.25f,0.5f,
    0.5f,0.75f,		0.75f,0.75f,	0.75f,0.5f,		0.5f,0.5f,
    //mirrors to right direction		
    0.25f,0.25f,	0f,0.25f,		0f,0f,      	0.25f,0f,		
    0.5f,0.25f,		0.25f,0.25f,	0.25f,0f,   	0.5f,0f,		
    0.75f,0.25f,	0.5f,0.25f,		0.5f,0f,    	0.75f,0f,		
    1f,0.25f,		0.75f,0.25f,	0.75f,0f,   	1f,0f,			
    0.25f,0.5f,		0f,0.5f,		0f,0.25f,   	0.25f,0.25f,	
    0.5f,0.5f,		0.25f,0.5f,		0.25f,0.25f,	0.5f,0.25f,		
    0.75f,0.5f,		0.5f,0.5f,		0.5f,0.25f, 	0.75f,0.25f,	
    0.25f,1f,		0f,1f,			0f,0.75f,   	0.25f,0.75f,	
    0.5f,1f,		0.25f,1f,		0.25f,0.75f,	0.5f,0.75f,		
    0.75f,1f,		0.5f,1f,		0.5f,0.75f, 	0.75f,0.75f,	
    1f,1f,			0.75f,1f,		0.75f,0.75f,	1f,0.75f,		
    0.25f,0.75f,	0f,0.75f,		0f,0.5f,    	0.25f,0.5f,		
    0.5f,0.75f,		0.25f,0.75f,	0.25f,0.5f, 	0.5f,0.5f,		
    0.75f,0.75f,	0.5f,0.75f,		0.5f,0.5f,   	0.75f,0.5f		
    };
    
    FloatBuffer txMapFloatBuffer;
    int txMapVBO[]=new int[1];
    int attributeTextureCoordLocation;
    short[] indicesArray={0,1,2,2,3,0};
    ShortBuffer indicesShortBuffer;
    int indicesVBO[]=new int[1];
    float[] projectionMatrix=new float[16];
    int projectionMatrixHandle;
    float[] physicsMatrix= new float[16];
    int physicsMatrixHandle;
    Bitmap bitmap;
    int textureId;
    int samplerHandle;
    long time;
    long lastTime;
    float deltaTime;
    float posx, posy, velx, vely, accx, accy;
    String vertexSharderSource = 
            "attribute vec4 attributePosition;    \n"
          + "uniform mat4 projectionMatrix;                 \n" 
          + "uniform mat4 physicsMatrix;                 \n"
          + "attribute vec2 attributeTextureCoord;\n"
          + "varying vec2 varyingTextureCoord;    \n"
          + "void main()                          \n"
          + "{                                    \n"
          + "   gl_Position = projectionMatrix*physicsMatrix*attributePosition;  \n"
          + "   varyingTextureCoord.x=attributeTextureCoord.x;    \n"
          + "   varyingTextureCoord.y=attributeTextureCoord.y;    \n"
          + "}                            \n";
    String fragmentShaderSource = 
            "precision mediump float;					  \n"
          + "varying vec2 varyingTextureCoord; 			   \n"
          + "uniform sampler2D samplerTexture;					\n" 
          + "void main()                                  \n"
          + "{                                            \n"
          + "  gl_FragColor = texture2D(samplerTexture, varyingTextureCoord); \n"
          + "}                                            \n";
int frame;
float duration;
float backgroundArray[]={0.0f,0.0f,0.0f,1600.0f,0.0f,0.0f,1600.0f,480.0f,0.0f,0.0f,480.0f,0.0f};
FloatBuffer backgroundBuffer;
int backgroundVBO[]=new int[1];
float backgroundTxmapArray[]={0.0f,1.0f,1.0f,1.0f,1.0f,0.0f,0.0f,0.0f};
FloatBuffer backgroundTxmapBuffer;
int backgroundTxmapVBO[]=new int[1];
int backgroundtextureId;
float turtleArray[]={-80.0f,-80.0f,0,80.0f,-80.0f,0,80.0f,80.0f,0,-80.0f,80.0f,0};
FloatBuffer turtleBuffer;
int turtleVBO[]=new int[1];
float turtleTxmapArray[]={0.0f,1.0f,0.25f,1.0f,0.25f,0.0f,0.0f,0.0f};
FloatBuffer turtleTxmapBuffer;
int turtleTxmapVBO[]=new int[1];
int turtletextureId;
float Tposx, Tposy, Tvelx, Tvely, Taccx, Taccy;
boolean collision=false;

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
    //allocate num of vertices*3 members XYZ*4bytes each float
    backgroundBuffer = ByteBuffer.allocateDirect(backgroundArray.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    backgroundBuffer.put(backgroundArray).position(0);
    //allocate number of txMap coords*4bytes
    backgroundTxmapBuffer = ByteBuffer.allocateDirect(backgroundTxmapArray.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    backgroundTxmapBuffer.put(backgroundTxmapArray).position(0);
    //allocate num of vertices*3 members XYZ*4bytes each float
    turtleBuffer = ByteBuffer.allocateDirect(turtleArray.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    turtleBuffer.put(turtleArray).position(0);
    //allocate number of txMap coords*4bytes
    turtleTxmapBuffer = ByteBuffer.allocateDirect(turtleTxmapArray.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    turtleTxmapBuffer.put(turtleTxmapArray).position(0);
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
    //GLES20.glBindAttribLocation(prog, 0, "attributePosition");
    //Bind Texture Coord to attribute location 1
    //GLES20.glBindAttribLocation(prog, 1, "attributeTextureCoord");
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
    //enable blend
    GLES20.glEnable(GL10.GL_BLEND);  
	//set blending source and function
	GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    //open asset to bitmap
    try {
		 AssetManager assetManager = getAssets();
	 	 InputStream inputStream = assetManager.open("cavemanAtlas512x512.png");
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
    
    //generate verticesVBO(1VBO,array to store handle, offset on array)
    GLES20.glGenBuffers(1, verticesVBO,0);
    //bind vertices VBO(array type,handle)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesVBO[0]);
    //copy FloatBuffer to VBO(type,size in bytes,source,usage)
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesFloatBuffer.capacity()*4 , verticesFloatBuffer, GLES20.GL_DYNAMIC_DRAW);
    //unbind VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    //generate txMapVBO (1VBO,array to store handle, offset on array)
    GLES20.glGenBuffers(1, txMapVBO,0);
    //bind txMap VBO(array type,handle)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, txMapVBO[0]);
    //copy FloatBuffer to VBO(type,size in bytes,source,usage)
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, txMapFloatBuffer.capacity()*4 , txMapFloatBuffer, GLES20.GL_DYNAMIC_DRAW);
    //unbind VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    //generate indicesVBO(1VBO,array to store handle, offset on array)
    GLES20.glGenBuffers(1, indicesVBO,0);
    //bind indices VBO(array type,handle)
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesVBO[0]);
    //copy ShortBuffer to VBO(type,size in bytes,source,usage)
    GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesShortBuffer.capacity()*2 , indicesShortBuffer, GLES20.GL_DYNAMIC_DRAW);
    //unbind VBO
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);  
    
    //open asset to bitmap
    try {
		 AssetManager assetManager = getAssets();
	 	 InputStream inputStream = assetManager.open("back512square.png");
	     bitmap = BitmapFactory.decodeStream(inputStream);
	     inputStream.close();
	     } catch (IOException e) {}
    //get the texture handle
    int[] backtextures = new int[1];
    //generate a texture on client mem, return handle
    GLES20.glGenTextures(1, backtextures, 0);
    backgroundtextureId = backtextures[0];
    //bind texture for next operations
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, backgroundtextureId);
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
    //generate verticesVBO(1VBO,array to store handle, offset on array)
    GLES20.glGenBuffers(1, backgroundVBO,0);
    //bind vertices VBO(array type,handle)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, backgroundVBO[0]);
    //copy FloatBuffer to VBO(type,size in bytes,source,usage)
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, backgroundBuffer.capacity()*4 , backgroundBuffer, GLES20.GL_DYNAMIC_DRAW);
    //unbind VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    //generate txMapVBO (1VBO,array to store handle, offset on array)
    GLES20.glGenBuffers(1, backgroundTxmapVBO,0);
    //bind txMap VBO(array type,handle)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, backgroundTxmapVBO[0]);
    //copy FloatBuffer to VBO(type,size in bytes,source,usage)
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, backgroundTxmapBuffer.capacity()*4 , backgroundTxmapBuffer, GLES20.GL_DYNAMIC_DRAW);
    //unbind VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    //open asset to bitmap
    try {
		 AssetManager assetManager = getAssets();
		 InputStream inputStream = assetManager.open("turtle512square.png");
	     bitmap = BitmapFactory.decodeStream(inputStream);
	     inputStream.close();
	     } catch (IOException e) {}
    //get the texture handle
    int[] turtletextures = new int[1];
    //generate a texture on client mem, return handle
    GLES20.glGenTextures(1, turtletextures, 0);
    turtletextureId = turtletextures[0];
    //bind texture for next operations
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, turtletextureId);
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
    //generate verticesVBO(1VBO,array to store handle, offset on array)
    GLES20.glGenBuffers(1, turtleVBO,0);
    //bind vertices VBO(array type,handle)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, turtleVBO[0]);
    //copy FloatBuffer to VBO(type,size in bytes,source,usage)
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, turtleBuffer.capacity()*4 , turtleBuffer, GLES20.GL_DYNAMIC_DRAW);
    //unbind VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    //generate txMapVBO (1VBO,array to store handle, offset on array)
    GLES20.glGenBuffers(1, turtleTxmapVBO,0);
    //bind txMap VBO(array type,handle)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, turtleTxmapVBO[0]);
    //copy FloatBuffer to VBO(type,size in bytes,source,usage)
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, turtleTxmapBuffer.capacity()*4 , turtleTxmapBuffer, GLES20.GL_DYNAMIC_DRAW);
    //unbind VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    
    //caveman initial settings
	//set initial position, velocity, acceleration
    posx=200;
	posy=100;
    //turtle initial settings
	//set initial position, velocity, acceleration
    Tposx=400;
	Tposy=100;
	Tvelx=300;
	Tvely=600;
	Taccx=0;
	Taccy=-980f;
	
	//initialize last time
	lastTime=System.currentTimeMillis();
    }

    public void onDrawFrame(GL10 gl)
    {//returns current time in a long
	time=System.currentTimeMillis();
	//float=long-long/float(to milliseconds)
	deltaTime=(time-lastTime)/1000.0f;
	//store last time for next frame
	lastTime=time;
     // Clear the color 
     GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
     
     //set program and get all locations
     GLES20.glUseProgram(program);
     //return attribute location to handle(program,attribute name)
     attributePositionLocation=GLES20.glGetAttribLocation(program, "attributePosition");
     //if not found handle returned with -1
     if (attributePositionLocation == -1) {
         throw new RuntimeException("Could not get attributePosition location");
     }
     //enable attrib on location (attributePositionLocation)
     GLES20.glEnableVertexAttribArray(attributePositionLocation); 
     //Log.d("attPosLoc", ""+attributePositionLocation);
   //return attribute location to handle(program,attribute name)
     attributeTextureCoordLocation=GLES20.glGetAttribLocation(program, "attributeTextureCoord");
     //if not found handle returned with -1
     if (attributeTextureCoordLocation == -1) {
         throw new RuntimeException("Could not get attributeTextureCoord Location");
     }
     //enable attrib on location (attributeTextureCoordLocation)
     GLES20.glEnableVertexAttribArray(attributeTextureCoordLocation);     
     //get handle to Sampler Uniform location
     samplerHandle=GLES20.glGetUniformLocation(program, "samplerTexture");
     //if Sampler Uniform location not found handle is returned -1
     if (samplerHandle == -1) {
         throw new RuntimeException("Could not get samplerTexture location");
     }
     //get Uniform Handle(program,uniformName) 
     projectionMatrixHandle = GLES20.glGetUniformLocation(program, "projectionMatrix");
     //if Uniform location not found handle is returned -1
     if (projectionMatrixHandle == -1) {
         throw new RuntimeException("Could not get projectionMatrix location");
     }
     //get Uniform Handle(program,uniformName) 
     physicsMatrixHandle = GLES20.glGetUniformLocation(program, "physicsMatrix");
     //if Uniform location not found handle is returned -1
     if (physicsMatrixHandle == -1) {
         throw new RuntimeException("Could not get physicsMatrix location");
     }
     //get max texture Units
     //int[] maxt=new int[1];
     //GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, maxt, 0);
     //Log.d("max texture image units", ""+maxt[0]);
     //get max texture Size
     //int[] maxNum = new int[1];
     //GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxNum, 0);
     //Log.d("max texture size", ""+maxNum[0]);
     
     
     //check if object is between 0+surfaceWidth/2 and 1600-surfaceWidth/2
   	if(posx>400 & posx<1200){
   	//follow object posx-surfaceWidth/2 ,0 offset on Y
   	updateCam(posx-400,0);}
     
     
     //set background data and draw
     //bind background vertices
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, backgroundVBO[0]);
     //set attribPointer(location,3membersXYZ,float,norm=false,stride=0,offset on bound VBO)
     GLES20.glVertexAttribPointer(attributePositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);         
     //bind background txmap
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, backgroundTxmapVBO[0]);
     //set attribPointer(location,2membersTxTy,float,norm=false,stride=0,offsetbytes on VBO)
     GLES20.glVertexAttribPointer(attributeTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 0, 0);
     //set active texture unit
     GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
     //bind a (2Dtexture,Id) to the active texture unit 
     GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, backgroundtextureId);
     //set the texture unit to fetch from 0 to max Texture units
     GLES20.glUniform1i(samplerHandle, 0);
     //set the Uniform data source(Handle,elements to read,false,source,offset)
     GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);
     //background doesnot need physics so identity  
     Matrix.setIdentityM(physicsMatrix, 0);     
     //set the Uniform data source(Handle,elements to read,false,source,offset)
     GLES20.glUniformMatrix4fv(physicsMatrixHandle, 1, false, physicsMatrix, 0);     
     //Bind indices VBO
     GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesVBO[0]);
     //drawelements(type,number of indices,dataType, indicesVBO offset)
     GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesArray.length, GLES20.GL_UNSIGNED_SHORT, 0);
     
     //set caveman data and draw
     //bind vertices VBO
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesVBO[0]);
     //set attribPointer(location,2membersTxTy,float,norm=false,stride=0,offsetbytes on VBO)
     GLES20.glVertexAttribPointer(attributePositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
     //bind txMapVBO
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, txMapVBO[0]);
     //set attribPointer(location,2membersTxTy,float,norm=false,stride=0,offsetbytes on VBO)
     GLES20.glVertexAttribPointer(attributeTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 0, frame*32);
     //set active texture unit
     GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
     //bind a (2Dtexture,Id) to the active texture unit 
     GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
     //set the texture unit to fetch from 0 to max Texture units
     GLES20.glUniform1i(samplerHandle, 0); 
     //set the Uniform data source(Handle,elements to read,false,source,offset)
     GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);
     //Update physics matrix with object position
     Matrix.setIdentityM(physicsMatrix, 0);
	 Matrix.translateM(physicsMatrix, 0, posx, posy, 0);
     //set the Uniform data source(Handle,elements to read,false,source,offset)
     GLES20.glUniformMatrix4fv(physicsMatrixHandle, 1, false, physicsMatrix, 0);
     //Bind indices VBO
     GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesVBO[0]);
     //drawelements(type,number of indices,dataType, indicesVBO offset)
     GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesArray.length, GLES20.GL_UNSIGNED_SHORT, 0);
     
     //set turtle data and draw
     //bind turtle VBO
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, turtleVBO[0]);
     //set attribPointer(location,2membersTxTy,float,norm=false,stride=0,offsetbytes on VBO)
     GLES20.glVertexAttribPointer(attributePositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
     //bind turtleTxmapVBO
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, turtleTxmapVBO[0]);
     //set attribPointer(location,2membersTxTy,float,norm=false,stride=0,offsetbytes on VBO)
     GLES20.glVertexAttribPointer(attributeTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 0, 0);
     //set active texture unit
     GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
     //bind a (2Dtexture,Id) to the active texture unit 
     GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, turtletextureId);
     //set the texture unit to fetch from 0 to max Texture units
     GLES20.glUniform1i(samplerHandle, 0); 
     //set the Uniform data source(Handle,elements to read,false,source,offset)
     GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);
     //Update physics matrix with object position
     Matrix.setIdentityM(physicsMatrix, 0);
	 Matrix.translateM(physicsMatrix, 0, Tposx, Tposy, 0);
     //set the Uniform data source(Handle,elements to read,false,source,offset)
     GLES20.glUniformMatrix4fv(physicsMatrixHandle, 1, false, physicsMatrix, 0);
     //Bind indices VBO
     GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesVBO[0]);
     //drawelements(type,number of indices,dataType, indicesVBO offset)
     GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesArray.length, GLES20.GL_UNSIGNED_SHORT, 0);
     
     //physics should be on an independent thread :(
     //caveman physics
     //set vel and direction to touchpoint		
     if(touchX<posx){velx=-200;}
     if(touchX>posx){velx=200; }
     //if no finger on screen vel=0
     //or posx inside your finger(50px around touchx)
     if(!touch||(touchX-posx)*(touchX-posx)<50*50){velx=0;}
     //physics update
     velx=velx+(accx*deltaTime);
     vely=vely+(accy*deltaTime);
     posx=posx+(velx*deltaTime);
     posy=posy+(vely*deltaTime);
     //debug variables on LogCat
     //Log.d("var", "x:"+posx+"y:"+posy+"vx:"+velx+"vy:"+vely+"ax:"+accx+"ay:"+accy+"t:"+deltaTime);	
     //turtle physics
     if(Tposx<0){Tvelx=300;}if(Tposx>1600){Tvelx=-300;}
     if (Tposy<=100){Tvely=400;}
     Tvelx=Tvelx+(Taccx*deltaTime);
     Tvely=Tvely+(Taccy*deltaTime);
     Tposx=Tposx+(Tvelx*deltaTime);
     Tposy=Tposy+(Tvely*deltaTime);
     //collision test
     collision=((Tposx-posx)*(Tposx-posx))<160*160;
     //Log.d("collision",""+collision);
     //animate depending state
     if(collision){animate(fight,velx>0,deltaTime);}else{
    	//if velocityX not 0 walk anim
         if(velx!=0){animate(walk,velx>0,deltaTime);} 
     }
     
    }

    int walk=0;//offset to walk first frame
	int fight=4;//offset to fight first frame
	int walkstone=7;//offset to walkstone first frame
	int stone=11;//offset to stone first frame
	int currentState;//store current animation
	boolean currentDir;//store current direction
	private void animate(int animState, boolean animDir, float deltaTime){
	//if animation changes or direction changes
	if (currentState!=animState || animDir!=currentDir)
	//if direction is positive X offset to second set off frames with offset 14
	{if(animDir){frame=animState+14;}
	//else first set of frames, then store dir,state, duration=0 to start counting again
	else{frame=animState;} currentDir=animDir;currentState=animState;duration=0;}
	//if duration >250 miliseconds change to next frame, reset time counter
	if(duration>0.25){
	frame++; 
	duration=0;
	//if animation is walk or walkstone and frame has used the 4 frames return to first anim frame
	if((currentState==walk||currentState==walkstone) && (frame==currentState+4||frame-14==currentState+4)){frame=frame-4;}
	//if animation is fight or stone and frame has used the 3 frames return to first anim frame
	if((currentState==fight||currentState==stone) && (frame==currentState+3||frame-14==currentState+3)){frame=frame-3;}
	} //increase time counter
	duration+=deltaTime;//Log.d("frame",""+frame);
	}
    
	float camx,camy;
	public void updateCam(float cx, float cy){ 
		camx=cx; camy=cy;
		 Matrix.setIdentityM(projectionMatrix, 0);
		 Matrix.orthoM(projectionMatrix, 0, 0+cx, 800+cx, 0+cy, 480+cy, 1, -1);
	}
	
	float surfaceWidth,surfaceHeight;
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {//set viewport to whole surface
    surfaceWidth=width;
    surfaceHeight=height;
    GLES20.glViewport(0, 0, width, height);
    Matrix.setIdentityM(projectionMatrix, 0);
    Matrix.orthoM(projectionMatrix, 0, 0, 800, 0, 480, 1, -1);
    }

    float touchX, touchY;
    boolean touch;
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		for(int i=0; i < event.getPointerCount(); i++){
			if(event.getPointerId(i)==0){
				//touchX=(event.getX(i) / surfaceWidth)*800;
			    //touchY=(1-event.getY(i) / surfaceHeight)*480;
			    //translate touch to cam position
			    touchX = ((event.getX(i) / surfaceWidth) *800 + camx);
			    touchY = ((1-event.getY(i) / surfaceHeight) *480 + camy);
			    
			  //  Log.d("Touch","X:"+touchX+" Y:"+touchY);
			    if(event.getActionMasked()==MotionEvent.ACTION_UP){touch=false;}else{touch=true;}
			}
		}
		return true;
	}
}
}
