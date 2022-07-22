package com.letmeinform.tellu;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VisionClient {

    private static final String CLOUD_VISION_API_KEY = "AIzaSyBA6LQe2JQKLLefdOR-RRoMwvDnuz0pOW8";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MAX_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private final UIHandler<Product> uiHandler;

    public VisionClient(UIHandler<Product> uiHandler) {
        this.uiHandler = uiHandler;
    }

    public void searchProductName(final Bitmap bitmap) {
        Thread thread = new Thread(new BackgroundTask(bitmap));
        thread.start();
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            Image base64EncodedImage = new Image();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature webDetection = new Feature();
                webDetection.setType("WEB_DETECTION");
                webDetection.setMaxResults(MAX_RESULTS);
                add(webDetection);

                Feature textDetection = new Feature();
                textDetection.setType("TEXT_DETECTION");
                textDetection.setMaxResults(MAX_RESULTS);
                add(textDetection);
            }});

            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = VisionClient.MAX_DIMENSION;
        int resizedHeight = VisionClient.MAX_DIMENSION;

        if (originalHeight > originalWidth) {
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        }

        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static Product convertResponseToProduct(BatchAnnotateImagesResponse response) throws ParseException {
        String productName = "";
        Date expirationDate = null;

        for (AnnotateImageResponse res : response.getResponses()) {
            WebDetection webDetection = res.getWebDetection();
            if (webDetection == null)
                continue;
            for (WebLabel label : webDetection.getBestGuessLabels()) {
                productName = label.getLabel();
            }

            if (res.getTextAnnotations() == null)
                continue;
            Pattern pattern = Pattern.compile("20\\d\\d\\.[0-1]\\d\\.[0-3]\\d");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            for (EntityAnnotation label : res.getTextAnnotations()) {
                Matcher matcher = pattern.matcher(label.getDescription());
                if (matcher.find()) {
                    expirationDate = dateFormat.parse(matcher.group());
                }
            }
        }

        return new Product(productName, expirationDate);
    }

    private class BackgroundTask implements Runnable {
        private final Bitmap bitmap;

        public BackgroundTask(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            try {
                Bitmap scaledBitmap = scaleBitmapDown(bitmap);
                Vision.Images.Annotate request = prepareAnnotationRequest(scaledBitmap);

                BatchAnnotateImagesResponse response = request.execute();
                Product product = convertResponseToProduct(response);
                uiHandler.updateUI(product);

            } catch (IOException exception) {
                exception.printStackTrace();
            } catch (ParseException e) {
                Toast.makeText(uiHandler.getActivity(), "Failed to parse date", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
