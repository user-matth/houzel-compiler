package br.com.houzelcompiler.houzelcompiler.service;

import br.com.houzelcompiler.houzelcompiler.model.EvaluationResponse;
import br.com.houzelcompiler.houzelcompiler.utils.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class EvaluationService {

    private final GoogleVisionService googleVisionService;
    private final OpenAIService openAIService;

    public EvaluationService(GoogleVisionService googleVisionService, OpenAIService openAIService) {
        this.googleVisionService = googleVisionService;
        this.openAIService = openAIService;
    }

    public EvaluationResponse evaluateEssay(MultipartFile[] images, String auxPrompt, boolean aiDetection) throws Exception {
        // Cria uma lista para armazenar os textos extraídos
        List<String> extractedTexts = new ArrayList<>();
        // Pasta para armazenar arquivos temporários (você pode configurar em application.properties)
        String uploadFolder = "uploads";

        // Garante que a pasta existe
        FileUtil.createDirectory(uploadFolder);

        // Para cada imagem enviada
        List<File> tempFiles = new ArrayList<>();
        try {
            for (MultipartFile file : images) {
                if (file.isEmpty()) {
                    throw new Exception("Nenhum arquivo enviado");
                }
                // Salva o arquivo temporariamente
                File tempFile = FileUtil.convertMultipartFileToFile(file, uploadFolder);
                tempFiles.add(tempFile);

                // Chama o serviço do Google Vision para extrair texto
                String text = googleVisionService.extractHandwrittenText(tempFile);
                extractedTexts.add(text);
            }

            // Junta todos os textos extraídos
            String combinedText = String.join("\n\n", extractedTexts);

            // Corrige o texto utilizando a OpenAI
            String correctedText = openAIService.correctText(combinedText);

            // Avalia as competências e a nota final
            EvaluationResponse evaluationResponse = openAIService.evaluateCompetencies(correctedText, auxPrompt);

            // Se for solicitado detecção de IA, adiciona o resultado
            if (aiDetection) {
                String aiDetectionResult = openAIService.detectAiGenerated(correctedText);
                evaluationResponse.setAiDetection(aiDetectionResult);
            }

            return evaluationResponse;
        } finally {
            // Remove arquivos temporários
            for (File tempFile : tempFiles) {
                FileUtil.deleteFile(tempFile);
            }
        }
    }
}
