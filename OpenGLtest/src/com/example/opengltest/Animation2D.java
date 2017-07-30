package com.example.opengltest;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Animation2D extends Activity{
	GLSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//no title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//fullscreen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//initialize GLSurfaceView
		glSurfaceView = new GLSurfaceView(this);
		//set renderer
		glSurfaceView.setRenderer(new myRenderer());
		//set surface as content
		setContentView(glSurfaceView);
	}


@Override
public void onResume() {
super.onResume();
//resume drawing if activity onResume
glSurfaceView.onResume();
}

@Override
public void onPause() {
super.onPause();
//stop drawing if activity onPause
glSurfaceView.onPause();
}



class myRenderer implements Renderer {
float Rectangle[]={-100.0f,-100.0f,100.0f,-100.0f,100.0f,100.0f,-100.0f,100.0f};
short RectangleIndex[]={0,1,2,2,3,0};
float RectangleTexturemap[]={0.25f,0.25f,0f,0.25f,0f,0f,0.25f,0f};
float Rectangle2[]={-100.0f,-100.0f,100.0f,-100.0f,100.0f,100.0f,-100.0f,100.0f};
short Rectangle2Index[]={0,1,2,2,3,0};
float Rectangle2Texturemap[]={0.0f,1.0f,0.25f,1.0f,0.25f,0.0f,0.0f,0.0f};
ByteBuffer byteBuffer;
FloatBuffer vertices;
ShortBuffer index;
FloatBuffer texturemap;
FloatBuffer vertices2;
ShortBuffer index2;
FloatBuffer texturemap2;
Bitmap bitmap=null;
int textureId;
IntBuffer textureID;
Bitmap bitmap2=null;
int textureId2;
IntBuffer textureID2;
long time;
long lastTime;
float deltaTime;
float posx, posy, velx, vely, accx, accy;
float pos2x, pos2y, vel2x, vel2y, acc2x, acc2y;
float centerx,centery,center2x,center2y,radius1,radius2;
float distancex,distancey;
boolean isOverlapping;
int frame;
float duration=0.25f;
float auxTime;
float[] Walk={
0f,0.25f,			0.25f,0.25f,		0.25f,0f,		0f,0f,
0.25f,0.25f,		0.5f,0.25f,			0.5f,0f,		0.25f,0f,
0.5f,0.25f,			0.75f,0.25f,		0.75f,0f,		0.5f,0f,
0.75f,0.25f,		1f,0.25f,			1f,0f,			0.75f,0f
		};


float[] Fight={
0f,0.5f,		0.25f,0.5f,		0.25f,0.25f,		0f,0.25f,
0.25f,0.5f,		0.5f,0.5f,		0.5f,0.25f,			0.25f,0.25f,
0.5f,0.5f,		0.75f,0.5f,		0.75f,0.25f,		0.5f,0.25f
		};

float[] Stone={
0f,0.75f,		0.25f,0.75f,		0.25f,0.5f,		0f,0.5f,
0.25f,0.75f,	0.5f,0.75f,			0.5f,0.5f,		0.25f,0.5f,
0.5f,0.75f,		0.75f,0.75f,		0.75f,0.5f,		0.5f,0.5f
		};

float[] WalkStone={
0f,1f,		0.25f,1f,	0.25f,0.75f,		0f,0.75f,
0.25f,1f,	0.5f,1f,	0.5f,0.75f,			0.25f,0.75f,
0.5f,1f,	0.75f,1f,	0.75f,0.75f,		0.5f,0.75f,
0.75f,1f,	1f,1f,		1f,0.75f,			0.75f,0.75f
		};


