package com.letmeinform.tellu;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProductVerifyActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView productNameTv;
    private TextView expirationDateTv;
    private Button saveBtn;
    private Product product = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_verify);

        imageView = findViewById(R.id.captured_image);
        productNameTv = findViewById(R.id.product_name_tv);
        expirationDateTv = findViewById(R.id.expiration_date_tv);
        saveBtn = findViewById(R.id.save_btn);

        saveBtn.setOnClickListener(view -> {
            try (DBHelper dbHelper = new DBHelper(this)) {
                if (product != null)
                    dbHelper.addProduct(product);

                saveBtn.setEnabled(false);
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
            }
        });

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

    public class ProductVerityUIHandler extends UIHandler<Product> {

        public ProductVerityUIHandler(ProductVerifyActivity activity) {
            super(activity);
        }

        @Override
        protected void runOnUIThread(Product data) {
            product = data;
            if (productNameTv != null)
                productNameTv.setText(data.name);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            if (expirationDateTv != null) {
                if (data.expirationDate != null) {
                    expirationDateTv.setText(dateFormat.format(data.expirationDate));
                    saveBtn.setEnabled(true);
                } else {
                    expirationDateTv.setText(R.string.not_found);
                    Toast.makeText(ProductVerifyActivity.this, R.string.date_not_found, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}