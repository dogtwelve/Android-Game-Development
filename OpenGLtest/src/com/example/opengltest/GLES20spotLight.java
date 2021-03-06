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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

public class GLES20spotLight extends Activity {
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
		Log.d("openGL ES 2.0 Support: ",""+ detectOpenGLES20());
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
    FloatBuffer objVerticesFloatBuffer;
    int objVerticesVBO[]=new int[1];
    int attributePositionLocation;
    FloatBuffer objTxFloatBuffer;
    int objTxVBO[]=new int[1];
    int attributeTextureCoordLocation;
    FloatBuffer objNormalFloatBuffer;
    int objNormalVBO[]=new int[1];
    int attributeNormalLocation;
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
    float angle;
    ObjLoader OBJ; 
    String vertexSharderSource = 
            "attribute vec4 attributePosition;     \n"
          + "attribute vec3 attributeNormal;       \n"
          + "attribute vec2 attributeTextureCoord; \n"
          + "varying vec2 varyingTextureCoord;     \n"
          + "varying vec4 varyingLight;            \n"
          + "uniform mat4 projectionMatrix;        \n" 
          + "uniform mat4 physicsMatrix;           \n"
          + "vec3 lightPos=vec3(0.0,0.5,-5.0);      \n"
          + "vec3 eyePos=vec3(0.0,0.0,0.0);        \n"
          + "const float zero=0.0;                 \n"
          + "const float one=1.0;                  \n"
          + "float emissive=0.0;                   \n"
          + "float ambient=0.2;                    \n"
          + "float diffuse=0.80;                   \n" 
          + "float specular=5.0;                   \n"
          + "float specularPower=400.0;                \n"
          + "vec4 ambientColor=vec4(1.0,1.0,1.0,1.0); \n"
          + "float constantAtt=0.1;                   \n"
          + "float linearAtt=0.05;                    \n"
          + "float quadraticAtt=0.025;                 \n"
          + "void main()                              \n"
          + "{  gl_Position = projectionMatrix*physicsMatrix*attributePosition;  \n" 
          + "   lightPos=(projectionMatrix*vec4(lightPos,one)).xyz;  \n" 
          + " 	float distance=length(lightPos-gl_Position.xyz);                 \n"
          + " 	float attFactor=one/(constantAtt+linearAtt*distance+quadraticAtt*distance*distance); \n"
          + " 	vec3 lightDir=normalize(lightPos-gl_Position.xyz);               \n"
          + "   vec4 tempNormal=vec4(attributeNormal.xyz,one);                   \n"
          + "   mat4 physicsRotationM=mat4(physicsMatrix[0], physicsMatrix[1], physicsMatrix[2], physicsMatrix[3]);                       \n"
          + "   physicsRotationM[3]=vec4(zero,zero,zero,one);                    \n"
          + "   tempNormal=projectionMatrix*physicsRotationM*tempNormal;         \n"
          + "   vec3 normalizedTempNormal=normalize(tempNormal.xyz);             \n"
          + "   float lightDiffuse=max(dot(normalizedTempNormal,lightDir),zero); \n"
          + "   vec3 eyeDir=normalize(eyePos-gl_Position.xyz); \n"
          + "   vec3 halfPlane=normalize(lightDir+eyeDir);            \n"
          + "   float lightSpecular=pow(max(dot(normalizedTempNormal,halfPlane),zero),specularPower);  \n"
          + "   varyingLight=emissive+(ambient*ambientColor)+(diffuse*lightDiffuse*ambientColor)+(specular*lightSpecular*ambientColor); \n"
          + "   varyingLight*=attFactor;                                         \n"
          + "   varyingTextureCoord=attributeTextureCoord;                       \n"
          + "}                                                                   \n";
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
   
       
    public SurfaceRenderer(Context context)
    {
 
    OBJ=new ObjLoader(context);
    OBJ.load("ball.obj");

    //create OBJ Buffers
    //allocate num of vertices*3 members XYZ*4bytes each float
    objVerticesFloatBuffer = ByteBuffer.allocateDirect(OBJ.vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    objVerticesFloatBuffer.put(OBJ.vertices).position(0);

    //allocate number of txMap coords*4bytes
    objTxFloatBuffer = ByteBuffer.allocateDirect(OBJ.tx.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    objTxFloatBuffer.put(OBJ.tx).position(0);
    //allocate num of normals*3 members XYZ*4bytes each float
    objNormalFloatBuffer = ByteBuffer.allocateDirect(OBJ.normal.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    objNormalFloatBuffer.put(OBJ.normal).position(0);  
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
    GLES20.glEnable(GLES20.GL_BLEND);  
	//set blending source and function
	GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	//enable depth test
	GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    //open asset to bitmap
    try {
		 AssetManager assetManager = getAssets();
	 	 InputStream inputStream = assetManager.open("boxTexture.png");
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
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    
    //generate verticesVBO(1VBO,array to store handle, offset on array)
    GLES20.glGenBuffers(1, objVerticesVBO,0);
    //bind vertices VBO(array type,handle)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, objVerticesVBO[0]);
    //copy FloatBuffer to VBO(type,size in bytes,source,usage)
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, objVerticesFloatBuffer.capacity()*4 , objVerticesFloatBuffer, GLES20.GL_STATIC_DRAW);
    //unbind VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    //generate normalVBO(1VBO,array to store handle, offset on array)
    GLES20.glGenBuffers(1, objNormalVBO,0);
    //bind normal VBO(array type,handle)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, objNormalVBO[0]);
    //copy FloatBuffer to VBO(type,size in bytes,source,usage)
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, objNormalFloatBuffer.capacity()*4 , objNormalFloatBuffer, GLES20.GL_STATIC_DRAW);
    //unbind VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    //generate txMapVBO (1VBO,array to store handle, offset on array)
    GLES20.glGenBuffers(1, objTxVBO,0);
    //bind txMap VBO(array type,handle)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, objTxVBO[0]);
    //copy FloatBuffer to VBO(type,size in bytes,source,usage)
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, objTxFloatBuffer.capacity()*4 , objTxFloatBuffer, GLES20.GL_STATIC_DRAW);
    //unbind VBO
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
     
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
     // Clear the color and depth
	GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
     
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
     //return attribute location to handle(program,attribute name)
     attributeNormalLocation=GLES20.glGetAttribLocation(program, "attributeNormal");
     //if not found handle returned with -1
     if (attributeNormalLocation == -1) {
         throw new RuntimeException("Could not get attributeNormal Location");
     }
     //enable attrib on location (attributeTextureCoordLocation)
     GLES20.glEnableVertexAttribArray(attributeNormalLocation);   
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
     
    
     
     //bind vertices VBO
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, objVerticesVBO[0]);
     //set attribPointer(location,3membersXYZ,float,norm=false,stride=0,offsetbytes on VBO)
     GLES20.glVertexAttribPointer(attributePositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
     //bind normal VBO
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, objNormalVBO[0]);
     //set attribPointer(location,3membersXYZ,float,norm=false,stride=0,offsetbytes on VBO)
     GLES20.glVertexAttribPointer(attributeNormalLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
     //bind txMapVBO
     GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, objTxVBO[0]);
     //set attribPointer(location,2membersTxTy,float,norm=false,stride=0,offsetbytes on VBO)
     GLES20.glVertexAttribPointer(attributeTextureCoordLocation, 2, GLES20.GL_FLOAT, false, 0, 0);
     //set active texture unit
     GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
     //bind a (2Dtexture,Id) to the active texture unit 
     GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
     //set the texture unit to fetch from 0 to max Texture units
     GLES20.glUniform1i(samplerHandle, 0); 
     //set the Uniform data source(Handle,elements to read,false,source,offset)
     GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);
     //draw objects 5 rows spaced 3 units
     for(int j=-15; j<=0;j+=3){//7 objects per row
     for(int i=-3; i<=3;i++){
         //Update physics matrix with objects position
         Matrix.setIdentityM(physicsMatrix, 0);
         Matrix.translateM(physicsMatrix, 0, i*3, -2, j);
         Matrix.rotateM(physicsMatrix, 0, angle, 1, 0, 0);
         //set the Uniform data source(Handle,elements to read,false,source,offset)
         GLES20.glUniformMatrix4fv(physicsMatrixHandle, 1, false, physicsMatrix, 0);
         //drawArrays(triangles,start on Vertex0,vertices to use(num of coordsXYZ/3))
         GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,objVerticesFloatBuffer.capacity()/3);
    }}
     angle++;
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
    Matrix.perspectiveM(projectionMatrix, 0, 67, width/(float)height, 1.5f, 15);
    }else{
    Matrix.frustumM(projectionMatrix, 0, -(width/(float)height)*fSize, (width/(float)height)*fSize, -fSize, fSize, 1.5f, 15);}
    }

    float touchX, touchY;
    boolean touch;
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		return true;
	}
}
}

