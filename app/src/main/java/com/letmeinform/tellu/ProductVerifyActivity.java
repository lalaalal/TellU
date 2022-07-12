package com.letmeinform.tellu;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProductVerifyActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView productNameTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_verify);

        imageView = findViewById(R.id.captured_image);
        productNameTv = findViewById(R.id.product_name);

        Intent intent = getIntent();
        String imageName = intent.getExtras().getString("file_name");

        init(imageName);
    }

    private void init(String imageName) {
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH
        };

        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

        String selection = MediaStore.Images.Media.DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{imageName};

        try (Cursor cursor = getContentResolver().query(collection, projection, selection, selectionArgs, null)) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

            if (!cursor.moveToNext())
                return;
            long id = cursor.getLong(idColumn);
            String displayName = cursor.getString(nameColumn);

            if (displayName.equals(imageName)) {
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                setImageView(imageUri);
            }
        }
    }

    private void setImageView(Uri imageUri) {

        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(is);

            imageView.setImageBitmap(bitmap);
            imageView.setMaxHeight(bitmap.getHeight());
            imageView.setMaxWidth(bitmap.getWidth());

            VisionClient visionClient = new VisionClient(new ProductVerityUIHandler(this));
            visionClient.searchProductName(bitmap);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public class ProductVerityUIHandler extends UIHandler<String> {

        public ProductVerityUIHandler(ProductVerifyActivity activity) {
            super(activity);
        }

        @Override
        protected void runOnUIThread(String data) {
            if (productNameTv != null)
                productNameTv.setText(data);
        }
    }
}