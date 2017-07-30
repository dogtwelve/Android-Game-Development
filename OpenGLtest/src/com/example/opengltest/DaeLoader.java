package com.example.opengltest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.Matrix;
import android.util.Log;

public class DaeLoader {
	Context context;
	List<String> strings;
	float[] verticesAux;
	float[] normalsAux;
	float[] txAux;
	int[] polygons;
	float[] vertices;
	float[] normals;
	float[] tx;
	int polygonsCount;
	int[] polygonsVindex;
	float[] skinWeightsArray;
	int[] weightsVcount;
	int maxVcount=0;
	float [] weightsnoVcount;
	int[] weightsVindex;
	float [] weightsVnoindex;
	String[] skinJointsArray;
	boolean hasSkinningData=false;
	float [] perVertexWeights;
	int supportedBones=4;
	ArrayList<Bone> Bones;
	Bone bone;
	float[] skinBindPosesArray;
	boolean bindPosesFound=false;
	float[] bindShapeMatrix;
	ArrayList<String> hierarchy;
	float[] jointMatrix;
	float[] invBindPoseMatrix;
	

	
	public DaeLoader(Context context) {
		super();
		this.context = context;
		Bones=new ArrayList<Bone>();
		hierarchy=new ArrayList<String>();
	}
	
	public void DAEreader(String fileName){
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
			Log.d("dae Loader", fileName+" loaded");
			} catch (IOException e) {
			Log.d("dae Loader","Could not load "+fileName);
			} finally {
			if (inputStream != null)
			try {
			inputStream.close();
			} catch (IOException e) {
			Log.d("dae Loader","Couldn't close "+fileName);
			}
			}
		}
	
	public void load(String fileName){
		//open file
		DAEreader(fileName);
		//iterate through the whole strings array
		for (int i = 0; i < strings.size(); i++) {
			//get the string(i) from the list
			String string = strings.get(i);
			//if float_array is an array of data
			if (string.contains("<float_array id=")) {
				//if position array split the string on: "
				if (string.contains("positions-array")){
					String[] V = string.split("[\"]+");
					//log positions count to compare with array size
					Log.d("dae Loader","Positions Array found, count:"+V[3]);
					//check for closing tag if not found file corrupted
					if(V[4].contains("</float_array>")){Log.d("dae Loader", "Closing Tag found");
					//split string on: > 
					String[] V2 = V[4].split("[>]+");
					//split string on: <
					String[] V3 = V2[1].split("[<]+");
					//split string on spaces
					String[] V4 = V3[0].split("[ ]+");
					//initialize float array size
					verticesAux=new float[V4.length];
					//copy elements from string to float array
					for(int Vindex=0;Vindex<V4.length;Vindex++){verticesAux[Vindex]=Float.parseFloat(V4[Vindex]);}
					//log positions array size
					Log.d("dae Loader","verticesAux length: "+verticesAux.length);}
					}
				if (string.contains("normals-array")){
					//if normals array split on: "
					String[] N = string.split("[\"]+");
					//log normals count to compare with array size
					Log.d("dae Loader","Normals Array found, count:"+N[3]);
					//check for closing tag if not found file corrupted
					if(N[4].contains("</float_array>")){Log.d("dae Loader", "Closing Tag found");
					//split string on: > 
					String[] N2 = N[4].split("[>]+");
					//split string on: <
					String[] N3 = N2[1].split("[<]+");
					//split string on spaces
					String[] N4 = N3[0].split("[ ]+");
					//initialize float array size
					normalsAux=new float[N4.length];
					//copy elements from string to float array
					for(int Nindex=0;Nindex<N4.length;Nindex++){normalsAux[Nindex]=Float.parseFloat(N4[Nindex]);}
					//logs normals array size
					Log.d("dae Loader","normalAux length: "+normalsAux.length);}
				}
				if (string.contains("map-0")){
					//if texture map array split on: "
					String[] T = string.split("[\"]+");
					//log map-0 count to campare with array size
					Log.d("dae Loader","TextCoord Array found, count:"+T[3]);
					//check for closing tag if not found file corrupted
					if(T[4].contains("</float_array>")){Log.d("dae Loader", "Closing Tag found");
					//split string on: > 
					String[] T2 = T[4].split("[>]+");
					//split string on: <
					String[] T3 = T2[1].split("[<]+");
					//split string on spaces
					String[] T4 = T3[0].split("[ ]+");Log.d("dae Loader", ""+T4.length);
					//initialize float array size
					txAux=new float[T4.length];
					//copy elements from string to float array
					for(int Tindex=0;Tindex<T4.length;Tindex++){txAux[Tindex]=Float.parseFloat(T4[Tindex]);}
					//logs Tx map array size
					Log.d("dae Loader","txAux length: "+txAux.length);}
				}
				if (string.contains("skin-weights-array")){
					String[] Wa=string.split("[\"]+");
					skinWeightsArray=new float[Integer.parseInt(Wa[3])];
					String[] Wb=Wa[4].split("[>]+");
					String[] Wc=Wb[1].split("[<]+");
					String[] Wd=Wc[0].split("[ ]+");
					for(int j=0;j<skinWeightsArray.length;j++){skinWeightsArray[j]=Float.parseFloat(Wd[j]);}
					Log.d("dae Loader","<skin-weights-array>: "+skinWeightsArray.length);
				}
				if (string.contains("_pose_matrix-output-array")){
					String[] poseMatrix=string.split("[\"]+");
					bone=new Bone();
					bone.animationMatrices=new float[Integer.parseInt(poseMatrix[3])];
					String[] boneSid=poseMatrix[1].split("_pose_matrix-output-array");
					String[] boneSid2=boneSid[0].split("Armature_");
					bone.sid=boneSid2[1];
					String[] poseMatrix2=poseMatrix[4].split("[>]+");
					String[] poseMatrix3=poseMatrix2[1].split("[<]+");
					String[] poseMatrix4=poseMatrix3[0].split("[ ]+");
					for(int u=0;u<bone.animationMatrices.length;u++){bone.animationMatrices[u]=Float.parseFloat(poseMatrix4[u]);}
					//String animMat=""; for(int q=0;q<bone.animationMatrices.length;q++){animMat+=" "+bone.animationMatrices[q];}
					//Log.d("dae Loader", "bone: "+bone.sid+" animMatrix: "+animMat);
					Bones.add(bone);
					Log.d("dae Loader", "Bone list size: "+Bones.size());
				}
				if (string.contains("skin-bind_poses")){
					String[] bp=string.split("[\"]+"); 
					String[] bp2=bp[4].split("[>]+");
					String[] bp3=bp2[1].split("[<]+");
					String[] bp4=bp3[0].split("[ ]+");
					skinBindPosesArray=new float[Integer.parseInt(bp[3])];
					for(int y=0;y<skinBindPosesArray.length;y++){skinBindPosesArray[y]=Float.parseFloat(bp4[y]);}
					//String msg=""; for(int y=0;y<skinBindPosesArray.length;y++){msg+=" "+skinBindPosesArray[y];} 
					//Log.d("dae Loader", "Skin Bind Poses Array"+msg); 
					bindPosesFound=true;
				}
				}
			if (string.contains("<polylist")){String[] Pol = string.split("[\"]+"); 
			polygonsCount=Integer.parseInt(Pol[Pol.length-2]);Log.d("dae Loader", "Polygons count:"+polygonsCount);}
			if (string.contains("<p>")) {Log.d("dae Loader", "Polygons Array found");
			String[] P = string.split("[>]+");
			String[] P2 = P[1].split("[<]+");
			String[] P3 = P2[0].split("[ ]+");Log.d("dae Loader",""+P3.length);
			polygons=new int[P3.length];
			for(int Pindex=0;Pindex<P3.length;Pindex++){polygons[Pindex]=Integer.parseInt(P3[Pindex]);}
			}
			if (string.contains("<vertex_weights")){
			String[] Waux=string.split("[\"]+");
			weightsVcount=new int[Integer.parseInt(Waux[1])];
			String string2 = strings.get(i+3);
			if (string2.contains("<vcount>")){
				String[] w2= string2.split("[>]+");
				String[] w3= w2[1].split("[<]+");
				String[] w4= w3[0].split("[ ]+");
				for(int W=0;W<weightsVcount.length;W++){weightsVcount[W]=Integer.parseInt(w4[W]); if(weightsVcount[W]>maxVcount){maxVcount=weightsVcount[W];}}
				Log.d("dae Loader","<vertex_weights> <Vcount> : "+weightsVcount.length);
				Log.d("dae Loader","maxVcout :"+maxVcount); 
				hasSkinningData=true;
				}
			polygonsVindex=new int[polygonsCount*3];
			for(int h=0;h<polygonsCount*3;h++){polygonsVindex[h]=polygons[h*3];}
			Log.d("dae Loader","polygonsVindex: "+polygonsVindex.length);
			}
			if(string.contains("<v>")){
			String [] V=string.split("[>]+");
			String [] V1=V[1].split("[<]+");
			String [] V2=V1[0].split("[ ]+");
			weightsVindex=new int[V2.length];
			for(int y=0;y<V2.length;y++){weightsVindex[y]=Integer.parseInt(V2[y]);}
			Log.d("dae Loader", "<V>: "+weightsVindex.length);
			}
			if(string.contains("<Name_array id=")){
				if(string.contains("skin-joints-array")){
					String[] B=string.split("[>]+");
					String[] B2=B[1].split("[<]+");
					skinJointsArray=B2[0].split("[ ]+");
					Log.d("dae Loader", "<skin-joints-array>: "+skinJointsArray.length);
				}
			}
			if(string.contains("type=\"JOINT\"")){
				String[] boneName=string.split("sid=\""); 
				String[] boneName2=boneName[1].split("[\"]+"); Log.d("dae Loader", "bone "+boneName2[0]); hierarchy.add(boneName2[0]);
				if(Bones.size()!=0){
					for(int r=0;r<Bones.size();r++){
						if(Bones.get(r).sid.matches(boneName2[0])){ if(hierarchy.size()>1){Bones.get(r).parentSid=hierarchy.get(hierarchy.size()-2);Bones.get(r).hasParent=true;}
							String auxString = strings.get(i+1);
							if(auxString.contains("matrix sid=\"transform\"")){
								String[] matrix=auxString.split("[>]+");
								String[] matrix2=matrix[1].split("[<]+");
								String[] matrix3=matrix2[0].split("[ ]+"); 
								for(int k=0; k<Bones.get(r).transformMatrix.length;k++){
									Bones.get(r).transformMatrix[k]=Float.parseFloat(matrix3[k]);
								}
								//Log.d("dae Loader", ""+Bones.get(r).sid+" transform Matrix: "+Bones.get(r).transformMatrix[0]+" "+Bones.get(r).transformMatrix[1]+" "+Bones.get(r).transformMatrix[2]+" "+Bones.get(r).transformMatrix[3]+" "+Bones.get(r).transformMatrix[4]+" "+Bones.get(r).transformMatrix[5]+" "+Bones.get(r).transformMatrix[6]+" "+Bones.get(r).transformMatrix[7]+" "+Bones.get(r).transformMatrix[8]+" "+Bones.get(r).transformMatrix[9]+" "+Bones.get(r).transformMatrix[10]+" "+Bones.get(r).transformMatrix[11]+" "+Bones.get(r).transformMatrix[12]+" "+Bones.get(r).transformMatrix[13]+" "+Bones.get(r).transformMatrix[14]+" "+Bones.get(r).transformMatrix[15]+" ");
							//if(Bones.get(r).hasParent){Log.d("dae Loader", "bone: "+Bones.get(r).sid+" Parent: "+Bones.get(r).parentSid);}
							}
						}
					}
				}
				//Log.d("dae Loader",i+""+ string);
			}
			if(string.contains("</node>")){if(hierarchy.size()>0){hierarchy.remove(hierarchy.size()-1);}}
			if(string.contains("<bind_shape_matrix>")){
				String[] bsm=string.split("[>]+");
				String[] bsm2=bsm[1].split("[<]+");
				String[] bsm3=bsm2[0].split("[ ]+");
				bindShapeMatrix=new float[bsm3.length];
				for(int w=0;w<bsm3.length;w++){bindShapeMatrix[w]=Float.parseFloat(bsm3[w]);}
				//String bsm4=""; for(int h=0;h<bindShapeMatrix.length;h++){bsm4+=bindShapeMatrix[h]+" ";}Log.d("dae Loader","bindShapeMatrix: "+bsm4);
			}
		}
		
		//allocate polygons*vertices*elements arrays
		vertices=new float[polygonsCount*3*3];
		normals=new float[polygonsCount*3*3];
		tx=new float[polygonsCount*3*2];
		//read every triangle and construct V1,V2,V3 per triangle
		for (int index=0;index<polygonsCount;index++){
			vertices[index*9]=verticesAux[polygons[index*9]*3];
			vertices[(index*9)+1]=verticesAux[(polygons[index*9]*3)+1];
			vertices[(index*9)+2]=verticesAux[(polygons[(index*9)]*3)+2];
			normals[index*9]=normalsAux[(polygons[(index*9)+1]*3)];
			normals[(index*9)+1]=normalsAux[(polygons[(index*9)+1]*3)+1];
			normals[(index*9)+2]=normalsAux[(polygons[(index*9)+1]*3)+2];
			tx[index*6]=txAux[(polygons[(index*9)+2]*2)];
			tx[(index*6)+1]=1-txAux[(polygons[(index*9)+2]*2)+1];
			vertices[(index*9)+3]=verticesAux[(polygons[(index*9)+3]*3)];
			vertices[(index*9)+4]=verticesAux[(polygons[(index*9)+3]*3)+1];
			vertices[(index*9)+5]=verticesAux[(polygons[(index*9)+3]*3)+2];
			normals[(index*9)+3]=normalsAux[(polygons[(index*9)+4]*3)];
			normals[(index*9)+4]=normalsAux[(polygons[(index*9)+4]*3)+1];
			normals[(index*9)+5]=normalsAux[(polygons[(index*9)+4]*3)+2];
			tx[(index*6)+2]=txAux[(polygons[(index*9)+5]*2)];
			tx[(index*6)+3]=1-txAux[(polygons[(index*9)+5]*2)+1];
			vertices[(index*9)+6]=verticesAux[(polygons[(index*9)+6]*3)];
			vertices[(index*9)+7]=verticesAux[(polygons[(index*9)+6]*3)+1];
			vertices[(index*9)+8]=verticesAux[(polygons[(index*9)+6]*3)+2];
			normals[(index*9)+6]=normalsAux[(polygons[(index*9)+7]*3)];
			normals[(index*9)+7]=normalsAux[(polygons[(index*9)+7]*3)+1];
			normals[(index*9)+8]=normalsAux[(polygons[(index*9)+7]*3)+2];
			tx[(index*6)+4]=txAux[(polygons[(index*9)+8]*2)];
			tx[(index*6)+5]=1-txAux[(polygons[(index*9)+8]*2)+1];
			}
		//check if skinning data was loaded
		if(hasSkinningData){
			//substitute the weight index on <v> with the value on the index position taken from the skin-weights-array 
			//and save it as a new array we will have (joint index-weight value) pairs on array instead of 
			//(joint index-weight index) pairs from original <v>
			int wIndex; weightsVnoindex=new float[weightsVindex.length];
			for(int index=0;index<weightsVindex.length;index+=2){
				weightsVnoindex[index]=weightsVindex[index];
				wIndex=weightsVindex[index+1];
				weightsVnoindex[index+1]=skinWeightsArray[wIndex];
				//Log.d("dae Loader"," "+weightsVindex[index]+" "+weightsVindex[index+1]+" "+weightsVnoindex[index]+" "+weightsVnoindex[index+1]+" "+skinWeightsArray[wIndex]);
			}
			Log.d("dae Loader","weightsV no index: "+weightsVnoindex.length+" total weights: "+weightsVnoindex.length/2);
			weightsnoVcount=new float[weightsVcount.length*(supportedBones*2)]; int auxIndex=0; int lastauxIndex=0;
			for(int t=0; t<weightsVcount.length;t++){
				for(int element=0;auxIndex<lastauxIndex+weightsVcount[t] && element<(supportedBones*2);auxIndex++,element+=2){
					weightsnoVcount[(t*(supportedBones*2))+element]=weightsVnoindex[auxIndex*2];
					weightsnoVcount[(t*(supportedBones*2))+element+1]=weightsVnoindex[(auxIndex*2)+1];
				} lastauxIndex=lastauxIndex+weightsVcount[t]; auxIndex=lastauxIndex;/////////&& element<(supportedBones*2) checks for last element numbers out of bounds exception////////////////
			}
			Log.d("dae Loader","weights no Vcount: "+weightsnoVcount.length+"influenced vertices:"+weightsnoVcount.length/(supportedBones*2));
			//for(int abc=722;abc<752;abc++){Log.d("dae Loader",weightsnoVcount[(abc*8)]+" "+weightsnoVcount[(abc*8)+1]+" "+weightsnoVcount[(abc*8)+2]+" "+weightsnoVcount[(abc*8)+3]+" "+weightsnoVcount[(abc*8)+4]+" "+weightsnoVcount[(abc*8)+5]+" "+weightsnoVcount[(abc*8)+6]+" "+weightsnoVcount[(abc*8)+7]+" ");}
		
			perVertexWeights= new float[polygonsVindex.length*(supportedBones*2)];
			for(int z=0;z<polygonsVindex.length;z++){
				for(int w=0; w<(supportedBones*2);w++){
					perVertexWeights[(z*(supportedBones*2))+w]=weightsnoVcount[(polygonsVindex[z]*(supportedBones*2))+w];
				}
			}
			//LOG WEIGHTS
		//	String logW;
		//	for(int abc=0;abc<perVertexWeights.length/(supportedBones*2);abc++){
		//		logW="";
		//		for(int l=0; l<supportedBones*2;l++){logW+=perVertexWeights[(abc*(supportedBones*2))+l]+" ";}
		//		Log.d("dae Loader",logW);}
			Log.d("dae Loader", "Weights per vertex: "+perVertexWeights.length+" Vertices: "+perVertexWeights.length/(supportedBones*2));
			for(int u=0;u<skinJointsArray.length;u++){
				for(int p=0;p<Bones.size();p++){
					if(Bones.get(p).sid.matches(skinJointsArray[u])){
						for(int g=0;g<Bones.get(p).invBindPoseMatrix.length;g++){
							Bones.get(p).invBindPoseMatrix[g]=skinBindPosesArray[(u*16)+g];
						}String pm="";for(int c=0;c<16;c++){pm+=" "+Bones.get(p).invBindPoseMatrix[c];}Log.d("dae Loader"," "+Bones.get(p).sid+" bind pose matrix: "+pm);
					}
				}
			}
			
			///////////////
			rowTOcolumn(bindShapeMatrix);
			for (int b=0;b<Bones.size();b++){
			rowTOcolumn(Bones.get(b).transformMatrix);
			rowTOcolumn(Bones.get(b).animationMatrices);
			rowTOcolumn(Bones.get(b).invBindPoseMatrix);}
			///////////////
		restPose();
		}
	}//load function end
	
	
	
	

