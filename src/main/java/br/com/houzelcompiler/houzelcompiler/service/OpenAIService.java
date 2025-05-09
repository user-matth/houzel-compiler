package br.com.houzelcompiler.houzelcompiler.service;

import br.com.houzelcompiler.houzelcompiler.model.EvaluationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String OPENAI_CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String correctText(String text) throws Exception {
        // Crie o prompt similar ao que foi feito na aplicação Python
        String prompt = "A partir dessa redação, existem falhas de gramática e de contexto, "
                + "me retorne a redação corrigindo essas falhas que não deveriam existir, "
                + "a redação deve estar completa e com contexto re-estabelecido:\n\n" + text;

        // Construa o payload para a API
        String payload = "{\n" +
                "  \"model\": \"gpt-4\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"Iniciar correção da redação\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\"}\n" +
                "  ]\n" +
                "}";

        JsonNode responseJson = callOpenAI(payload);
        // Extraia o conteúdo da resposta; ajuste conforme a estrutura real retornada
        return responseJson.at("/choices/0/message/content").asText();
    }

    public EvaluationResponse evaluateCompetencies(String text, String auxPrompt) throws Exception {
        // Primeiro, gerar a avaliação de competências
        String basePrompt = "Você é uma Inteligência Artificial especializada em avaliação de redações do ENEM. "
                + "Avalie o seguinte texto de acordo com as competências do ENEM, dando uma análise detalhada:\n\n"
                + text;
        if (auxPrompt != null && !auxPrompt.isEmpty()) {
            basePrompt += "\n\n" + auxPrompt;
        }
        String payloadCompetencies = "{\n" +
                "  \"model\": \"gpt-4\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"Você é um avaliador especializado em redações do ENEM.\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + basePrompt.replace("\"", "\\\"") + "\"}\n" +
                "  ]\n" +
                "}";

        JsonNode competencyJson = callOpenAI(payloadCompetencies);
        String competencyEvaluation = competencyJson.at("/choices/0/message/content").asText();

        // Segundo, calcular a nota final baseado na avaliação anterior
        String promptScore = "Com base na avaliação detalhada das competências abaixo, atribua uma nota final de 0 a 1000 "
                + "para esta redação, justificando a pontuação:\n\n" + competencyEvaluation;
        String payloadScore = "{\n" +
                "  \"model\": \"gpt-4\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"Você é um avaliador especializado em redações do ENEM.\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + promptScore.replace("\"", "\\\"") + "\"}\n" +
                "  ]\n" +
                "}";

        JsonNode scoreJson = callOpenAI(payloadScore);
        String finalScore = scoreJson.at("/choices/0/message/content").asText();

        EvaluationResponse response = new EvaluationResponse();
        response.setCompetencyEvaluation(competencyEvaluation);
        response.setFinalScore(finalScore);
        return response;
    }

    public String detectAiGenerated(String text) throws Exception {
        String prompt = "Analise o seguinte texto e determine se ele foi provavelmente escrito por uma IA ou por um humano. \n\n" + text;
        String payload = "{\n" +
                "  \"model\": \"gpt-4\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"Você é um especialista em detectar textos gerados por IA.\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\"}\n" +
                "  ]\n" +
                "}";
        JsonNode responseJson = callOpenAI(payload);
        return responseJson.at("/choices/0/message/content").asText();
    }

    private JsonNode callOpenAI(String payload) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);
        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(OPENAI_CHAT_COMPLETIONS_URL, requestEntity, String.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Erro ao chamar a API OpenAI: " + responseEntity.getBody());
        }
        return objectMapper.readTree(responseEntity.getBody());
    }
}
