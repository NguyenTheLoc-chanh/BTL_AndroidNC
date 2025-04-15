package com.example.btl_androidnc.Booking;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import com.example.btl_androidnc.R;

public class CameraCaptureActivity extends AppCompatActivity implements SensorEventListener {

    private PreviewView previewView;
    private ImageView imgPreview;
    private Button btnCapture, btnConfirm, btnRetry;
    private LinearLayout btnContainer;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private Uri photoUri;
    private CameraXHelper cameraXHelper;

    private static final float SHAKE_THRESHOLD = 15.0f;
    private static final int SHAKE_COOLDOWN_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);

        previewView = findViewById(R.id.previewView);
        imgPreview = findViewById(R.id.imgPreview);
        btnCapture = findViewById(R.id.btnCapture);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnRetry = findViewById(R.id.btnRetry);
        btnContainer = findViewById(R.id.btnContainer);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Khởi tạo camera helper
        cameraXHelper = new CameraXHelper(this, previewView, this, uri -> {
            photoUri = uri;

            runOnUiThread(() -> {
                // Hiển thị ảnh đã chụp
                imgPreview.setImageURI(uri);
                imgPreview.setVisibility(View.VISIBLE);
                previewView.setVisibility(View.GONE);

                // Chuyển đổi hiển thị nút
                btnCapture.setVisibility(View.GONE);
                btnContainer.setVisibility(View.VISIBLE);

                // Đảm bảo các nút nằm trên cùng
                btnContainer.bringToFront();
            });
        });
        cameraXHelper.startCamera();

        btnCapture.setOnClickListener(v -> cameraXHelper.takePhoto());

        btnConfirm.setOnClickListener(v -> {
            if (photoUri != null) {
                Toast.makeText(this, "Đã xác nhận ảnh", Toast.LENGTH_SHORT).show();
                if (photoUri != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("photoUri", photoUri.toString());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        btnRetry.setOnClickListener(v -> {
            runOnUiThread(() -> {
                // Quay lại chế độ chụp ảnh
                imgPreview.setVisibility(View.GONE);
                previewView.setVisibility(View.VISIBLE);
                btnContainer.setVisibility(View.GONE);
                btnCapture.setVisibility(View.VISIBLE);

                // Đảm bảo nút chụp nằm trên cùng
                btnCapture.bringToFront();
            });
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
        if (acceleration > SHAKE_THRESHOLD) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShakeTime > SHAKE_COOLDOWN_MS) {
                lastShakeTime = currentTime;
                Toast.makeText(this, "Thiết bị đang bị rung! Vui lòng giữ ổn định trước khi chụp.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
