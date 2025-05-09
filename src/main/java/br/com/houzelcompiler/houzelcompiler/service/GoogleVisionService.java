package br.com.houzelcompiler.houzelcompiler.service;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleVisionService {

    public String extractHandwrittenText(File imageFile) throws Exception {
        try {
            // Lê os bytes do arquivo
            byte[] data = Files.readAllBytes(imageFile.toPath());
            ByteString imgBytes = ByteString.copyFrom(data);

            // Cria a imagem para o request
            Image image = Image.newBuilder().setContent(imgBytes).build();

            // Define o recurso de texto a ser detectado
            Feature feature = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build();

            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();
                if (!responses.isEmpty()) {
                    AnnotateImageResponse res = responses.get(0);
                    if (res.hasError()) {
                        throw new Exception("Erro na API Vision: " + res.getError().getMessage());
                    }
                    // Retorna a primeira anotação que contém o texto completo
                    if (res.getTextAnnotationsCount() > 0) {
                        return res.getTextAnnotations(0).getDescription();
                    }
                }
            }
            return "";
        } catch (IOException e) {
            throw new Exception("Erro na extração do texto: " + e.getMessage(), e);
        }
    }
}
