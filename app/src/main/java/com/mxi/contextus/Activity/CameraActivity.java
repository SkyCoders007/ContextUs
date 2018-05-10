package com.mxi.contextus.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mxi.contextus.Database.SQLitehelper;
import com.mxi.contextus.R;
import com.mxi.contextus.Util.CommanClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private Button btn_capture;
    private Context myContext;
    private FrameLayout fl_cameraPreview;
    Camera.Parameters params;

    ImageView iv_lastImage, iv_back, iv_flash;

    CommanClass cc;
    SQLitehelper dbcon;
    Bitmap img_bitmap;

    boolean isImageProcessCompleted = true;
    boolean isFlashOn = true;
    String project_id = "";
    String project_name, image_name, image_path;
    int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    int img_sequence_number;
    private int degrees = -1;
    private int orientation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        initialize();
        dbcon = new SQLitehelper(this);
        cc = new CommanClass(this);

        checkCameraPermission();
        Intent intent = getIntent();
        project_name = intent.getStringExtra("ProjectName");

        if (cc.loadPrefBoolean("noProjectCamera")) {
            img_sequence_number = 0;
        } else {
            img_sequence_number = getCurrentProjectTotalPics(project_name);

        }

        SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(new SensorEventListener() {


            @Override
            public void onSensorChanged(SensorEvent event) {
//                Log.e("00000000000",event.values[1]+"");
                if (event.values[1] < 6.5 && event.values[1] > -6.5) {
                    if (orientation != 1) {
                        Log.d("Sensor", orientation + "");
                    }
                    orientation = 1;
                } else {
                    if (orientation != 0) {
                        Log.d("Sensor", orientation + "");
                    }
                    orientation = 0;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub

                Log.e("11111111111",sensor+"");
                Log.e("22222222222",accuracy+"");

            }
        }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

    }


    private int findBackFacingCamera() {
        int cameraId = -1;

        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext)) {
            finish();
        }

        releaseCameraAndPreview();
        if (mCamera == null) {

            try {
                mCamera = Camera.open(findBackFacingCamera());
                params = mCamera.getParameters();

                List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();

                boolean bestAspectRatioset=false;
                int i=0;
                int getAspectRatio=0;
                for (Camera.Size size: pictureSizes) {
                        i++;
                    Log.d("CameraSurface","pictureSizes: " + size.width + "x"+ size.height + "/n");
                    double aspectRatio=size.width/size.height;

                if(!bestAspectRatioset){
                    if(aspectRatio == 1.7777777778){
                        getAspectRatio=i;
                        bestAspectRatioset=true;
                    }
                }

                }

                Camera.Size highestPreviewSize = params.getSupportedPictureSizes().get(getAspectRatio);
                Log.d("CameraSurface","pictureSizes: " + highestPreviewSize.width + "x"+ highestPreviewSize.height + "/n");
                params.setPreviewSize(highestPreviewSize.width,highestPreviewSize.height);

                params.setPictureSize(highestPreviewSize.width,highestPreviewSize.height);
                params.setPictureFormat(PixelFormat.JPEG);
                params.setJpegQuality(100);

                mCamera.setParameters(params);

            } catch (Exception e) {
                e.printStackTrace();
            }

            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }

    }


    public void initialize() {
        fl_cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(myContext, mCamera);
        fl_cameraPreview.addView(mPreview);

        iv_lastImage = (ImageView) findViewById(R.id.iv_lastImage);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        btn_capture = (Button) findViewById(R.id.button_capture);

        iv_flash = (ImageView) findViewById(R.id.iv_flash);
        iv_flash.setOnClickListener(flashlistener);

        btn_capture.setOnClickListener(captrureListener);
        iv_back.setOnClickListener(backListner);

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private boolean hasCamera(Context context) {

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private void releaseCameraAndPreview() {
        mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.e("Camera",data+"");

                File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/lessunknown/" + project_name + "/" + "DCIM/");

                if (!mediaStorageDir.exists()) {
                    mediaStorageDir.mkdirs();
                }

                String[] parts = project_name.split("_");
                String part1 = parts[0];
                String p_name = part1;
                SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMMdd");
                SimpleDateFormat sdf_time = new SimpleDateFormat("hhmmss");

                image_name = p_name + "_" + sdf_date.format(Calendar.getInstance().getTime()) + "_" + sdf_time.format(Calendar.getInstance().getTime()) + ".jpg";
                image_path = mediaStorageDir + "/" + image_name;

                final File file = new File(image_path);

                if (file == null) {
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.close();
                    img_sequence_number++;

                    Cursor cur;
                    cur = dbcon.getProjectIdByName(project_name);
                    cc.savePrefBoolean("noProjectCamera", false);
                    if (cur != null && cur.getCount() != 0) {
                        cur.moveToFirst();
                        project_id = cur.getString(0);
                    }
/*
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;*/
                    img_bitmap = BitmapFactory.decodeFile(file.getPath());

                    iv_lastImage.setImageBitmap(img_bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Cursor cur;
                cur = dbcon.getProjectIdByName(project_name);
                cc.savePrefBoolean("noProjectCamera", false);
                if (cur != null && cur.getCount() != 0) {
                    cur.moveToFirst();
                    project_id = cur.getString(0);
                }

                Log.e("Width, Height", img_bitmap.getWidth() + "," + img_bitmap.getHeight() + "");
                dbcon.insertPics(project_id, img_sequence_number + "", img_bitmap.getHeight() + "", img_bitmap.getWidth() + "", image_path, getCurrentDate(), null, image_name, String.valueOf(orientation));
                cc.savePrefString("lastUsedProject_image_position", img_sequence_number + "");

                isImageProcessCompleted = true;
                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }

    View.OnClickListener captrureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (isImageProcessCompleted) {
                mCamera.takePicture(null, null, mPicture);
                isImageProcessCompleted = false;
            }

        }
    };

    View.OnClickListener flashlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (isFlashOn) {
                turnOnFlash();
            } else {
                turnOffFlash();
            }

        }
    };

    private void turnOffFlash() {
        toggleButtonImage();
        isFlashOn = true;

        params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);

    }

    private void turnOnFlash() {
        toggleButtonImage();
        isFlashOn = false;

        params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        mCamera.setParameters(params);
        mCamera.startPreview();


    }

    private void toggleButtonImage() {
        if (isFlashOn) {
            iv_flash.setImageResource(R.drawable.tp_flash_on);
        } else {
            iv_flash.setImageResource(R.drawable.tp_flash_off);
        }
    }

    View.OnClickListener backListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(CameraActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    };


    private void releaseCamera() {

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(CameraActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(CameraActivity.this,
                    Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(CameraActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
    }

    public int getCurrentProjectTotalPics(String currentProject) {
        String currentProjectID = "";
        Cursor cur;
        cur = dbcon.getProjectIdByName(currentProject);
        if (cur != null && cur.getCount() != 0) {
            cur.moveToFirst();
            currentProjectID = cur.getString(0);
        }
        int i = 0;
        Cursor cur1 = dbcon.getPicsFromProjectId(currentProjectID);
        cur1.moveToFirst();
        if (cur1 != null && cur1.getCount() != 0) {
            do {
                i++;
            } while (cur1.moveToNext());
        }
        return i;
    }

    public String getCurrentDate() {

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyyyy");
        String last_update = sdf.format(Calendar.getInstance().getTime());

        return last_update;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CameraActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}