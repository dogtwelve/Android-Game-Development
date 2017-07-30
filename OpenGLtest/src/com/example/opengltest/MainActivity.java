package com.example.opengltest;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
public class MainActivity extends ListActivity {
//public String activity[] = {"DrawTest","Triangletest","IndexTest","VertexColor","VertexColor2","TextureTest","TextureTest2","GLTransformation","PhysicsTest","CollisionTest","Animation2D","TouchTest","CamTest","Perspective3DProjection"};
public String activity[] = {"OpenGL ES 2.0 examples:","DrawTest","GLES20draw","GLES20attributes","GLES20uniforms","GLES20samplers","GLES20vbo","GLES20physics","GLES20anim2D","GLES20cam2D","GLES20DirLight","GLES20spotLight","GLES20navigation","GLES20stencil","GLES20EGL","Modeling3D","KeyFrameAnimation3D","MultipleShaders","SkeletalAnimation","OpenGL ES 1.0 (old devices) examples:","Triangletest","IndexTest","VertexColor","TextureTest","TextureTest2","GLTransformation","PhysicsTest","CollisionTest","Animation2D","TouchTest","CamTest","Perspective3DProjection","Light","FPSNavigation","OBJ3Dmodel","VerletPhysics"};
@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, activity));
}
@Override
public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
getMenuInflater().inflate(R.menu.main, menu);
return true;
}
@Override
protected void onListItemClick(ListView list, View view, int position, long id) {
super.onListItemClick(list, view, position, id);
String activityName = activity[position];
try {
Class klass = Class.forName("com.example.opengltest." + activityName);
Intent intent = new Intent(this, klass);
startActivity(intent);
} catch (ClassNotFoundException e) {
e.printStackTrace();
}}
}