	public myRenderer(){
		
		//first object buffers
		//allocate 4 vertices*2 coordinates eachV(x,y)*4bytes each float
		byteBuffer = ByteBuffer.allocateDirect(4*2*4);
		//set byteorder
		byteBuffer.order(ByteOrder.nativeOrder());
		//vertices as floatbuffer
		vertices = byteBuffer.asFloatBuffer();
		//copy float array to float buffer
		vertices.put(Rectangle);
		//set limits from 0 to number of floats
		vertices.flip();
		//allocate 6 short indices*2bytes each short
		byteBuffer = ByteBuffer.allocateDirect(6*2);
		//byte order
		byteBuffer.order(ByteOrder.nativeOrder());
		//index as shortbuffer
		index = byteBuffer.asShortBuffer();
		//copy short array to buffer
		index.put(RectangleIndex);
		//set limits 0 to number of shorts
		index.flip();
		//allocate 4 vertices*2 coordinates (TEXx,TEXy)*4bytes each float
		byteBuffer = ByteBuffer.allocateDirect(4*2*4);
		//set byteorder
		byteBuffer.order(ByteOrder.nativeOrder());
		//texturemap as floatbuffer
		texturemap = byteBuffer.asFloatBuffer();
		//copy float array to float buffer
		texturemap.put(RectangleTexturemap);
		//set limits from 0 to number of floats
		texturemap.flip();
		//open external image on assets
		try {
				AssetManager assetManager = getAssets();
				InputStream inputStream = assetManager.open("cavemanAtlas512x512.png");
				bitmap = BitmapFactory.decodeStream(inputStream);
				inputStream.close();
				} catch (IOException e) {}
				
		//second object buffers
		//allocate 4 vertices*2 coordinates eachV(x,y)*4bytes each float
		byteBuffer = ByteBuffer.allocateDirect(4*2*4);
		//set byteorder
		byteBuffer.order(ByteOrder.nativeOrder());
		//vertices as floatbuffer
		vertices2 = byteBuffer.asFloatBuffer();
		//copy float array to float buffer
		vertices2.put(Rectangle2);
		//set limits from 0 to number of floats
		vertices2.flip();
		//allocate 6 short indices*2bytes each short
		byteBuffer = ByteBuffer.allocateDirect(6*2);
		//byte order
		byteBuffer.order(ByteOrder.nativeOrder());
		//index as shortbuffer
		index2 = byteBuffer.asShortBuffer();
		//copy short array to buffer
		index2.put(Rectangle2Index);
		//set limits 0 to number of shorts
		index2.flip();
		//allocate 4 vertices*2 coordinates (TEXx,TEXy)*4bytes each float
		byteBuffer = ByteBuffer.allocateDirect(4*2*4);
		//set byteorder
		byteBuffer.order(ByteOrder.nativeOrder());
		//texturemap as floatbuffer
		texturemap2 = byteBuffer.asFloatBuffer();
		//copy float array to float buffer
		texturemap2.put(Rectangle2Texturemap);
		//set limits from 0 to number of floats
		texturemap2.flip();
		//open external image on assets
		try {
			 AssetManager assetManager = getAssets();
		 	 InputStream inputStream = assetManager.open("turtle.png");
		     bitmap2 = BitmapFactory.decodeStream(inputStream);
		     inputStream.close();
		     } catch (IOException e) {}
		
 	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		//set projection matrix as active matrix
		gl.glMatrixMode(GL10.GL_PROJECTION);
		//load identity matrix 
		gl.glLoadIdentity();
		//set parallel projection 800x480 with Zaxis 3 pixel width(1to-1)
		gl.glOrthof(0, 800, 0, 480, 1, -1);
		//set ModelView as active matrix
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		//set black as clear color
		gl.glClearColor(0,0,0,1); 
		
		//gen texture 1
		//glGentexture using int array
		//initialize array
		int textureIds[] = new int[1];
		//generate 1(n) texture in videomem return handle to textureIds[0]
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
		
		//gen texture2
		//glGentexture using int array
		//initialize array
		int textureIds2[] = new int[1];
		//generate 1(n) texture in videomem return handle to textureIds[0]
		gl.glGenTextures(1, textureIds2, 0);
		//get handle from array to an int
		textureId2 = textureIds2[0];
		//bind using array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId2);
		//copy image pixels data to texture buffer on video mem
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap2, 0);
		//filter for minif
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		//filter for magnif
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		//unbind texture buffer
		gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		//bitmap.recycle(); just if not used anymore 
		
