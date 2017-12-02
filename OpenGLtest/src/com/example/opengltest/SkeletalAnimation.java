package com.example.opengltest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class SkeletalAnimation extends Activity {
	SurfaceRenderer render;
	GLSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);//keep screen on
		glSurfaceView = new GLSurfaceView(this); //initialize surface
		render=new SurfaceRenderer(this); //initialize renderer 
		glSurfaceView.setEGLContextClientVersion(2);//set EGLcontext
		glSurfaceView.setRenderer(render);//set renderer
		glSurfaceView.setOnTouchListener(render);
		Log.d("openGL ES 2.0 Support: ",""+ detectOpenGLES20());
		setContentView(glSurfaceView);//set surface as content
	}
	
	@Override
	public void onResume() {
	super.onResume();
	glSurfaceView.onResume();
	}

	@Override
	public void onPause() {
	super.onPause();
	glSurfaceView.onPause();
	}

	 private boolean detectOpenGLES20() 
	 {//detect if device support GLES2.0
	 ActivityManager activityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	 ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
	 return (configurationInfo.reqGlEsVersion >= 0x20000);	 }



public class SurfaceRenderer implements GLSurfaceView.Renderer,OnTouchListener{
	int program,program2;
    FloatBuffer verticesFloatBuffer;
    int verticesVBO[]=new int[1];
    int attributePositionLocation;
    FloatBuffer txFloatBuffer;
    int txVBO[]=new int[1];
    int attributeTextureCoordLocation;
    FloatBuffer normalFloatBuffer;
    int normalVBO[]=new int[1];
    int attributeNormalLocation;
    FloatBuffer weightsFloatBuffer;
    int weightsVBO[]=new int[1];
    int attributeWeightsLocation;
    float[] projectionMatrix=new float[16];
    int projectionMatrixHandle;
    float[] physicsMatrix= new float[16];
    int physicsMatrixHandle;
    float[] navigationMatrix= new float[16];
    int navigationMatrixHandle;
    int attributebone12Location;
    int attributebone34Location;
    int jointMatrixHandle;
    int invBindPoseMatrixHandle;
    Bitmap bitmap;
    int textureId;
    int samplerHandle;
    long time;
    long lastTime;
    float deltaTime;
    float angle;
    ObjLoader OBJ; 
    DaeLoader DAE;
    //Loading Screen VertexShader
    String vertexShaderSource = 
            "attribute vec4 attributePosition;     \n"
          + "attribute vec3 attributeNormal;       \n"
          + "attribute vec2 attributeTextureCoord; \n"
          + "varying vec2 varyingTextureCoord;     \n"
          + "varying vec4 varyingLight;            \n"
          + "uniform mat4 projectionMatrix;        \n" 
          + "uniform mat4 navigationMatrix;        \n" 
          + "uniform mat4 physicsMatrix;           \n"
          + "vec3 lightPos=vec3(0.5,0.5,2.5);      \n"
          + "vec3 eyePos=vec3(0.0,0.0,0.0);        \n"
          + "const float zero=0.0;                 \n"
          + "const float one=1.0;                  \n"
          + "float emissive=0.0;                   \n"
          + "float ambient=0.2;                    \n"
          + "float diffuse=0.6;                   \n" 
          + "float specular=1.2;                   \n"
          + "float specularPower=200.0;                \n"
          + "vec4 ambientColor=vec4(1.0,1.0,1.0,1.0); \n"
          + "float constantAtt=0.1;                   \n"
          + "float linearAtt=0.05;                    \n"
          + "float quadraticAtt=0.02;                 \n"
          + "void main()                              \n"
          + "{  gl_Position = projectionMatrix*navigationMatrix*physicsMatrix*attributePosition;  \n" 
          + "   mat4 navigationRotationM=mat4(navigationMatrix[0], navigationMatrix[1], navigationMatrix[2], navigationMatrix[3]);                       \n"
          + "   navigationRotationM[3]=vec4(zero,zero,zero,one);                    \n" 
          + "   vec4 tempNormal=vec4(attributeNormal.xyz,one);                   \n"
          + "   mat4 physicsRotationM=mat4(physicsMatrix[0], physicsMatrix[1], physicsMatrix[2], physicsMatrix[3]);                       \n"
          + "   physicsRotationM[3]=vec4(zero,zero,zero,one);                    \n"
          + "   tempNormal=projectionMatrix*navigationRotationM*physicsRotationM*tempNormal;         \n"
          + "   vec3 normalizedTempNormal=normalize(tempNormal.xyz);             \n"
          //directional light navigation rotation:
 	      + "   lightPos=(projectionMatrix*navigationRotationM*vec4(lightPos,one)).xyz;  \n"   
          //directional light normalize vector:
          + " 	vec3 lightDir=normalize(lightPos);                                       \n"             
          //spot light navigation translate and rotate:
         // + "   lightPos=(projectionMatrix*navigationMatrix*vec4(lightPos,one)).xyz;  \n"   
          //spot light get distance from light to vertex:
          //+ " 	float distance=length(lightPos-gl_Position.xyz);                 \n"    
          //spot light calculate attenuation based on distance
          //+ " 	float attFactor=one/(constantAtt+linearAtt*distance+quadraticAtt*distance*distance); \n" 
          //spot light get direction from spotlight to vertex:
          //+ " 	vec3 lightDir=normalize(lightPos-gl_Position.xyz);               \n"                     
          + "   float lightDiffuse=max(dot(normalizedTempNormal,lightDir),zero); \n"
          + "   vec3 eyeDir=normalize(eyePos-gl_Position.xyz); \n"
          + "   vec3 halfPlane=normalize(lightDir+eyeDir);            \n"
          + "   float lightSpecular=pow(max(dot(normalizedTempNormal,halfPlane),zero),specularPower);  \n"
          + "   varyingLight=emissive+(ambient*ambientColor)+(diffuse*lightDiffuse*ambientColor)+(specular*lightSpecular*ambientColor); \n"
         // + "   varyingLight*=attFactor;                                         \n"  //spot light
          + "   varyingTextureCoord=attributeTextureCoord;                       \n"
          + "}                                                                   \n";
    
