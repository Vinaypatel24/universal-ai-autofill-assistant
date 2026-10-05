package com.example.smartautofiller.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smartautofiller.R;
import com.example.smartautofiller.model.SectionField;
import com.example.smartautofiller.ocr.DocumentParser;
import com.example.smartautofiller.ocr.ScannedData;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Camera scanning activity utilizing CameraX and Google ML Kit Text Recognition
 * to scan ID cards, driving licenses, and academic marksheets into structured profiles.
 */
public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 1001;

    private PreviewView previewView;
    private TextView tvScanStatus;
    private ProgressBar progressScanning;
    private ImageButton btnCapture;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.camera_preview);
        tvScanStatus = findViewById(R.id.tv_scan_status);
        progressScanning = findViewById(R.id.progress_scanning);
        btnCapture = findViewById(R.id.btn_capture);
        ImageButton btnBack = findViewById(R.id.btn_camera_back);

        btnBack.setOnClickListener(v -> finish());
        btnCapture.setOnClickListener(v -> captureAndScan());

        cameraExecutor = Executors.newSingleThreadExecutor();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to scan documents", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureAndScan() {
        if (imageCapture == null) return;

        progressScanning.setVisibility(View.VISIBLE);
        btnCapture.setEnabled(false);
        tvScanStatus.setText("Recognizing text via ML Kit...");

        imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                processImageWithMLKit(imageProxy);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> {
                    progressScanning.setVisibility(View.GONE);
                    btnCapture.setEnabled(true);
                    tvScanStatus.setText("Capture failed: " + exception.getMessage());
                });
            }
        });
    }

    @androidx.annotation.OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void processImageWithMLKit(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    imageProxy.close();
                    ScannedData data = DocumentParser.parse(visionText.getText());
                    runOnUiThread(() -> returnScanResult(data));
                })
                .addOnFailureListener(e -> {
                    imageProxy.close();
                    runOnUiThread(() -> {
                        progressScanning.setVisibility(View.GONE);
                        btnCapture.setEnabled(true);
                        tvScanStatus.setText("OCR recognition failed: " + e.getMessage());
                    });
                });
    }

    private void returnScanResult(ScannedData data) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("detected_name", data.getName());
        resultIntent.putExtra("detected_email", data.getEmail());
        resultIntent.putExtra("detected_phone", data.getPhone());
        resultIntent.putExtra("detected_pan", data.getPanNumber());
        resultIntent.putExtra("detected_aadhaar", data.getAadhaarNumber());
        resultIntent.putExtra("detected_dob", data.getDob());
        resultIntent.putExtra("detected_address", data.getAddress());
        resultIntent.putExtra("detected_dl", data.getDlNumber());
        resultIntent.putExtra("detected_father", data.getFatherName());
        resultIntent.putExtra("doc_type", data.getDocType());

        List<String> labels = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (SectionField field : data.getSectionFields()) {
            labels.add(field.getLabel());
            values.add(field.getValue());
        }

        resultIntent.putExtra("section_labels", labels.toArray(new String[0]));
        resultIntent.putExtra("section_values", values.toArray(new String[0]));

        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "Scanned: " + data.getDocType(), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }
}
