package com.example.opengltest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class ObjLoader {
	Context context;
	List<String> strings;
	float[] vertices;
	float[] normal;
	float[] tx;

	public ObjLoader(Context context) {
		super();
		this.context = context;
	}
	
	public void OBJreader(String fileName){
		AssetManager assetManager = context.getAssets();
		InputStream inputStream = null;
		InputStreamReader inputStreamReader;
		BufferedReader bufferedReader;
		String string=null;
		strings = new ArrayList <String>();
		try {
			inputStream = assetManager.open(fileName);
			inputStreamReader=new InputStreamReader(inputStream);
			bufferedReader= new BufferedReader(inputStreamReader);
			while ((string = bufferedReader.readLine()) != null){
				strings.add(string);
			}
			Log.d("obj loader", fileName+" loaded");
			} catch (IOException e) {
			Log.d("obj loader","Could not load "+fileName);
			} finally {
			if (inputStream != null)
			try {
			inputStream.close();
			} catch (IOException e) {
			Log.d("obj loader","Couldn't close "+fileName);
			}
			}
		}
	
	public void load(String fileName){
		int vertexIndex=0;
		int numVertices=0;
		int normalIndex=0;
		int numNormals=0;
		int txIndex=0;
		int numTX=0;
		int fIndex=0;
		int numface=0;
		int v=0,vn=0,vt=0,vf=0;
		OBJreader(fileName);
		for (int i = 0; i < strings.size(); i++) {
			String string = strings.get(i);
			if (string.startsWith("v ")) {v++;}
			if (string.startsWith("vn ")) {vn++;}
			if (string.startsWith("vt ")) {vt++;}
			if (string.startsWith("f ")) 
			{String[] auxstring = string.split("[ ]+");
			if(auxstring.length==5){vf++;vf++;}else{vf++;}}}
			Log.d("stringssize ", ""+strings.size());
		float[] verticesAUX=new float[v*3];
		float[] normalsAUX= new float[vn*3];
		float[] txAUX= new float[vt*2];
		int[] f=new int[vf*9];
		Log.d("vsize ", ""+verticesAUX.length);
		Log.d("nsize ", ""+normalsAUX.length);
		Log.d("tsize ", ""+txAUX.length);
		Log.d("fsize ", ""+f.length);
		for (int i = 0; i < strings.size(); i++) {
			String string = strings.get(i);
				if (string.startsWith("v ")) {
				String[] V = string.split("[ ]+");
				verticesAUX[vertexIndex] = Float.parseFloat(V[1]);
				vertexIndex++;
				verticesAUX[vertexIndex] = Float.parseFloat(V[2]);
				vertexIndex++;
				verticesAUX[vertexIndex] = Float.parseFloat(V[3]);
				vertexIndex++;
				numVertices++;
				continue;
				}
			if (string.startsWith("vn ")) {
				String[] N = string.split("[ ]+");
				normalsAUX[normalIndex] = Float.parseFloat(N[1]);
				normalIndex++;
				normalsAUX[normalIndex] = Float.parseFloat(N[2]);
				normalIndex++;
				normalsAUX[normalIndex] = Float.parseFloat(N[3]);
				normalIndex++;
				numNormals++;
				continue;
				}
			if (string.startsWith("vt ")) {
				String[] TEX = string.split("[ ]+");
				txAUX[txIndex] = Float.parseFloat(TEX[1]);
				txIndex++;
				txAUX[txIndex] = Float.parseFloat(TEX[2]);
				txIndex++;
				numTX++;
				continue;
				}
			if (string.startsWith("f ")) {
				String[] Face = string.split("[ ]+");
				if(Face.length==4){
				String[] V1=Face[1].split("/");
				String[] V2=Face[2].split("/");
				String[] V3=Face[3].split("/");
				f[fIndex]=Integer.parseInt(V1[0]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V1[1]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V1[2]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V2[0]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V2[1]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V2[2]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V3[0]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V3[1]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V3[2]);
				fIndex++;
				numface++;}
				if(Face.length==5){
				String[] V1=Face[1].split("/");
				String[] V2=Face[2].split("/");
				String[] V3=Face[3].split("/");
				String[] V4=Face[4].split("/");
				f[fIndex]=Integer.parseInt(V1[0]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V1[1]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V1[2]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V2[0]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V2[1]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V2[2]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V3[0]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V3[1]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V3[2]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V3[0]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V3[1]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V3[2]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V4[0]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V4[1]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V4[2]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V1[0]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V1[1]);
				fIndex++;
				f[fIndex]=Integer.parseInt(V1[2]);
				fIndex++;
				numface++;
				numface++;}
				//Log.d("facesize ", Face.length+"");
				}
		}
		vertices=new float[(f.length/3)*3];
		tx=new float[(f.length/3)*2];
		normal= new float[(f.length/3)*3];
		Log.d("vertices",""+vertices.length);
		Log.d("tx",""+tx.length);
		Log.d("normal",""+normal.length);
		for(int n=0,nt=0; n<fIndex;n+=3,nt+=2){
		vertices[n]=verticesAUX[(3*f[n])-3];
		vertices[n+1]=verticesAUX[(3*f[n])-2];
		vertices[n+2]=verticesAUX[(3*f[n])-1];
		tx[nt]=txAUX[(2*f[n+1])-2];
		tx[nt+1]=1-txAUX[(2*f[n+1])-1]; 
		//1-texture Y check OBJ standard texture coordinates mapping
		normal[n]=normalsAUX[(3*f[n+2])-3];
		normal[n+1]=normalsAUX[(3*f[n+2])-2];
		normal[n+2]=normalsAUX[(3*f[n+2])-1];
		}
		Log.d("Obj loader",fileName+" buffers created");
		//output whole obj file to LOGCAT
		//for(int h=0;h<vertices.length;h+=3)
		//{Log.d("Vertices ",vertices[h]+","+vertices[h+1]+","+vertices[h+2]);}
		//for(int h=0;h<tx.length;h+=2)
		//{Log.d("texture ",tx[h]+","+tx[h+1]);}	
		//for(int h=0;h<normal.length;h+=3)
		//{ Log.d("normals ",normal[h]+","+normal[h+1]+","+normal[h+2]);}
	}
}

