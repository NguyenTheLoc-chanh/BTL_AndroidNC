package com.example.btl_androidnc;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class CameraXHelper {

    private final Context context;
    private final PreviewView previewView;
    private final LifecycleOwner lifecycleOwner;
    private final OnPhotoCapturedListener listener;
    private ImageCapture imageCapture;

    public interface OnPhotoCapturedListener {
        void onPhotoCaptured(Uri photoUri);
    }

    public CameraXHelper(Context context, PreviewView previewView, LifecycleOwner lifecycleOwner, OnPhotoCapturedListener listener) {
        this.context = context;
        this.previewView = previewView;
        this.lifecycleOwner = lifecycleOwner;
        this.listener = listener;
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Lỗi khi khởi động camera", Toast.LENGTH_SHORT).show();
            }
        }, getExecutor());
    }

    public void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(context, "Camera chưa sẵn sàng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        Log.d("CameraXHelper", "Ảnh đã lưu: " + savedUri);

                        // Chạy trên luồng giao diện
                        new Handler(Looper.getMainLooper()).post(() -> {
                            listener.onPhotoCaptured(savedUri);
                        });
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        Log.e("CameraXHelper", "Lỗi khi chụp ảnh", exception);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, "Không thể chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(context);
    }
}