//////
	public void rowTOcolumn(float m[]){
		float[] auxm=new float[m.length];
		for(int p=0;p<(m.length/16);p++){
			for(int u=0;u<16;u++){
				auxm[(p*16)+u]=m[(p*16)+u];
			}
		} 
		for(int p=0;p<(m.length/16);p++){Matrix.transposeM(m, p*16, auxm, p*16);}
	} 
/////	
	
	float[] verticesBindShape;
	public void restPose(){
		 //hierarchy multiplication of resting pose matrices   
	    for(int p=0; p<Bones.size();p++){
	    	if(Bones.get(p).hasParent){int parentIndex=0; for(int q=0;q<Bones.size();q++){
	    		if(Bones.get(q).sid.matches(Bones.get(p).parentSid)){parentIndex=q;}}
	    		Matrix.multiplyMM(Bones.get(p).worldMatrix, 0, Bones.get(p).transformMatrix, 0, Bones.get(parentIndex).worldMatrix, 0);}
	    	else{for (int k=0; k<16;k++){Bones.get(p).worldMatrix[k]=Bones.get(p).transformMatrix[k];}}
	    }
	    //fill jointMatrix following skinJointsArray indexing
	    jointMatrix=new float[Bones.size()*16];
	    for(int p=0;p<skinJointsArray.length;p++){
	    	for(int b=0;b<Bones.size();b++){
	    	if(Bones.get(b).sid.matches(skinJointsArray[p])){
	    	for(int j=0;j<16;j++){
	    		jointMatrix[(p*16)+j]=Bones.get(b).worldMatrix[j];
	    		}}}}
	    //fill invBindPoseMatrix following skinJointsArray indexing
	    invBindPoseMatrix=new float[Bones.size()*16];
	    for(int p=0;p<skinJointsArray.length;p++){
	    	for(int b=0;b<Bones.size();b++){
	    	if(Bones.get(b).sid.matches(skinJointsArray[p])){
	    	for(int j=0;j<16;j++){
	    		invBindPoseMatrix[(p*16)+j]=Bones.get(b).invBindPoseMatrix[j];
	    		}}}}
	    //premultiply mesh with bind shape matrxi
		verticesBindShape=new float[vertices.length];
		for(int c=0;c<vertices.length/3;c++){
			verticesBindShape[c*3]=(vertices[c*3]*bindShapeMatrix[0])+(vertices[(c*3)+1]*bindShapeMatrix[4])+(vertices[(c*3)+2]*bindShapeMatrix[8])+bindShapeMatrix[12];
			verticesBindShape[(c*3)+1]=(vertices[c*3]*bindShapeMatrix[1])+(vertices[(c*3)+1]*bindShapeMatrix[5])+(vertices[(c*3)+2]*bindShapeMatrix[9])+bindShapeMatrix[13];
			verticesBindShape[(c*3)+2]=(vertices[c*3]*bindShapeMatrix[2])+(vertices[(c*3)+1]*bindShapeMatrix[6])+(vertices[(c*3)+2]*bindShapeMatrix[10])+bindShapeMatrix[14];	
		}
	  }
	
	
	
	float auxTime=0f; int frame=0;
	public void animate(float deltaTime, int fStart, int fEnd){
    	//check time and change frame
        if(auxTime>0.25){frame++;auxTime=0;if(frame==fEnd){frame=fStart;}}auxTime+=deltaTime;
        //hierarchy multiplication of animationMatrices
        for(int p=0; p<Bones.size();p++){
        	if(Bones.get(p).hasParent){int parentIndex=0; for(int q=0;q<Bones.size();q++){
        		if(Bones.get(q).sid.matches(Bones.get(p).parentSid)){parentIndex=q;}}
        		Matrix.multiplyMM(Bones.get(p).worldMatrix, 0, Bones.get(p).animationMatrices, frame*16, Bones.get(parentIndex).worldMatrix, 0);}
        	else{for (int k=0; k<16;k++){Bones.get(p).worldMatrix[k]=Bones.get(p).animationMatrices[(frame*16)+k];}}
        }
        //fill jointMatrix according to skinJointsArray indexing
        for(int p=0;p<skinJointsArray.length;p++){
        	for(int b=0;b<Bones.size();b++){
        	if(Bones.get(b).sid.matches(skinJointsArray[p])){
        	for(int j=0;j<16;j++){
        		jointMatrix[(p*16)+j]=Bones.get(b).worldMatrix[j];
        		}}}}
    }
	
	

	
	
	
	
	public class Bone{
		String sid;
		float [] transformMatrix;
		float [] animationMatrices;
		float [] invBindPoseMatrix;
		boolean hasParent;
		float [] worldMatrix;
		String parentSid;
		
		public Bone(){super();
		transformMatrix=new float[16];
		invBindPoseMatrix=new float[16];
		hasParent=false;
		worldMatrix=new float[16];
		}
	}
}