		//enable vertex array
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		//enable texture coord array
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
		//enable texture state
		gl.glEnable(GL10.GL_TEXTURE_2D);
		//enable blending
		gl.glEnable(GL10.GL_BLEND);  
		//set blending source and function
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		//set initial position, velocity, acceleration obj1
		posx=200;
		posy=100;
		velx=150;
		//vely=600;
		accx=0;
		//accy=-980f;
		//set circle bound center if offset needed
		centerx=posx;
		centery=posy;
		radius1=60;
		//set initial position, velocity, acceleration obj2
		pos2x=400;
		pos2y=100;
		vel2x=-250;
		vel2y=600;
		acc2x=0;
		acc2y=-980f;
		//set circle bound center if offset needed
		center2x=pos2x;
		center2y=pos2y;
		radius2=60;
		//initialize last time
		lastTime=System.currentTimeMillis();
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	//set viewport to the whole GLSurfaceView size
	gl.glViewport(0, 0, width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		//returns current time in a long
		time=System.currentTimeMillis();
		//float=long-long/float(to milliseconds)
		deltaTime=(time-lastTime)/1000.0f;
		//store last time for next frame
		lastTime=time;
		//clear framebuffer with clearcolor
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		//if collision detected change clear color to blue
		if(isOverlapping){gl.glClearColor(0,0,0.5f,1);}
		//if no collision return to black
		else{gl.glClearColor(0,0,0,1);}
		
		//translate object1
		//load a clean matrix
		gl.glLoadIdentity();
		//translate to posx,posy
		gl.glTranslatef(posx,posy,0);
		
		//draw object1
		//bind texture buffer
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		//glVertexPointer(num of members,data_type,stride,databuffer)
		gl.glVertexPointer(2,GL10.GL_FLOAT,8,vertices);
		
		//if is overlapping use fight animation
		if(isOverlapping){
			//if check for frame duration
			if(auxTime>duration){
				//if last frame, set to first frame again
				if(frame>=3){frame=0;}
				//check for direction
				if(velx==150){
				//if direction change swap v1 to v2, v3 to v4
				//for vertical flip of texture
				texturemap.put(2, Fight[frame*8+0]);
				texturemap.put(3, Fight[frame*8+1]);
				texturemap.put(0, Fight[frame*8+2]);
				texturemap.put(1, Fight[frame*8+3]);
				texturemap.put(6, Fight[frame*8+4]);
				texturemap.put(7, Fight[frame*8+5]);
				texturemap.put(4, Fight[frame*8+6]);
				texturemap.put(5, Fight[frame*8+7]);
				}
				else
				{//texture with no vertical flip
					texturemap.put(0, Fight[frame*8+0]);
					texturemap.put(1, Fight[frame*8+1]);
					texturemap.put(2, Fight[frame*8+2]);
					texturemap.put(3, Fight[frame*8+3]);
					texturemap.put(4, Fight[frame*8+4]);
					texturemap.put(5, Fight[frame*8+5]);
					texturemap.put(6, Fight[frame*8+6]);
					texturemap.put(7, Fight[frame*8+7]);	
				}
				//reset timer, increment  frame
			auxTime=0f;frame++;}
		}else{//if not overlapping use walk anim
			if(auxTime>duration){
				if(frame>=4){frame=0;}
				if(velx==150){
					texturemap.put(2, Walk[frame*8+0]);
					texturemap.put(3, Walk[frame*8+1]);
					texturemap.put(0, Walk[frame*8+2]);
					texturemap.put(1, Walk[frame*8+3]);
					texturemap.put(6, Walk[frame*8+4]);
					texturemap.put(7, Walk[frame*8+5]);
					texturemap.put(4, Walk[frame*8+6]);
					texturemap.put(5, Walk[frame*8+7]);
					}
					else
					{
						texturemap.put(0, Walk[frame*8+0]);
						texturemap.put(1, Walk[frame*8+1]);
						texturemap.put(2, Walk[frame*8+2]);
						texturemap.put(3, Walk[frame*8+3]);
						texturemap.put(4, Walk[frame*8+4]);
						texturemap.put(5, Walk[frame*8+5]);
						texturemap.put(6, Walk[frame*8+6]);
						texturemap.put(7, Walk[frame*8+7]);	
					}
			auxTime=0f;frame++;}	
		}
		
		//pointer textmap(num_of_coord2 xy,dataFLOAT,stride,array)
		gl.glTexCoordPointer(2, GL10.GL_FLOAT,8, texturemap);
		//indexed draw
		gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, index);
		//glDrawElements(DrawType,num of indices,DataType,indexBuffer)
		
		//physics object1
		//check if posx is going out ofscreen
		//if (posx<0 | posx>800){velx=-velx;}
		if(posx<0){velx=150;}if(posx>800){velx=-150;}
		//check if posy is on the fixed ground 100
		//if (posy<=100){vely=400;}
		//physics update
		velx=velx+(accx*deltaTime);
		vely=vely+(accy*deltaTime);
		posx=posx+(velx*deltaTime);
		posy=posy+(vely*deltaTime);
		//set circle1 bound center to posx,posy
		//or set offset if is not rectangle centered
		centerx=posx;
		centery=posy;
				
		//translate object2
		//load a clean matrix
		gl.glLoadIdentity();
		//translate to pos2x,pos2y
		gl.glTranslatef(pos2x,pos2y,0);
		
		//draw object2
		//bind texture buffer
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId2);
		//glVertexPointer(num of members,data_type,stride,databuffer)
		gl.glVertexPointer(2,GL10.GL_FLOAT,8,vertices2);
		//pointer textmap(num_of_coord2 xy,dataFLOAT,stride,array)
		gl.glTexCoordPointer(2, GL10.GL_FLOAT,8, texturemap2);
		//indexed draw
		gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT, index2);
		//glDrawElements(DrawType,num of indices,DataType,indexBuffer)
		
		//physics object2
		//check if posx is going out ofscreen
		//if (posx<0 | posx>800){velx=-velx;}
		if(pos2x<0){vel2x=250;}if(pos2x>800){vel2x=-250;}
		//check if posy is on the fixed ground 100
		if (pos2y<=100){vel2y=400;}
		//physics update
		vel2x=vel2x+(acc2x*deltaTime);
		vel2y=vel2y+(acc2y*deltaTime);
		pos2x=pos2x+(vel2x*deltaTime);
		pos2y=pos2y+(vel2y*deltaTime);
		//set circle2 bound center to pos2x,pos2y
		//or set offset if is not rectangle centered
		center2x=pos2x;
		center2y=pos2y;
		
		//collision test
		//get distance
		distancex=centerx-center2x;
		distancey=centery-center2y;
		//compare distance to radius sum
		isOverlapping=(distancex*distancex)+(distancey*distancey)<=(radius1+radius2)*(radius1+radius2);
		auxTime=auxTime+deltaTime;
	}
}

}