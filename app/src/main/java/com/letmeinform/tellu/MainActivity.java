package com.letmeinform.tellu;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.camera.camera2.internal.annotation.CameraExecutor;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.common.util.concurrent.ListenableFuture;
import com.letmeinform.tellu.databinding.ActivityMainBinding;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
//    private static final String CLOUD_VISION_API_KEY = "AIzaSyBA6LQe2JQKLLefdOR-RRoMwvDnuz0pOW8";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(), new GsonFactory(), null);
//        visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer(CLOUD_VISION_API_KEY));
//
//        Vision vision = visionBuilder.build();
    }
}