    //spotlight shader not used now :-(
    String vertexShaderSource3 = 
            "attribute vec4 attributePosition;     \n"
          + "attribute vec3 attributeNormal;       \n"
          + "attribute vec2 attributeTextureCoord; \n"
          + "varying vec2 varyingTextureCoord;     \n"
          + "varying vec4 varyingLight;            \n"
          + "uniform mat4 projectionMatrix;        \n" 
          + "uniform mat4 navigationMatrix;        \n" 
          + "uniform mat4 physicsMatrix;           \n"
          + "vec3 lightPos=vec3(0.5,0.5,-4.5);      \n"
          + "vec3 eyePos=vec3(0.0,0.0,0.0);        \n"
          + "const float zero=0.0;                 \n"
          + "const float one=1.0;                  \n"
          + "float emissive=0.0;                   \n"
          + "float ambient=0.2;                    \n"
          + "float diffuse=0.6;                   \n" 
          + "float specular=1.2;                   \n"
          + "float specularPower=200.0;                \n"
          + "vec4 ambientColor=vec4(1.0,1.0,1.0,1.0); \n"
          + "float constantAtt=0.1;                   \n"
          + "float linearAtt=0.05;                    \n"
          + "float quadraticAtt=0.02;                 \n"
          + "void main()                              \n"
          + "{  gl_Position = projectionMatrix*navigationMatrix*physicsMatrix*attributePosition;  \n" 
          + "   mat4 navigationRotationM=mat4(navigationMatrix[0], navigationMatrix[1], navigationMatrix[2], navigationMatrix[3]);                       \n"
          + "   navigationRotationM[3]=vec4(zero,zero,zero,one);                    \n" 
          + "   vec4 tempNormal=vec4(attributeNormal.xyz,one);                   \n"
          + "   mat4 physicsRotationM=mat4(physicsMatrix[0], physicsMatrix[1], physicsMatrix[2], physicsMatrix[3]);                       \n"
          + "   physicsRotationM[3]=vec4(zero,zero,zero,one);                    \n"
          + "   tempNormal=projectionMatrix*navigationRotationM*physicsRotationM*tempNormal;         \n"
          + "   vec3 normalizedTempNormal=normalize(tempNormal.xyz);             \n"
          //directional light navigation rotation:
 	      //+ "   lightPos=(projectionMatrix*navigationRotationM*vec4(lightPos,one)).xyz;  \n"   
          //directional light normalize vector:
          //+ " 	vec3 lightDir=normalize(lightPos);                                       \n"             
          //spot light navigation translate and rotate:
          + "   lightPos=(projectionMatrix*navigationMatrix*vec4(lightPos,one)).xyz;  \n"   
          //spot light get distance from light to vertex:
          + " 	float distance=length(lightPos-gl_Position.xyz);                 \n"    
          //spot light calculate attenuation based on distance
          + " 	float attFactor=one/(constantAtt+linearAtt*distance+quadraticAtt*distance*distance); \n" 
          //spot light get direction from spotlight to vertex:
          + " 	vec3 lightDir=normalize(lightPos-gl_Position.xyz);               \n"                     
          + "   float lightDiffuse=max(dot(normalizedTempNormal,lightDir),zero); \n"
          + "   vec3 eyeDir=normalize(eyePos-gl_Position.xyz); \n"
          + "   vec3 halfPlane=normalize(lightDir+eyeDir);            \n"
          + "   float lightSpecular=pow(max(dot(normalizedTempNormal,halfPlane),zero),specularPower);  \n"
          + "   varyingLight=emissive+(ambient*ambientColor)+(diffuse*lightDiffuse*ambientColor)+(specular*lightSpecular*ambientColor); \n"
          + "   varyingLight*=attFactor;                                         \n"  //spot light
          + "   varyingTextureCoord=attributeTextureCoord;                       \n"
          + "}                                                                   \n";
    
