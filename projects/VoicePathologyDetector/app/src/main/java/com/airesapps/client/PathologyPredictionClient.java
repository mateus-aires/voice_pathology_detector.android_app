package com.airesapps.client;

import android.os.Build;

import com.airesapps.dto.PredictionDTO;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import okhttp3.*;
import java.io.*;
import java.util.Base64;
public class PathologyPredictionClient {

    private String response;
    private static final String PROD_URL = "https://flask-production-a030.up.railway.app/predict";
    private static final String LOCAL_URL = "http://10.0.2.2:5000/predict";

    public static String predict(File audioFile1, File audioFile2, File audioFile3) {

        String url = PROD_URL;
        MediaType mediaType = MediaType.parse("application/octet-stream");
        OkHttpClient client = new OkHttpClient();

        // cria o objeto de requisição HTTP POST
        // Constrói o corpo da requisição com os arquivos de áudio codificados em Base64
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

        try {
            // Envia a requisição e obtém a resposta
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            Gson gson = new Gson();
            PredictionDTO predictionDTO = gson.fromJson(responseBody, PredictionDTO.class);

            return responseBody;

        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    // Converte um arquivo para uma string codificada em Base64
    private static String fileToBase64(File file) {
        try {
            byte[] fileBytes = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileBytes);
            fileInputStream.close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(fileBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

}
