package com.airesapps.client;

import android.os.Build;

import java.io.File;
import java.io.IOException;
import okhttp3.*;
import java.io.*;
import java.util.Base64;
public class PathologyPredictionClient {

    private String response;

    public static String predict(File audioFile1, File audioFile2, File audioFile3) {

        String url = "http://10.0.2.2:5000/predict"; // altere para o endereço correto da sua API
        MediaType mediaType = MediaType.parse("application/octet-stream");
        OkHttpClient client = new OkHttpClient();

        String audio1Base64 = fileToBase64(audioFile1);
        String audio2Base64 = fileToBase64(audioFile2);
        String audio3Base64 = fileToBase64(audioFile3);
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
