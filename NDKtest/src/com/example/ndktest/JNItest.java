package com.example.ndktest;

public class JNItest {

    static {
        System.loadLibrary("NDKtest");
    }

  
    public static native void surfaceChanged(int width, int height);
    public static native void drawFrame();
    public static native void surfaceCreated();
}
