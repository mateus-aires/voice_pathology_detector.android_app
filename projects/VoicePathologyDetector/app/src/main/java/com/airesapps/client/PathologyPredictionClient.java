package com.airesapps.client;

import static com.airesapps.util.Constants.CONNECTION_ERROR_MESSAGE;

import android.os.Build;

import com.airesapps.dto.PredictionDTO;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import okhttp3.*;
import java.io.*;
import java.util.Base64;
public class PathologyPredictionClient {

    private static final String PROD_URL = "https://flask-production-a030.up.railway.app/predict";
    private static final String LOCAL_URL = "http://10.0.2.2:5000/predict";

    public static PredictionDTO predict(File audioFile1, File audioFile2, File audioFile3) {

        String url = LOCAL_URL;
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("audio1", audioFile1.getName(), RequestBody.create(MediaType.parse("audio/*m4a"), audioFile1))
                .addFormDataPart("audio2", audioFile2.getName(), RequestBody.create(MediaType.parse("audio/*m4a"), audioFile2))
                .addFormDataPart("audio3", audioFile3.getName(), RequestBody.create(MediaType.parse("audio/*m4a"), audioFile3))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        PredictionDTO predictionDTO = new PredictionDTO();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            Gson gson = new Gson();
            predictionDTO = gson.fromJson(responseBody, PredictionDTO.class);

            return predictionDTO;

        } catch (IOException e) {
            e.printStackTrace();
            predictionDTO.setSuccessful(false);
            predictionDTO.setErrorCause(CONNECTION_ERROR_MESSAGE);
            return predictionDTO;
        }
    }

}
