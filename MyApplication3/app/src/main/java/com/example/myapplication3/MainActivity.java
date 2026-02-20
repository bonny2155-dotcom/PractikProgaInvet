package com.example.myapplication3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private LinearLayout scannerScreen;
    private LinearLayout errorScreen;
    private TextView errorMessage;
    private PreviewView previewView;

    private ExecutorService cameraExecutor;
    private boolean scanningPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataRepository.init(this);

        scannerScreen = findViewById(R.id.scannerScreen);
        errorScreen   = findViewById(R.id.errorScreen);
        errorMessage  = findViewById(R.id.errorMessage);
        previewView   = findViewById(R.id.previewView);

        cameraExecutor = Executors.newSingleThreadExecutor();

        showScreen("scanner");

        findViewById(R.id.navScanner).setOnClickListener(v -> {
            scanningPaused = false;
            showScreen("scanner");
        });
        findViewById(R.id.navShelves).setOnClickListener(v -> {
            startActivity(new Intent(this, PolkiActivity.class));
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.manualButton).setOnClickListener(v -> {
            EditText input = findViewById(R.id.barcodeInput);
            String barcode = input.getText().toString().trim();
            if (!barcode.isEmpty()) {
                onScanResult(barcode);
            }
        });

        findViewById(R.id.retryButton).setOnClickListener(v -> {
            scanningPaused = false;
            showScreen("scanner");
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                bindCamera(provider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera(ProcessCameraProvider provider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        analysis.setAnalyzer(cameraExecutor, imageProxy -> {
            if (scanningPaused) { imageProxy.close(); return; }
            scanBarcode(imageProxy);
        });

        CameraSelector selector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        provider.unbindAll();
        provider.bindToLifecycle(this, selector, preview, analysis);
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void scanBarcode(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) { imageProxy.close(); return; }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());

        BarcodeScannerOptions opts = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_QR_CODE, Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39, Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E)
                .build();

        BarcodeScanning.getClient(opts)
                .process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode b : barcodes) {
                        String value = b.getRawValue();
                        if (value != null && !value.isEmpty()) {
                            scanningPaused = true;
                            runOnUiThread(() -> onScanResult(value));
                            break;
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void onScanResult(String barcode) {
        DataRepository.Item item = DataRepository.findItemByBarcode(barcode);
        if (item != null) {
            Intent intent = new Intent(this, InfoOPredmeteActivity.class);
            intent.putExtra("barcode", barcode);
            startActivity(intent);
        } else {
            errorMessage.setText("Объект с кодом «" + barcode + "» не найден");
            showScreen("error");
        }
    }

    private void showScreen(String name) {
        scannerScreen.setVisibility(View.GONE);
        errorScreen.setVisibility(View.GONE);
        if ("scanner".equals(name)) scannerScreen.setVisibility(View.VISIBLE);
        else if ("error".equals(name)) errorScreen.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int code,
                                           @NonNull String[] permissions,
                                           @NonNull int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == REQUEST_CAMERA_PERMISSION) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                ((TextView) findViewById(R.id.cameraHint))
                        .setText("Камера недоступна — используйте ввод вручную");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanningPaused = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}