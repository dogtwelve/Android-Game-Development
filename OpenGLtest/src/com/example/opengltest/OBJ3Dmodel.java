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
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

public class OBJ3Dmodel extends Activity{
	GLSurfaceView glSurfaceView;
	myRenderer Render;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//no title bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//fullscreen
		glSurfaceView = new GLSurfaceView(this);//initialize GLSurfaceView
		Render=new myRenderer(this);//initializa renderer
		glSurfaceView.setRenderer(Render);//set renderer
		glSurfaceView.setOnTouchListener(Render);
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

class myRenderer implements Renderer,OnTouchListener {
ByteBuffer byteBuffer;
FloatBuffer vertices;
FloatBuffer normals;
FloatBuffer colors;
FloatBuffer texturemap;
Bitmap bitmap;
int textureId;
long time;
long lastTime;
float deltaTime;
ObjLoader OBJ;

public myRenderer(Context context){
	//initialize objloader
	OBJ=new ObjLoader(context);
	//load ".OBJ" file on assets folder(fileName)
	OBJ.load("ball.obj");
	//Vertices
	byteBuffer = ByteBuffer.allocateDirect(OBJ.vertices.length*4);
	//OBJ vertices count*3 members(x,y,z)*4bytes each float
	byteBuffer.order(ByteOrder.nativeOrder());//set byteorder
	vertices = byteBuffer.asFloatBuffer();//vertices as floatbuffer
	vertices.put(OBJ.vertices);//copy float array to float buffer
	vertices.flip();//set limits from 0 to number of floats
	//Normals
	byteBuffer = ByteBuffer.allocateDirect(OBJ.normal.length*4);
	//OBJ normal count*3 members(x,y,z)*4bytes each float
	byteBuffer.order(ByteOrder.nativeOrder());//set byteorder
	normals = byteBuffer.asFloatBuffer();//normals as floatbuffer
	normals.put(OBJ.normal);//copy float array to float buffer
	normals.flip();//set limits from 0 to number of floats
	//TextreMap
	byteBuffer = ByteBuffer.allocateDirect(OBJ.tx.length*4);
	//OBJ vertices count*2 membersTexX,TexY*4bytes each float
	byteBuffer.order(ByteOrder.nativeOrder());//set byteorder
	texturemap = byteBuffer.asFloatBuffer();//texturemap as floatbuffer
	texturemap.put(OBJ.tx);//copy float array to float buffer
	texturemap.flip();//set limits from 0 to number of floats
	//texture bitmap		
	try {
		 AssetManager assetManager = getAssets();
	 	 InputStream inputStream = assetManager.open("balltx128.jpg");
	     bitmap = BitmapFactory.decodeStream(inputStream);
	     inputStream.close();
	     } catch (IOException e) {}
		}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	//set black as clear color
	gl.glClearColor(0,0,0,1); 
	//enable vertex array for vertex pointer
	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
	//enable normal array
	gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
	//enable DepthTest
	gl.glEnable(GL10.GL_DEPTH_TEST); 
	//enable Blend
	gl.glEnable(GL10.GL_BLEND);
	//set blend function
	gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
	//Gen texture
	//initialize array
	int textureIds[] = new int[1];
	//generate 1 texture return handle to textureIds
	gl.glGenTextures(1, textureIds, 0);
	//get handle from array to an int
	textureId = textureIds[0];
	//bind using array
	gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
	//copy image pixels data to texture buffer on video mem
	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	//filter for minif
	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
	//filter for magnif
	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
	//unbind texture buffer
	gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
	//bitmap.recycle(); just if not used anymore 
	}
	
float surfaceWidth, surfaceHeight;	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	//set viewport to the whole GLSurfaceView size
	gl.glViewport(0, 0, width, height);
	surfaceWidth=width;
	surfaceHeight=height;
	//set projection matrix as active matrix
	gl.glMatrixMode(GL10.GL_PROJECTION);  
	//load identity matrix to multiply with next matrix operation
	gl.glLoadIdentity();     
	//set perspective projection(GL10,FovY,Aspect,zNear,zFar)
	GLU.gluPerspective(gl, 70,width/(float)height,0.1f, 10f);
	//set ModelView matrix as active
	gl.glMatrixMode(GL10.GL_MODELVIEW);
	//initialize time
	lastTime=System.currentTimeMillis();
	}

