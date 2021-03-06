package com.example.myshh.snapclient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    Button btnSwitchCamera;
    boolean setBackCamera = true;
    FrameLayout preview;
    private ImageView imageView;
    boolean flagCanTakePicture = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        prepareCameraView(setBackCamera);

        Button captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                v -> {
                    // get an image from the camera
                    if (flagCanTakePicture) {
                        flagCanTakePicture = false;
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );

        Button btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnSwitchCamera.setOnClickListener(
                v -> {
                    setBackCamera = !setBackCamera;
                    releaseCamera();
                    prepareCameraView(setBackCamera);
                }
        );
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }*/

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            mCamera.startPreview();
            if (pictureFile == null){
                System.out.println("Creating file failed");
                flagCanTakePicture = true;
                return;
            }

            try {
                //Sending byte array to server
                MessageObject messageObject = new MessageObject();
                messageObject.imageBytes = data;
                new Thread(() -> {
                    try {
                        StaticSocket.objectOutputStream.writeObject(messageObject);
                        StaticSocket.objectOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                //Convert byte data to bitmap
                imageView = new ImageView(getApplicationContext());
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(data,0, data.length));

                //preview.removeAllViews();
                //Add image to screen
                //preview.addView(imageView);

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://"+ pictureFile)));
                flagCanTakePicture = true;
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Saving image failed");
            }
        }
    };

    private void prepareCameraView(boolean setBackCamera){
        mCamera = getCameraInstance(setBackCamera);
        Camera.Parameters cameraParameters = mCamera.getParameters();
        cameraParameters.setPictureSize(cameraParameters.getSupportedPictureSizes().get(0).width,
                cameraParameters.getSupportedPictureSizes().get(0).height);
        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        cameraParameters.setRotation(90);
        mCamera.setParameters(cameraParameters);
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.removeAllViews();
        preview.addView(mPreview);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(boolean setBackCamera){
        Camera c = null;
        try {
            c = Camera.open(setBackCamera ? 0 : 1); // attempt to get a Camera instance
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return c; //Returns null if camera is unavailable
    }
}
