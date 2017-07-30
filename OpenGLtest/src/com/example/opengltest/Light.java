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
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Light extends Activity{
	GLSurfaceView glSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//no title bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//fullscreen
		glSurfaceView = new GLSurfaceView(this);//initialize GLSurfaceView
		glSurfaceView.setRenderer(new myRenderer());//set renderer
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

class myRenderer implements Renderer {
ByteBuffer byteBuffer;
FloatBuffer vertices;
FloatBuffer normals;
FloatBuffer colors;
FloatBuffer texturemap;
ShortBuffer index;
Bitmap bitmap;
int textureId;
float[] cube= {
		1f,-1f,1f,    
		1f,-1f,-1f,   
		1f,1f,-1f,    
		1f,1f,1f,     
		
		-1f,-1f,1f, 
		-1f,-1f,-1f,  
		-1f,1f,-1f,   
		-1f,1f,1f,    
		
		-1f,1f,1f,     
		1f,1f,1f,     
		1f,1f,-1f,     
		-1f,1f,-1f,    
		
		-1f,-1f,1f,    
		1f,-1f,1f,    
		1f,-1f,-1f,    
		-1f,-1f,-1f,   
		
		-1f,-1f,1f,    
		1f,-1f,1f,    
		1f,1f,1f,      
		-1f,1f,1f,     
		
		-1f,-1f,-1f,   
		1f,-1f,-1f,   
		1f,1f,-1f,    
		-1f,1f,-1f};
float [] cubeNormal={
		1f,0f,0f,
		1f,0f,0f,
		1f,0f,0f,
		1f,0f,0f,
		
		-1f,0f,0f,
		-1f,0f,0f,
		-1f,0f,0f,
		-1f,0f,0f,
		
		0f,1f,0f,
		0f,1f,0f,
		0f,1f,0f,
		0f,1f,0f,
		
		0f,-1f,0f,
		0f,-1f,0f,
		0f,-1f,0f,
		0f,-1f,0f,
		
		0f,0f,1f,
		0f,0f,1f,
		0f,0f,1f,
		0f,0f,1f,
		
		0f,0f,-1f,
		0f,0f,-1f,
		0f,0f,-1f,
		0f,0f,-1f};
float[] cubeTx={
		0f,1f,		
		1f,1f,	
		1f,0f,		
		0f,0f,	
		
		0f,1f,		
		1f,1f,		
		1f,0f,	
		0f,0f,		
		
		0f,1f,	
		1f,1f,		
		1f,0f,		
		0f,0f,	
		
		0f,1f,		
		1f,1f,		
		1f,0f,		
		0f,0f,		
				
	 	0f,1f,		
		1f,1f,		
		1f,0f,		
		0f,0f,		
		
	 	0f,1f,		
		1f,1f,		
		1f,0f,		
	 	0f,0f};
float[] cubeColor={
		1f,1f,1f,0.0f,
		1f,1f,1f,0.0f,
		1f,1f,1f,1f,
		1f,1f,1f,1f,
		
		1f,1f,1f,0.0f,
		1f,1f,1f,0.0f,
		1f,1f,1f,1f,
		1f,1f,1f,1f,
		
		1f,1f,1f,0.0f,
		1f,1f,1f,0.0f,
		1f,1f,1f,1f,
		1f,1f,1f,1f,
		
		1f,1f,1f,0.0f,
		1f,1f,1f,0.0f,
		1f,1f,1f,1f,
		1f,1f,1f,1f,
		
		1f,1f,1f,0.0f,
		1f,1f,1f,0.0f,
		1f,1f,1f,1f,
		1f,1f,1f,1f,
		
		1f,1f,1f,0.0f,
		1f,1f,1f,0.0f,
		1f,1f,1f,1f,
		1f,1f,1f,1f};
short[] cubeIndices = { 0, 1, 2, 2, 3, 0,
		4,5,6,6,7,4,
		8,9,10,10,11,8,
		12,13,14,14,15,12,
		16,17,18,18,19,16,
		20,21,22,22,23,20};

public myRenderer(){
		//Vertices
		byteBuffer = ByteBuffer.allocateDirect(24*3*4);
		//24 vertices*3 members(x,y,z)*4bytes each float
		byteBuffer.order(ByteOrder.nativeOrder());//set byteorder
		vertices = byteBuffer.asFloatBuffer();//vertices as floatbuffer
		vertices.put(cube);//copy float array to float buffer
		vertices.flip();//set limits from 0 to number of floats
		//Normals
		byteBuffer = ByteBuffer.allocateDirect(24*3*4);
		//24 normals*3 members(x,y,z)*4bytes each float
		byteBuffer.order(ByteOrder.nativeOrder());//set byteorder
		normals = byteBuffer.asFloatBuffer();//normals as floatbuffer
		normals.put(cubeNormal);//copy float array to float buffer
		normals.flip();//set limits from 0 to number of floats
		//Colors
		byteBuffer = ByteBuffer.allocateDirect(24*4*4);
		//24 vertices*4 members(R,G,B,A)*4bytes each float
		byteBuffer.order(ByteOrder.nativeOrder());//set byteorder
		colors = byteBuffer.asFloatBuffer();//colors as floatbuffer
		colors.put(cubeColor);//copy float array to float buffer
		colors.flip();//set limits from 0 to number of floats
		//TextreMap
		byteBuffer = ByteBuffer.allocateDirect(24*2*4);
		//24 vertices*2 membersTexX,TexY*4bytes each float
		byteBuffer.order(ByteOrder.nativeOrder());//set byteorder
		texturemap = byteBuffer.asFloatBuffer();//texturemap as floatbuffer
		texturemap.put(cubeTx);//copy float array to float buffer
		texturemap.flip();//set limits from 0 to number of floats
		//Indices
		byteBuffer = ByteBuffer.allocateDirect(36*2);
		//36 short indices*2bytes each short
		byteBuffer.order(ByteOrder.nativeOrder());
		index = byteBuffer.asShortBuffer();//index as shortbuffer
		index.put(cubeIndices);//copy array to buffer
		index.flip();//set limits
		//Texture bitmap
		try {
			 AssetManager assetManager = getAssets();
		 	 InputStream inputStream = assetManager.open("texture1.png");
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
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	//set viewport to the whole GLSurfaceView size
	gl.glViewport(0, 0, width, height);
	//set projection matrix as active matrix
	gl.glMatrixMode(GL10.GL_PROJECTION);  
	//load identity matrix to multiply with next matrix operation
	gl.glLoadIdentity();     
	//set perspective projection(GL10,FovY,Aspect,zNear,zFar)
	GLU.gluPerspective(gl, 70,width/(float)height,0.1f, 10f);
	//set ModelView matrix as active
	gl.glMatrixMode(GL10.GL_MODELVIEW);
	}

float angle;
	@Override
	public void onDrawFrame(GL10 gl) {
		//clear framebuffer and depth buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); 
		//set vertices shared by all cubes
		//set vertexpointer(3memberXYZ,FloatData,3Float*4Byte,dataBuffer)
		gl.glVertexPointer(3, GL10.GL_FLOAT, 12, vertices);
		//set normals shared by all cubes
		//normalpointer(dataTYPE,3float*4bytes,dataBuffer)
		gl.glNormalPointer(GL10.GL_FLOAT, 12, normals);

		//set light parameters
		gl.glLoadIdentity();
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
		
		//1st cube will use default color, so disable color array
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		//set default render color for 2nd cube
		gl.glColor4f(0, 1, 0.5f, 1);
				
		//transform 1st cube
		gl.glLoadIdentity();
		gl.glTranslatef(0, 0, -8);
		gl.glScalef(0.3f, 0.3f, 0.3f);	
		gl.glRotatef(angle, 1, 1, 0);
				
		//draw first cube
		gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_SHORT, index);
		//GL10.glDrawElements(DrawType,36 indices,shorData,dataBuffer)
		
		//second cube with Vertexcolor
		//enable color array
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		//glcolorpointer(4members RGBA,dataType,4floats*4bytes,dataBuffer)
		gl.glColorPointer(4, GL10.GL_FLOAT, 16, colors);
				
		//set transformation for second cube
		gl.glLoadIdentity();
		gl.glTranslatef(0, 0, -8);
		gl.glRotatef(angle, 0, 1, 0);
		//Draw second cube
		gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_SHORT, index);
		
		//third cube will use texture, disable color array
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
		
		//translate 3rd cube
		gl.glLoadIdentity();
		gl.glTranslatef(-5, -1, -7);
		gl.glRotatef(-angle, 0, 1, 0);
		//draw 3rd cube
		gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_SHORT, index);
		//unbind texture so other cubes dont use texture
		gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
		//disable texture state if not next frame first cube 
		//will try to use the texture
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		angle++;
	}

}

}