float angle;
float navX,navY,navZ,Yaw,Pitch;
	@Override
	public void onDrawFrame(GL10 gl) {
	//get time as double
	time=System.currentTimeMillis();
	//get delta time as float
	deltaTime=(time-lastTime)/1000.0f;//float=double-double/float
	//remember last time for next frame
	lastTime=time;
	
	//clear framebuffer and depth buffer
	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); 
	//set vertices shared by all cubes
	//set vertexpointer(3memberXYZ,FloatData,3Float*4Byte,dataBuffer)
	gl.glVertexPointer(3, GL10.GL_FLOAT, 12, vertices);
	//set normals shared by all cubes
	//normalpointer(dataTYPE,3float*4bytes,dataBuffer)
	gl.glNormalPointer(GL10.GL_FLOAT, 12, normals);
		
	//set navigation
	gl.glLoadIdentity();
	//look up-down
	gl.glRotatef(-Pitch, 1, 0, 0); 
	//turn right-left
	gl.glRotatef(-Yaw, 0, 1, 0);  
	//move world in xyz axis
	gl.glTranslatef(-navX,-navY,-navZ); 
	
	//set light parameters
	//RGBA ambient light colors
	float[] ambientColor = { 0.1f, 0.11f, 0.1f, 1f }; 
	//glLightModelfv(ambientLight, array with color, offset to color)
	gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, ambientColor, 0);
	//position from where the light comes to the origin
	float[] pos = {3, 0, 2, 0};
	//set source1 position 
	//glLightfv(source id, parameter, data array, offset);
	gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, pos, 0);

	//enable lighting
	gl.glEnable(GL10.GL_LIGHTING);
	//enable source 1
	gl.glEnable(GL10.GL_LIGHT0);
	//light shade model interpolate
	gl.glShadeModel(GL10.GL_SMOOTH);
	//enable material
	gl.glEnable(GL10.GL_COLOR_MATERIAL);
		
//save nav and light matrix		
gl.glPushMatrix();
		
	gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	//set default render color 
	gl.glColor4f(0, 1, 0.5f, 1);
		
	//transform 1st obj
	gl.glTranslatef(0, 0, -8);
	gl.glScalef(0.3f, 0.3f, 0.3f);	
	gl.glRotatef(angle, 1, 1, 0);
				
	//draw first obj
	gl.glDrawArrays(GL10.GL_TRIANGLES, 0, OBJ.vertices.length/3);
	//GL10.glDrawElements(DrawType,36 indices,shorData,dataBuffer)
		
//discard nav,light,transfOBJ1 matrix
//and return to nav,light saved matrix
gl.glPopMatrix();
//save nav and light matrix
gl.glPushMatrix();
		
	//set transformation for second obj
	//gl.glLoadIdentity();
	gl.glColor4f(0, 0.5f, 1f, 0.5f);
	gl.glTranslatef(0, 0, -8);
	gl.glRotatef(angle, 0, 1, 0);
	//Draw second obj
	gl.glDrawArrays(GL10.GL_TRIANGLES, 0, OBJ.vertices.length/3);
		
//discard nav,light,transfOBJ2 matrix
//and return to nav,light saved matrix
gl.glPopMatrix();
//save nav and light matrix
gl.glPushMatrix();	

	//third obj will use texture, disable color array
	gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	//set default color to solid white
	gl.glColor4f(1, 1, 1, 1);
	//enable texture coordinates array
	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	//enable 2D texture for bind
	gl.glEnable(GL10.GL_TEXTURE_2D);
	//set texturecoord pointer
	gl.glTexCoordPointer(2, GL10.GL_FLOAT, 8, texturemap); 
	//bind texture
	gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		
	//translate 3rd obj
	//gl.glLoadIdentity();
	gl.glTranslatef(-5, -1, -7);
	gl.glRotatef(-angle, 0, 1, 0);
	//draw 3rd obj
	gl.glDrawArrays(GL10.GL_TRIANGLES, 0, OBJ.vertices.length/3);
	//unbind texture so other cubes dont use texture
	gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
	//disable texture state if not next frame first obj 
	//will try to use the texture
	gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	gl.glDisable(GL10.GL_TEXTURE_2D);
	angle++;
		
//discard nav,light,transfCube3 matrix
//and return to nav,light saved matrix
gl.glPopMatrix();
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
	//enable Log for debug
	//Log.d("move", "x:"+initialTouchMoveX+" Y:"+initialTouchMoveY+" navX:"+navX+" navZ"+navZ);
	//Log.d("look", "x:"+initialTouchLookX+" Y:"+initialTouchLookY+" Pitch:"+Pitch+" Yaw"+Yaw);
	//Log.d("direction", "x:"+directionX+" y:"+directionY+" z:"+directionZ);
	//Log.d("normdirection", "x:"+normDirectionX+" y:"+normDirectionY+" z:"+normDirectionZ);
	return true;
	}
}
}
