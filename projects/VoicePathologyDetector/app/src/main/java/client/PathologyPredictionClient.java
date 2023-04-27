package client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
public class PathologyPredictionClient {

    public static String predict(File audioFile1, File audioFile2, File audioFile3) {
        try {
            String url = "http://localhost:5000/predict"; // altere para o endereço correto da sua API
            HttpClient client = HttpClientBuilder.create()
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();

            // cria o objeto de requisição HTTP POST
            HttpPost post = new HttpPost(url);

            // cria o objeto de entidade HTTP com os arquivos de áudio
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("audio1", audioFile1, ContentType.APPLICATION_OCTET_STREAM, audioFile1.getName());
            builder.addBinaryBody("audio2", audioFile2, ContentType.APPLICATION_OCTET_STREAM, audioFile2.getName());
            builder.addBinaryBody("audio3", audioFile3, ContentType.APPLICATION_OCTET_STREAM, audioFile3.getName());
            HttpEntity entity = builder.build();

            // adiciona a entidade HTTP à requisição POST
            post.setEntity(entity);

            // executa a requisição e obtém a resposta
            HttpResponse response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();
            String result = EntityUtils.toString(responseEntity);

            return result;
        } catch (IOException e) {
            return "error";
        }

    }
}
