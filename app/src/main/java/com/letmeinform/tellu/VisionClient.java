package com.letmeinform.tellu;

import android.graphics.Bitmap;
import android.util.Log;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class VisionClient {

    private static final String CLOUD_VISION_API_KEY = "AIzaSyBA6LQe2JQKLLefdOR-RRoMwvDnuz0pOW8";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MAX_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private final UIHandler<String> uiHandler;

    public VisionClient(UIHandler<String> uiHandler) {
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

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder();

        for (AnnotateImageResponse res : response.getResponses()) {
            WebDetection webDetection = res.getWebDetection();
            for (WebLabel label : webDetection.getBestGuessLabels()) {
                message.append(label.getLabel());
            }
        }


        return message.toString();
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
                String productName = convertResponseToString(response);
                uiHandler.updateUI(productName);

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
