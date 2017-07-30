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
import android.view.Window;
import android.view.WindowManager;

public class GLES20physics extends Activity {
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
		Log.d("openGL ES 2.0 Support: ",""+ detectOpenGLES20());//checkfor ES2.0
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
    float[] verticesArray = {-100f,-100f,0f,100f,-100f,0f,100f,100f,0f,-100f,100f,0f};
    FloatBuffer verticesFloatBuffer;
    int verticesVBO[]=new int[1];
    int attributePositionLocation;
    float[] txMapArray={0f,1f,1f,1f,1f,0f,0f,0f};
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
    
	//set initial position, velocity, acceleration
    posx=200;
	posy=100;
	velx=400;
	vely=600;
	accx=0;
	accy=-980f;
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
     //indicate which program to use on draw
     GLES20.glUseProgram(program);
     
     //bind vertices VBO
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesVBO[0]);
     //return attribute location to handle(program,attribute name)
     attributePositionLocation=GLES20.glGetAttribLocation(program, "attributePosition");
     //if not found handle returned with -1
     if (attributePositionLocation == -1) {
         throw new RuntimeException("Could not get attributePosition location");
     }
     //enable attrib on location (attributePositionLocation)
     GLES20.glEnableVertexAttribArray(attributePositionLocation);     
     //set attribPointer(location,3membersXYZ,float,norm=false,stride=0,offset on bound VBO)
     GLES20.glVertexAttribPointer(attributePositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
     //debug attribute location
     //Log.d("attPosLoc", ""+attributePositionLocation);
     
     //bind txMapVBO
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, txMapVBO[0]);
     //return attribute location to handle(program,attribute name)
     attributeTextureCoordLocation=GLES20.glGetAttribLocation(program, "attributeTextureCoord");
     //if not found handle returned with -1
     if (attributeTextureCoordLocation == -1) {
         throw new RuntimeException("Could not get attributeTextureCoord Location");
     }
     //enable attrib on location (attributeTextureCoordLocation)
     GLES20.glEnableVertexAttribArray(attributeTextureCoordLocation);
     //set attribPointer(location,2membersTxTy,float,norm=false,stride=0,offset on bound VBO)
     GLES20.glVertexAttribPointer(attributeTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 0, 0);
     //debug attribute location
     //Log.d("attTxCoorLoc", ""+attributeTextureCoordLocation);

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
     projectionMatrixHandle = GLES20.glGetUniformLocation(program, "projectionMatrix");
     //if Uniform location not found handle is returned -1
     if (projectionMatrixHandle == -1) {
         throw new RuntimeException("Could not get projectionMatrix location");
     }
     //set the Uniform data source(Handle,elements to read,false,source,offset)
     GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);
       

     //get Uniform Handle(program,uniformName) 
     physicsMatrixHandle = GLES20.glGetUniformLocation(program, "physicsMatrix");
     //if Uniform location not found handle is returned -1
     if (physicsMatrixHandle == -1) {
         throw new RuntimeException("Could not get physicsMatrix location");
     }
     //set the Uniform data source(Handle,elements to read,false,source,offset)
     GLES20.glUniformMatrix4fv(physicsMatrixHandle, 1, false, physicsMatrix, 0);
   
     
     //Bind indices VBO
     GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesVBO[0]);
     //drawelements(type,number of indices,dataType, indicesVBO offset)
     GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesArray.length, GLES20.GL_UNSIGNED_SHORT, 0);
     
    //physics should be on an independent thread :(
   //check if posx is going out ofscreen
   		//if (posx<0 | posx>800){velx=-velx;}
   		if(posx<0){velx=400;}if(posx>800){velx=-400;}
   		//check if posy is on the fixed ground 100
   		if (posy<=100){vely=400;}
   		//physics update
   		velx=velx+(accx*deltaTime);
   		vely=vely+(accy*deltaTime);
   		posx=posx+(velx*deltaTime);
   		posy=posy+(vely*deltaTime);
   		//debug variables on LogCat
   		Log.d("var", "x:"+posx+"y:"+posy+"vx:"+velx+"vy:"+vely+"ax:"+accx+"ay:"+accy+"t:"+deltaTime);
   		//set identity to physicsMatrix
   		Matrix.setIdentityM(physicsMatrix, 0);
   		//transalte matrix to the new pos x,y
   		Matrix.translateM(physicsMatrix, 0, posx, posy, 0);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height)
    {//set viewport to whole surface
    GLES20.glViewport(0, 0, width, height);
    Matrix.setIdentityM(projectionMatrix, 0);
    Matrix.orthoM(projectionMatrix, 0, 0, 800, 0, 480, 1, -1);
    }
}
}