    //Skeletal animation Vertex Shader
    String vertexShaderSource2 = 
            "attribute vec4 attributePosition;     \n"
          + "attribute vec3 attributeNormal;       \n"
          + "attribute vec2 attributeTextureCoord; \n"
          + "attribute vec4 attributebone12; \n"
          + "attribute vec4 attributebone34; \n"
          + "varying vec2 varyingTextureCoord;     \n"
          + "varying vec4 varyingLight;            \n"
          + "uniform mat4 projectionMatrix;        \n" 
          + "uniform mat4 jointMatrix[8];          \n" 
          + "uniform mat4 invBindPoseMatrix[8];    \n" 
          + "uniform mat4 navigationMatrix;        \n" 
          + "uniform mat4 physicsMatrix;           \n"
          + "vec3 lightPos=vec3(1.5,1.5,1.0);      \n"
          + "vec3 eyePos=vec3(0.0,0.0,0.0);        \n"
          + "const float zero=0.0;                 \n"
          + "const float one=1.0;                  \n"
          + "float emissive=0.0;                   \n"
          + "float ambient=0.1;                    \n"
          + "float diffuse=0.5;                   \n" 
          + "float specular=0.05;                   \n"
          + "float specularPower=600.0;                \n"
          + "vec4 ambientColor=vec4(1.0,1.0,1.0,1.0); \n"
          + "vec4 tempPos=vec4(0.0,0.0,0.0,0.0);      \n"
          + "vec4 tempNormal=vec4(0.0,0.0,0.0,0.0);      \n"
          + "float constantAtt=0.1;                   \n"
          + "float linearAtt=0.05;                    \n"
          + "float quadraticAtt=0.02;                 \n"
          + "void main()                              \n"
          + "{                            \n"
          + " int i; for(i=0;i<8;i++)     \n"
          + " {  \n"
          + "   if(int(attributebone12.x)==i){tempPos+=jointMatrix[i]*invBindPoseMatrix[i]*attributePosition*attributebone12.y;\n" 
          + "   tempNormal+=jointMatrix[i]*invBindPoseMatrix[i]*vec4(attributeNormal.xyz,one)*attributebone12.y;}\n"
          + "   if(int(attributebone12.z)==i){tempPos+=jointMatrix[i]*invBindPoseMatrix[i]*attributePosition*attributebone12.w;\n"
          + "   tempNormal+=jointMatrix[i]*invBindPoseMatrix[i]*vec4(attributeNormal.xyz,one)*attributebone12.w;}\n"
          + "   if(int(attributebone34.x)==i){tempPos+=jointMatrix[i]*invBindPoseMatrix[i]*attributePosition*attributebone34.y;\n"
          + "   tempNormal+=jointMatrix[i]*invBindPoseMatrix[i]*vec4(attributeNormal.xyz,one)*attributebone34.y;}\n"
          + "   if(int(attributebone34.z)==i){tempPos+=jointMatrix[i]*invBindPoseMatrix[i]*attributePosition*attributebone34.w;\n"
 		  + "   tempNormal+=jointMatrix[i]*invBindPoseMatrix[i]*vec4(attributeNormal.xyz,one)*attributebone34.w;}\n"
          + "  }                          \n"
          + "   gl_Position = projectionMatrix*navigationMatrix*physicsMatrix*tempPos;  \n" 
          + "   mat4 navigationRotationM=mat4(navigationMatrix[0], navigationMatrix[1], navigationMatrix[2], navigationMatrix[3]);                       \n"
		  + "   navigationRotationM[3]=vec4(zero,zero,zero,one);                    \n" 
          + "   mat4 physicsRotationM=mat4(physicsMatrix[0], physicsMatrix[1], physicsMatrix[2], physicsMatrix[3]);                       \n"
          + "   physicsRotationM[3]=vec4(zero,zero,zero,one);                    \n"
          + "   tempNormal=projectionMatrix*navigationRotationM*physicsRotationM*tempNormal;         \n"
          + "   vec3 normalizedTempNormal=normalize(tempNormal.xyz);             \n"
          //directional light navigation rotation:
 	      + "   lightPos=(projectionMatrix*navigationRotationM*vec4(lightPos,one)).xyz;  \n"   
          //directional light normalize vector:
          + " 	vec3 lightDir=normalize(lightPos);                                       \n"             
          //spot light navigation translate and rotate source position:
          // + "   lightPos=(projectionMatrix*navigationMatrix*vec4(lightPos,one)).xyz;  \n"   
          //spot light get distance from light source to vertex:
          //+ " 	float distance=length(lightPos-gl_Position.xyz);                 \n"    
          //spot light calculate attenuation based on distance
          //+ " 	float attFactor=one/(constantAtt+linearAtt*distance+quadraticAtt*distance*distance); \n" 
          //spot light get direction from spotlight to vertex:
          //+ " 	vec3 lightDir=normalize(lightPos-gl_Position.xyz);               \n"                     
          + "   float lightDiffuse=max(dot(normalizedTempNormal,lightDir),zero); \n"
          + "   vec3 eyeDir=normalize(eyePos-gl_Position.xyz); \n"
          + "   vec3 halfPlane=normalize(lightDir+eyeDir);            \n"
          + "   float lightSpecular=pow(max(dot(normalizedTempNormal,halfPlane),zero),specularPower);  \n"
          + "   varyingLight=emissive+(ambient*ambientColor)+(diffuse*lightDiffuse*ambientColor)+(specular*lightSpecular*ambientColor); \n"
          //spot light attenuation factor
          // + "   varyingLight*=attFactor;                                         \n"  
          + "   varyingTextureCoord=attributeTextureCoord;                       \n"
          + "}                                                                   \n"; 
     //fragment shader shared by both drawings
     String fragmentShaderSource = 
            "precision mediump float;					 \n"
          + "varying vec2 varyingTextureCoord; 			 \n"
          + "varying vec4 varyingLight;   				 \n"
          + "uniform sampler2D samplerTexture;			 \n" 
          + "void main()                                 \n"
          + "{                                           \n"
          + "  gl_FragColor = texture2D(samplerTexture, varyingTextureCoord); \n"
          + "  gl_FragColor.rgb*=varyingLight.rgb;        \n"
          + "}                                            \n";

   float posX=0,posY=0,posZ=-6;
   float velX=0f,velY=0f,velZ=3f;
   float accX,accY,accZ;
   float auxTime;   
   boolean loaded=false;
   FloatBuffer loadingV,loadingN,loadingT; 
   int loadingtextureId;
   int loadingVVBO[]=new int[1];
   int loadingTVBO[]=new int[1];
   int loadingNVBO[]=new int[1];
   int loadingAngle;

   public SurfaceRenderer(Context context)
    {//initialize .dae file loader
	DAE = new DaeLoader(context);
	//initialize .obj file loader
    OBJ=new ObjLoader(context);
    OBJ.load("ball.obj");//load sphere for loading screen
    //create sphere Buffers
    //allocate num of vertices*3 members XYZ*4bytes each float
    loadingV = ByteBuffer.allocateDirect(OBJ.vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    loadingV.put(OBJ.vertices).position(0);
    //allocate number of txMap coords*4bytes
    loadingT = ByteBuffer.allocateDirect(OBJ.tx.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    loadingT.put(OBJ.tx).position(0);
    //allocate num of normals*3 members XYZ*4bytes each float
    loadingN = ByteBuffer.allocateDirect(OBJ.normal.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    loadingN.put(OBJ.normal).position(0);  
    //initialize and start second thread for loading frames
    loaderThread loader=new loaderThread();
    loader.start();    }
 
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
    shader = 0;}    }
    return shader;    }
    
