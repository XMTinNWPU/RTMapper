package com.dji.ui.sample.handlers;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//import dji.common.camera.CameraSettingsDef;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

import com.dji.ui.sample.Logger;
//import com.dji.ui.sample.ConnectionActivity;
import com.dji.ui.sample.MainActivity;
import com.dji.ui.sample.media.DJIVideoStreamDecoder;

/**
 * Created by dji on 16.01.2017.
 */

class VideoJpgWriterStrategy extends HandleStrategy
        implements DJIVideoStreamDecoder.IYuvDataListener{
    FlightController flightController;
    //BaseProduct mbaseproduct;
    static boolean isRecording = false;
    VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    public VideoJpgWriterStrategy(final Socket client){
        super(client);
        BaseProduct mProduct;
        mProduct = MainActivity.getProductInstance();
        if (mProduct != null) {
            //flightController=((Aircraft)mProduct).getFlightController();
            Logger.log("productget");
            DJIVideoStreamDecoder.getInstance().setYuvDataListener(this);
            //camera.setDJICameraReceivedVideoDataCallback(


            mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {
                @Override
                public void onReceive(byte[] bytes, int length) {
                    //Logger.log("Available bytes: " + bytes.length);
                    Logger.log("Size: " + length);
                    //Logger.log(Arrays.toString(bytes));
                    DJIVideoStreamDecoder.getInstance().parse(bytes, length);

                }
            };
        }
    }

    @Override
    protected void readMessage(InputStream istream) throws IOException {
        throw new UnsupportedOperationException("Unexpected input");
    }

    @Override
    protected void writeMessage(OutputStream ostream) throws IOException{


    }

    @Override
    protected boolean isNeedRead() { return false; }

    @Override
    protected boolean isNeedWrite() {
        return false;
    }

    @Override
    public void onYuvDataReceived(byte[] yuvFrame, int width, int height) {
        int frameIndex = DJIVideoStreamDecoder.getInstance().frameIndex;

        if (frameIndex % 15 == 0) { //need for preventing out of memory error
            try {
                convertYuvFormatToNv21(yuvFrame, width, height);
                YuvImage yuvImage = new YuvImage(yuvFrame, ImageFormat.NV21, width, height, null);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out);
                byte[] jpeg = out.toByteArray();

                Logger.log("Send frame with index "+frameIndex);
                Logger.log("Size: " + jpeg.length);

                DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                dos.writeInt(frameIndex);
                dos.writeInt(jpeg.length);
//                writeInt(client.getOutputStream(), frameIndex);
//                writeInt(client.getOutputStream(), jpeg.length);
                client.getOutputStream().write(jpeg);
            } catch (RuntimeException e) {
                Logger.log("JpgWriter: " + e.getMessage() + " w: " + width + " h: " + height
                        + " a: " + yuvFrame.length);
            } catch (IOException e) {
                Logger.log("JpgWriter: " + e.getMessage());
                try {
                    client.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void convertYuvFormatToNv21(byte[] yuvFrame, int width, int height) {
        byte[] y = new byte[width * height];
        byte[] u = new byte[width * height / 4];
        byte[] v = new byte[width * height / 4];
        byte[] nu = new byte[width * height / 4];
        byte[] nv = new byte[width * height / 4];
        System.arraycopy(yuvFrame, 0, y, 0, y.length);
        for (int i = 0; i < u.length; i++) {
            v[i] = yuvFrame[y.length + 2 * i];
            u[i] = yuvFrame[y.length + 2 * i + 1];
        }
        int uvWidth = width / 2;
        int uvHeight = height / 2;
        for (int j = 0; j < uvWidth / 2; j++) {
            for (int i = 0; i < uvHeight / 2; i++) {
                byte uSample1 = u[i * uvWidth + j];
                byte uSample2 = u[i * uvWidth + j + uvWidth / 2];
                byte vSample1 = v[(i + uvHeight / 2) * uvWidth + j];
                byte vSample2 = v[(i + uvHeight / 2) * uvWidth + j + uvWidth / 2];
                nu[2 * (i * uvWidth + j)] = uSample1;
                nu[2 * (i * uvWidth + j) + 1] = uSample1;
                nu[2 * (i * uvWidth + j) + uvWidth] = uSample2;
                nu[2 * (i * uvWidth + j) + 1 + uvWidth] = uSample2;
                nv[2 * (i * uvWidth + j)] = vSample1;
                nv[2 * (i * uvWidth + j) + 1] = vSample1;
                nv[2 * (i * uvWidth + j) + uvWidth] = vSample2;
                nv[2 * (i * uvWidth + j) + 1 + uvWidth] = vSample2;
            }
        }
        //nv21test
        System.arraycopy(y, 0, yuvFrame, 0, y.length);
        for (int i = 0; i < u.length; i++) {
            yuvFrame[y.length + (i * 2)] = nv[i];
            yuvFrame[y.length + (i * 2) + 1] = nu[i];
        }
    }

    @Override
    protected void initialize() {
        DJIVideoStreamDecoder.getInstance().setYuvDataListener(this);
        //startRecord();
    }

    @Override
    protected void interrupt() {
        //stopRecord();
        DJIVideoStreamDecoder.getInstance().setYuvDataListener(null);
    }
/*
    private void switchCameraMode(DJICameraSettingsDef.CameraMode cameraMode){

        DJICamera camera = PcApiApplication.getCameraInstance();
        if (camera != null) {
            camera.setCameraMode(cameraMode, new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError error) {

                    if (error == null) {
                        //Logger.showToast("Switch Camera Mode Succeeded");
                        Logger.log("Switch Camera Mode Succeeded");
                    } else {
                        //Logger.showToast(error.getDescription());
                        Logger.log(error.getDescription());
                    }
                }
            });
        }

    }

    // Method for starting recording
    private void startRecord(){
        switchCameraMode(DJICameraSettingsDef.CameraMode.RecordVideo);

        final DJICamera camera = PcApiApplication.getCameraInstance();
        if (camera != null) {
            camera.startRecordVideo(new DJICommonCallbacks.DJICompletionCallback(){
                @Override
                public void onResult(DJIError error)
                {
                    if (error == null) {
                        if (!isRecording) {
                            //Logger.showToast("Record video: success");
                            Logger.log("Record video: success");

                            DJIVideoStreamDecoder.getInstance().frameIndex = 0;
                            isRecording = true;
                        }
                    }else {
                        //Logger.showToast(error.getDescription());
                        Logger.log(error.getDescription());
                    }
                }
            }); // Execute the startRecordVideo API
        }
    }

    // Method for stopping recording
    private void stopRecord(){

        DJICamera camera = PcApiApplication.getCameraInstance();
        if (camera != null) {
            camera.stopRecordVideo(new DJICommonCallbacks.DJICompletionCallback(){

                @Override
                public void onResult(DJIError error)
                {
                    if(error == null) {
                        if (isRecording) {
                            //Logger.showToast("Stop recording: success");
                            Logger.log("Stop recording: success");

                            isRecording = false;
                        }
                    }else {
                        //Logger.showToast(error.getDescription());
                        Logger.log(error.getDescription());
                    }
                }
            }); // Execute the stopRecordVideo API
        }

    }*/
}
