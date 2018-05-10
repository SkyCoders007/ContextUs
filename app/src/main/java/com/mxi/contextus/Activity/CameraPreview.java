package com.mxi.contextus.Activity;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {


        try {

            if (mCamera == null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {

            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error stop camera preview: " + e.getMessage());
        }
        setCamera(camera);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();

        boolean bestAspectRatioset=false;
        int i=0;
        int getAspectRatio=0;
        for (Camera.Size size: previewSizes) {

            double aspectRatio=size.width/size.height;
            if(!bestAspectRatioset){
                if(aspectRatio == 1.7777777778){
                    getAspectRatio=i;
                    bestAspectRatioset=true;
                }
            }

            Log.d("CameraSurface","PreviewSize: " + size.width + "x"+ size.height + "/n");
        }

        Camera.Size highestPreviewSize = params.getSupportedPreviewSizes().get(getAspectRatio);
        Log.d("CameraSurface","PreviewSize: " + highestPreviewSize.width + "x"+ highestPreviewSize.height + "/n");
        params.setPreviewSize(highestPreviewSize.width,highestPreviewSize.height);

        mCamera.setParameters(params);
        refreshCamera(mCamera);
    }

    public void setCamera(Camera camera) {

        mCamera = camera;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        mCamera.release();
    }
}