    //create program method
    private int createProgram(String vSource, String fSource){
    int prog;
    int[] linked = new int[1];
    //load shader(type vertex, source code string)
    int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vSource);
    //load shader(type fragment, source code string)
    int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fSource);
    //create GLES2.0 program
    prog = GLES20.glCreateProgram();
    //if failed return
    if (prog == 0){return 0;}
    //attach vertex shader
    GLES20.glAttachShader(prog, vertexShader);
    //attach fragment shader
    GLES20.glAttachShader(prog, fragmentShader);
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
        return 0;   } 
    return prog;    }
    
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {//create program from shaders for loading screen
    program = createProgram(vertexShaderSource,fragmentShaderSource);
     //set clear color
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    //enable blend
    GLES20.glEnable(GLES20.GL_BLEND);  
	//set blending source and function
	GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	//enable depth test
	GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	//create buffers for loading screen objects
	//open asset to bitmap
	try {
	AssetManager assetManager = getAssets();
	InputStream inputStream = assetManager.open("loading512.png");
	bitmap = BitmapFactory.decodeStream(inputStream);
	inputStream.close();
	     } catch (IOException e) {}
	//get the texture handle
	int[] loadingtextures = new int[1];
	//generate a texture on client mem, return handle
	GLES20.glGenTextures(1, loadingtextures, 0);
	loadingtextureId = loadingtextures[0];
	//bind texture for next operations
	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, loadingtextureId);
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
	//generate mipmap
	GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
	//delete bitmap
	bitmap.recycle();
	//un bind texture
	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	//generate verticesVBO(1VBO,array to store handle, offset on array)
	GLES20.glGenBuffers(1, loadingVVBO,0);
	//bind vertices VBO(array type,handle)
	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, loadingVVBO[0]);
	//copy FloatBuffer to VBO(type,size in bytes,source,usage)
	GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, loadingV.capacity()*4 , loadingV, GLES20.GL_STATIC_DRAW);
	//unbind VBO
	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	//generate normalVBO(1VBO,array to store handle, offset on array)
	GLES20.glGenBuffers(1, loadingNVBO,0);
	//bind normal VBO(array type,handle)
	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, loadingNVBO[0]);
	//copy FloatBuffer to VBO(type,size in bytes,source,usage)
	GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, loadingN.capacity()*4 , loadingN, GLES20.GL_STATIC_DRAW);
	//unbind VBO
	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	//generate txMapVBO (1VBO,array to store handle, offset on array)
	GLES20.glGenBuffers(1, loadingTVBO,0);
	//bind txMap VBO(array type,handle)
	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, loadingTVBO[0]);
	//copy FloatBuffer to VBO(type,size in bytes,source,usage)
	GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, loadingT.capacity()*4 , loadingT, GLES20.GL_STATIC_DRAW);
	//unbind VBO
	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	   
    //initialize last time
	lastTime=System.currentTimeMillis();
    }

boolean VBOcreated=false;
    public void onDrawFrame(GL10 gl)
    { // Clear the color and depth
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    //check if .dae loaded and if VBOs created
    if(loaded && !VBOcreated){ 
    	//open asset to bitmap
    	try {
    		 AssetManager assetManager = getAssets();
    	 	InputStream inputStream = assetManager.open("green.png");
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
    	//generate mipmap
    	GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    	//delete bitmap
    	bitmap.recycle();
    	//un bind texture
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);Log.d("VBO", "VERTEX");
    	//generate verticesVBO(1VBO,array to store handle, offset on array)
    	GLES20.glGenBuffers(1, verticesVBO,0);
    	//bind vertices VBO(array type,handle)
    	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesVBO[0]);
    	//copy FloatBuffer to VBO(type,size in bytes,source,usage)
    	GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesFloatBuffer.capacity()*4 , verticesFloatBuffer, GLES20.GL_STATIC_DRAW);
    	//unbind VBO
    	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);Log.d("VBO", "NORMAL");
    	//generate normalVBO(1VBO,array to store handle, offset on array)
    	GLES20.glGenBuffers(1, normalVBO,0);
    	//bind normal VBO(array type,handle)
    	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalVBO[0]);
    	//copy FloatBuffer to VBO(type,size in bytes,source,usage)
    	GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, normalFloatBuffer.capacity()*4 , normalFloatBuffer, GLES20.GL_STATIC_DRAW);
    	//unbind VBO
    	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);Log.d("VBO", "TXMAP");
    	//generate txMapVBO (1VBO,array to store handle, offset on array)
    	GLES20.glGenBuffers(1, txVBO,0);
    	//bind txMap VBO(array type,handle)
    	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, txVBO[0]);
    	//copy FloatBuffer to VBO(type,size in bytes,source,usage)
    	GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, txFloatBuffer.capacity()*4 , txFloatBuffer, GLES20.GL_STATIC_DRAW);
    	//unbind VBO
    	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);Log.d("VBO", "WEIGHTS");
    	 //generate weightsVBO (1VBO,array to store handle, offset on array)
        GLES20.glGenBuffers(1, weightsVBO,0);
        //bind weightsVBO(array type,handle)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, weightsVBO[0]);
        //copy FloatBuffer to VBO(type,size in bytes,source,usage)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, weightsFloatBuffer.capacity()*4 , weightsFloatBuffer, GLES20.GL_STATIC_DRAW);
        //unbind VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);Log.d("VBO", "PROGRAM");
        //compile shaders and create second program for skeletal animation
        program2 = createProgram(vertexShaderSource2,fragmentShaderSource);
        //set navigation to 0
        navX=0;navY=0;navZ=0;Yaw=0;Pitch=0; 
    	VBOcreated=true;
    	} //initialize time
    
   	//returns time in a long
    time=System.currentTimeMillis();
	//float=long-long/float(to milliseconds)
	deltaTime=(time-lastTime)/1000.0f;
	//store last time for next frame
	lastTime=time;
  
	//if VBOs ready draw animation      
   if(VBOcreated){ 
	   //set program to use and get all locations
	     GLES20.glUseProgram(program2);
	     //return attribute location to handle(program,attribute name)
	     attributePositionLocation=GLES20.glGetAttribLocation(program2, "attributePosition");
	     //if not found handle returned with -1
	     if (attributePositionLocation == -1) {
	         throw new RuntimeException("Could not get attributePosition location");
	     }
	     //enable attrib on location (attributePositionLocation)
	     GLES20.glEnableVertexAttribArray(attributePositionLocation); 
	     //Log.d("attPosLoc", ""+attributePositionLocation);
	     //return attribute location to handle(program,attribute name)
	     attributeTextureCoordLocation=GLES20.glGetAttribLocation(program2, "attributeTextureCoord");
	     //if not found handle returned with -1
	     if (attributeTextureCoordLocation == -1) {
	         throw new RuntimeException("Could not get attributeTextureCoord Location");
	     }
	     //enable attrib on location (attributeTextureCoordLocation)
	     GLES20.glEnableVertexAttribArray(attributeTextureCoordLocation);     
	     //return attribute location to handle(program,attribute name)
	     attributeNormalLocation=GLES20.glGetAttribLocation(program2, "attributeNormal");
	     //if not found handle returned with -1
	     if (attributeNormalLocation == -1) {
	         throw new RuntimeException("Could not get attributeNormal Location");
	     }
	     //enable attrib on location (attributeNormalLocation)
	     GLES20.glEnableVertexAttribArray(attributeNormalLocation); 
	     //return attribute location to handle(program,attribute name)
	     attributebone12Location=GLES20.glGetAttribLocation(program2, "attributebone12");
	     //if not found handle returned with -1
	     if (attributebone12Location == -1) {
	         throw new RuntimeException("Could not get attributebone12 location");
	     }
	     //enable attrib on location (attributebone12Location)
	     GLES20.glEnableVertexAttribArray(attributebone12Location); 
	     //Log.d("attb12", ""+attributebone12Location);
	     //return attribute location to handle(program,attribute name)
	     attributebone34Location=GLES20.glGetAttribLocation(program2, "attributebone34");
	     //if not found handle returned with -1
	     if (attributebone34Location == -1) {
	         throw new RuntimeException("Could not get attributebone34 location");
	     }
	     //enable attrib on location (attributebone34Location)
	     GLES20.glEnableVertexAttribArray(attributebone34Location); 
	     //Log.d("attb34", ""+attributebone34Location);
	     //get handle to Sampler Uniform location
	     samplerHandle=GLES20.glGetUniformLocation(program2, "samplerTexture");
	     //if Sampler Uniform location not found handle is returned -1
	     if (samplerHandle == -1) {
	         throw new RuntimeException("Could not get samplerTexture location");
	    }
	     //get Uniform Handle(program,uniformName) 
	     projectionMatrixHandle = GLES20.glGetUniformLocation(program2, "projectionMatrix");
	     //if Uniform location not found handle is returned -1
	     if (projectionMatrixHandle == -1) {
	         throw new RuntimeException("Could not get projectionMatrix location");
	     }
	     //get Uniform Handle(program,uniformName) 
	     physicsMatrixHandle = GLES20.glGetUniformLocation(program2, "physicsMatrix");
	     //if Uniform location not found handle is returned -1
	     if (physicsMatrixHandle == -1) {
	         throw new RuntimeException("Could not get physicsMatrix location");
	     }
	     //get Uniform Handle(program,uniformName) 
	     navigationMatrixHandle = GLES20.glGetUniformLocation(program2, "navigationMatrix");
	     //if Uniform location not found handle is returned -1
	     if (navigationMatrixHandle == -1) {
	         throw new RuntimeException("Could not get navigationMatrix location");
	     }
	     //get Uniform Handle(program,uniformName) 
	    // bindShapeMatrixHandle = GLES20.glGetUniformLocation(program2, "bindShapeMatrix");
	     //if Uniform location not found handle is returned -1
	    // if (bindShapeMatrixHandle == -1) {
	     //    throw new RuntimeException("Could not get bindShapeMatrix location");
	    // }
	     //get Uniform Handle(program,uniformName) 
	     jointMatrixHandle = GLES20.glGetUniformLocation(program2, "jointMatrix");
	     //if Uniform location not found handle is returned -1
	     if (jointMatrixHandle == -1) {
	         throw new RuntimeException("Could not get jointMatrix location");
	     }
	     //get Uniform Handle(program,uniformName) 
	     invBindPoseMatrixHandle = GLES20.glGetUniformLocation(program2, "invBindPoseMatrix");
	     //if Uniform location not found handle is returned -1
	     if (invBindPoseMatrixHandle == -1) {
	         throw new RuntimeException("Could not get invBindPoseMatrix location");
	     }
	     //get uniform array size
	     //int[] l=new int[1];
	     //int[] uSize=new int[1];
	     //int[] type=new int[1];
	     //byte[] name=new byte[1];
	     //GLES20.glGetActiveUniform(program, jointMatrixHandle, 11, l, 0, uSize, 0, type, 0, name, 0);
	     //Log.e("USIZE", ""+uSize[0]);
	     //get max texture Units
	     //int[] maxt=new int[1];
	     //GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, maxt, 0);
	     //Log.d("max texture image units", ""+maxt[0]);
	     //get max texture Size
	     //int[] maxNum = new int[1];
	     //GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxNum, 0);
	     //Log.d("max texture size", ""+maxNum[0]);
	   
	     //bind .dae file loaded data and draw animation
	     //bind vertices VBO
	     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, verticesVBO[0]);
	     //set attribPointer(location,3membersXYZ,float,norm=false,stride=0,offsetbytes on VBO)
	     GLES20.glVertexAttribPointer(attributePositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
	     //bind normal VBO
	     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalVBO[0]);
	     //set attribPointer(location,3membersXYZ,float,norm=false,stride=0,offsetbytes on VBO)
	     GLES20.glVertexAttribPointer(attributeNormalLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
	     //bind txMapVBO
	     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, txVBO[0]);
	     //set attribPointer(location,2membersTxTy,float,norm=false,stride=0,offsetbytes on VBO)
	     GLES20.glVertexAttribPointer(attributeTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 0, 0);
	     //bind weights VBO
	     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, weightsVBO[0]);
	     //set attribPointer(location, members,float,norm=false,stride=0,offsetbytes on VBO)
	     GLES20.glVertexAttribPointer(attributebone12Location, 4, GLES20.GL_FLOAT, false, 32, 0);
	     //set attribPointer(location,members,float,norm=false,stride=0,offsetbytes on VBO)
	     GLES20.glVertexAttribPointer(attributebone34Location, 4, GLES20.GL_FLOAT, false, 32, 16);
	     //set active texture unit
	     GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	     //bind a (2Dtexture,Id) to the active texture unit 
	     GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
	     //set the texture unit to fetch from 0 to max Texture units
	     GLES20.glUniform1i(samplerHandle, 0); 
	     //set the Uniform data source(Handle,elements to read,false,source,offset)
	     GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);
	     Matrix.setIdentityM(navigationMatrix, 0);
	     Matrix.rotateM(navigationMatrix, 0, -Pitch, 1, 0, 0);
	     Matrix.rotateM(navigationMatrix, 0, -Yaw, 0, 1, 0);
	     Matrix.translateM(navigationMatrix, 0, -navX, -navY, -navZ);
	     GLES20.glUniformMatrix4fv(navigationMatrixHandle, 1, false, navigationMatrix, 0);
	     GLES20.glUniformMatrix4fv(jointMatrixHandle, 8, false, DAE.jointMatrix, 0);
	     GLES20.glUniformMatrix4fv(invBindPoseMatrixHandle, 8, false, DAE.invBindPoseMatrix, 0);
	    //Update physics matrix with objects position
	    Matrix.setIdentityM(physicsMatrix, 0);
	    Matrix.translateM(physicsMatrix, 0, 0, 0, -6);//fixed position
		//Matrix.translateM(physicsMatrix, 0, posX, posY, posZ);
	    //Matrix.rotateM(physicsMatrix, 0, angle, 0, 1, 0);

	    GLES20.glUniformMatrix4fv(physicsMatrixHandle, 1, false, physicsMatrix, 0);
	    //drawArrays(triangles,start on Vertex0,vertices to use(num of coordsXYZ/3))
	    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,verticesFloatBuffer.capacity()/3);
	    //animate object from frame 0 to frame 4 
	    DAE.animate(deltaTime,0,4);

   //physics update
    velX=velX+(accX*deltaTime);
   	velY=velY+(accY*deltaTime);
	velZ=velZ+(accZ*deltaTime);
   	posX=posX+(velX*deltaTime);
   	posY=posY+(velY*deltaTime);
   	posZ=posZ+(velZ*deltaTime);
   	auxTime+=deltaTime; if(auxTime>2f){
   		angle+=90; if(angle==360){angle=0;}
   		if (angle==0){velX=0; velZ=3;}
   		if (angle==90){velX=3; velZ=0;}
   		if (angle==180){velX=0; velZ=-3;}
   		if (angle==270){velX=-3; velZ=0;}
   		auxTime=0;
   		}
      }else{//if VBOs not ready draw Loading Screen
    	//set program and get all locations
    	    GLES20.glUseProgram(program);
    	    //return attribute location to handle(program,attribute name)
    	    attributePositionLocation=GLES20.glGetAttribLocation(program, "attributePosition");
    	    //if not found handle returned with -1
    	    if (attributePositionLocation == -1) {
    	        throw new RuntimeException("Could not get attributePosition location");    }
    	    //enable attrib on location (attributePositionLocation)
    	    GLES20.glEnableVertexAttribArray(attributePositionLocation); 
    	    //Log.d("attPosLoc", ""+attributePositionLocation);
    	    //return attribute location to handle(program,attribute name)
    	    attributeTextureCoordLocation=GLES20.glGetAttribLocation(program, "attributeTextureCoord");
    	    //if not found handle returned with -1
    	    if (attributeTextureCoordLocation == -1) {
    	        throw new RuntimeException("Could not get attributeTextureCoord Location");    }
    	    //enable attrib on location (attributeTextureCoordLocation)
    	    GLES20.glEnableVertexAttribArray(attributeTextureCoordLocation);     
    	    //return attribute location to handle(program,attribute name)
    	    attributeNormalLocation=GLES20.glGetAttribLocation(program, "attributeNormal");
    	    //if not found handle returned with -1
    	    if (attributeNormalLocation == -1) {
    	        throw new RuntimeException("Could not get attributeNormal Location");    }
    	    //enable attrib on location (attributeTextureCoordLocation)
    	    GLES20.glEnableVertexAttribArray(attributeNormalLocation);   
    	    //get handle to Sampler Uniform location
    	    samplerHandle=GLES20.glGetUniformLocation(program, "samplerTexture");
    	    //if Sampler Uniform location not found handle is returned -1
    	    if (samplerHandle == -1) {
    	        throw new RuntimeException("Could not get samplerTexture location");    }
    	    //get Uniform Handle(program,uniformName) 
    	    projectionMatrixHandle = GLES20.glGetUniformLocation(program, "projectionMatrix");
    	    //if Uniform location not found handle is returned -1
    	    if (projectionMatrixHandle == -1) {
    	        throw new RuntimeException("Could not get projectionMatrix location");    }
    	    //get Uniform Handle(program,uniformName) 
    	    physicsMatrixHandle = GLES20.glGetUniformLocation(program, "physicsMatrix");
    	    //if Uniform location not found handle is returned -1
    	    if (physicsMatrixHandle == -1) {
    	        throw new RuntimeException("Could not get physicsMatrix location");    }
    	    //get Uniform Handle(program,uniformName) 
    	    navigationMatrixHandle = GLES20.glGetUniformLocation(program, "navigationMatrix");
    	    //if Uniform location not found handle is returned -1
    	    if (navigationMatrixHandle == -1) {
    	        throw new RuntimeException("Could not get navigationMatrix location");    }
    //bind and draw loading sphere
    //bind vertices VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, loadingVVBO[0]);
    //set attribPointer(location,3membersXYZ,float,norm=false,stride=0,offsetbytes on VBO)
    GLES20.glVertexAttribPointer(attributePositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
    //bind normal VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, loadingNVBO[0]);
    //set attribPointer(location,3membersXYZ,float,norm=false,stride=0,offsetbytes on VBO)
    GLES20.glVertexAttribPointer(attributeNormalLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
    //bind txMapVBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, loadingTVBO[0]);
    //set attribPointer(location,2membersTxTy,float,norm=false,stride=0,offsetbytes on VBO)
    GLES20.glVertexAttribPointer(attributeTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 0, 0);
    //set active texture unit
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    //bind a (2Dtexture,Id) to the active texture unit 
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, loadingtextureId);
    //set the texture unit to fetch from 0 to max Texture units
    GLES20.glUniform1i(samplerHandle, 0); 
    //set the Uniform data source(Handle,elements to read,false,source,offset)
    GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);
    Matrix.setIdentityM(navigationMatrix, 0);
    GLES20.glUniformMatrix4fv(navigationMatrixHandle, 1, false, navigationMatrix, 0);
    //no navigation on loading screen
    navX=0;navY=0;navZ=0;Yaw=0;Pitch=0;  
    
    //Update physics matrix with objects position
    Matrix.setIdentityM(physicsMatrix, 0);
    Matrix.translateM(physicsMatrix, 0, 0, 0, -5);
    Matrix.rotateM(physicsMatrix, 0, loadingAngle, 0, 1, 0); 
    //set the Uniform data source(Handle,elements to read,false,source,offset)
    GLES20.glUniformMatrix4fv(physicsMatrixHandle, 1, false, physicsMatrix, 0);
    //drawArrays(triangles,start on Vertex0,vertices to use(num of coordsXYZ/3))
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,loadingV.capacity()/3);
    loadingAngle--; 
    }
   }

    
    float fSize=1.0f;
	float surfaceWidth,surfaceHeight, aspect;
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {//set viewport to whole surface
    surfaceWidth=width;
    surfaceHeight=height;
    GLES20.glViewport(0, 0, width, height);
    Matrix.setIdentityM(projectionMatrix, 0);
    //api level
    int api=android.os.Build.VERSION.SDK_INT;
    if(api>=14){
    Matrix.perspectiveM(projectionMatrix, 0, 67, width/(float)height, 1.5f, 20);
    }else{
    Matrix.frustumM(projectionMatrix, 0, -(width/(float)height)*fSize, (width/(float)height)*fSize, -fSize, fSize, 1.5f, 20);}
    }

    
	int controlIndex;
	int controlMoveId;
	int controlLookId;
	boolean Move=false;
	boolean Look=false;
	float initialTouchMoveX, initialTouchMoveY;
	float initialTouchLookX, initialTouchLookY;
	float directionX, directionY, directionZ;
	float normDirectionX,normDirectionY,normDirectionZ;
	final float[] matrix = new float[16];
	final float[] directionDefault= { 0, 0, -1, 1 };
	final float[] navDirection = new float[4];
	final float[] normalDefault={1,0,0,1};
	final float[] normalDir = new float [4];
	float navX,navY,navZ,Yaw,Pitch;
    
  //get direction for forward, backward movement
  	public void getDirection() {
  		Matrix.setIdentityM(matrix, 0);
  		//rotate Yaw degrees around Y axis
  		Matrix.rotateM(matrix, 0, Yaw, 0, 1, 0); 
  		//rotate Pitch degrees around X axis
  		Matrix.rotateM(matrix, 0, Pitch, 1, 0, 0);
  		Matrix.multiplyMV(navDirection, 0, matrix, 0, directionDefault, 0);
  		directionX=navDirection[0];
  		directionY=navDirection[1];
  		directionZ=navDirection[2];
  		}
  	
  		//get normal direction for strafe right,left
  		public void getNormalDirection() {
  		Matrix.setIdentityM(matrix, 0);
  		Matrix.rotateM(matrix, 0, Yaw, 0, 1, 0); //rotate degrees around Y axis
  		Matrix.rotateM(matrix, 0, Pitch, 1, 0, 0);//rotate degrees around X axis
  		Matrix.multiplyMV(normalDir, 0, matrix, 0, normalDefault, 0);
  		normDirectionX=normalDir[0];
  		normDirectionY=normalDir[1];
  		normDirectionZ=normalDir[2];
  		}
    
    
    float moveVsensitivity=0.03f;
    float moveHsensitivity=0.005f;
    float lookVsensitivity=0.05f;
    float lookHsensitivity=0.03f;
    	@Override
    	public boolean onTouch(View v, MotionEvent event) {
    		// TODO Auto-generated method stub
    		if((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN | (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN)
    		{
    			controlIndex=event.getActionIndex();
    			if(event.getX(controlIndex)<surfaceWidth/2){  
    			controlMoveId=event.getPointerId(controlIndex);
    			initialTouchMoveX=event.getX(controlIndex);
    			initialTouchMoveY=event.getY(controlIndex);
    			Move=true;
    				}
    			else{
    			controlLookId=event.getPointerId(controlIndex);
    			initialTouchLookX=event.getX(controlIndex);
    			initialTouchLookY=event.getY(controlIndex);
    			Look=true;
    				}
    			
    		}
    		
    		if((event.getAction() & MotionEvent.ACTION_MASK)==MotionEvent.ACTION_MOVE)
    		{
    			if(Move){
    				if(event.getX( event.findPointerIndex(controlMoveId) )   <   initialTouchMoveX){
    					
    					getNormalDirection();
    					navX-=(normDirectionX*deltaTime)*(initialTouchMoveX-event.getX(event.findPointerIndex(controlMoveId)))*moveHsensitivity;
    					//navY-=(normDirectionY*deltaTime)*(initialTouchMoveX-event.getX(event.findPointerIndex(controlMoveId)))*moveHsensitivity;
    					navZ-=(normDirectionZ*deltaTime)*(initialTouchMoveX-event.getX(event.findPointerIndex(controlMoveId)))*moveHsensitivity;
    					
    				}else{
    					
    					getNormalDirection();
    					navX+=(normDirectionX*deltaTime)*(event.getX(event.findPointerIndex(controlMoveId))-initialTouchMoveX)*moveHsensitivity; 
    					//navY+=(normDirectionY*deltaTime)*(event.getX(event.findPointerIndex(controlMoveId))-initialTouchMoveX)*moveHsensitivity;
    					navZ+=(normDirectionZ*deltaTime)*(event.getX(event.findPointerIndex(controlMoveId))-initialTouchMoveX)*moveHsensitivity;
    					
    				}
    				if(event.getY( event.findPointerIndex(controlMoveId) )   <   initialTouchMoveY){
    					getDirection();
    					navX+=(directionX*deltaTime)*(initialTouchMoveY-event.getY(event.findPointerIndex(controlMoveId)))*moveVsensitivity;
    					//navY+=(directionY*deltaTime)*(initialTouchMoveY-event.getY(event.findPointerIndex(controlMoveId)))*moveVsensitivity; 
    					navZ+=(directionZ*deltaTime)*(initialTouchMoveY-event.getY(event.findPointerIndex(controlMoveId)))*moveVsensitivity;
    				}else{
    					getDirection();
    					navX-=(directionX*deltaTime)*(event.getY(event.findPointerIndex(controlMoveId))-initialTouchMoveY)*moveVsensitivity;
    					//navY-=(directionY*deltaTime)*(event.getY(event.findPointerIndex(controlMoveId))-initialTouchMoveY)*moveVsensitivity; 
    					navZ-=(directionZ*deltaTime)*(event.getY(event.findPointerIndex(controlMoveId))-initialTouchMoveY)*moveVsensitivity;
    					}
    				}
    			
    			if(Look){
    				if(event.getX( event.findPointerIndex(controlLookId) )   <   initialTouchLookX){Yaw+=(0.5f*(initialTouchLookX-event.getX(event.findPointerIndex(controlLookId)))*lookHsensitivity);}else{Yaw-=(0.5f*(event.getX(event.findPointerIndex(controlLookId))-initialTouchLookX)*lookHsensitivity);}
    				if(event.getY( event.findPointerIndex(controlLookId) )   <   initialTouchLookY){Pitch+=(0.15f*(initialTouchLookY-event.getY(event.findPointerIndex(controlLookId)))*lookVsensitivity);if (Pitch > 90)Pitch = 90;}else{Pitch-=(0.15f*(event.getY(event.findPointerIndex(controlLookId))-initialTouchLookY)*lookVsensitivity);if (Pitch < -90)Pitch = -90;}
    							
    			}
    			
    		}
    		
    		if((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP | (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP)
    		{
    			if(Move){if(event.getPointerId(event.getActionIndex())==controlMoveId){Move=false;initialTouchMoveX=0;initialTouchMoveY=0;}}
    			if(Look){if(event.getPointerId(event.getActionIndex())==controlLookId){Look=false;initialTouchLookX=0;initialTouchLookY=0;}}
    			
    		}
    		//enable Log for touch debug
    		//Log.d("move", "x:"+initialTouchMoveX+" Y:"+initialTouchMoveY+" navX:"+navX+" navZ"+navZ);
    		//Log.d("look", "x:"+initialTouchLookX+" Y:"+initialTouchLookY+" Pitch:"+Pitch+" Yaw"+Yaw);
    		//Log.d("direction", "x:"+directionX+" y:"+directionY+" z:"+directionZ);
    		//Log.d("normdirection", "x:"+normDirectionX+" y:"+normDirectionY+" z:"+normDirectionZ);
    		return true;
    	}
    	

        
    	public class loaderThread extends Thread{
    		@Override public void run(){
    		Log.e("Thread", "runing");  
    		//load .dae file
    		DAE.load("AndroidSkeletonX90.dae");
    	    //allocate num of vertices*3 members XYZ*4bytes each float
    	    verticesFloatBuffer = ByteBuffer.allocateDirect(DAE.vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    	    verticesFloatBuffer.put(DAE.verticesBindShape).position(0);
    	    //allocate number of txMap coords*4bytes
    	    txFloatBuffer = ByteBuffer.allocateDirect(DAE.tx.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    	    txFloatBuffer.put(DAE.tx).position(0);
    	    //allocate num of normals*3 members XYZ*4bytes each float
    	    normalFloatBuffer = ByteBuffer.allocateDirect(DAE.normals.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    	    normalFloatBuffer.put(DAE.normals).position(0);  
    	    //allocate per vertex weights * 4bytes each float
    	    weightsFloatBuffer = ByteBuffer.allocateDirect(DAE.perVertexWeights.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    	    weightsFloatBuffer.put(DAE.perVertexWeights).position(0);  
    	    //output to logcat length of each buffer
    	    Log.d("VERTEX",""+verticesFloatBuffer.capacity()/3);
    	    Log.d("TX",""+txFloatBuffer.capacity()/2);
    	    Log.d("NORMAL",""+normalFloatBuffer.capacity()/3);
    	    Log.d("WEIGHTS",""+weightsFloatBuffer.capacity()/8);
    		//tell the app loaded has finished
    		Log.e("Thread", "finished");  	    
    	    loaded=true;
    		}
    	}
}
}

