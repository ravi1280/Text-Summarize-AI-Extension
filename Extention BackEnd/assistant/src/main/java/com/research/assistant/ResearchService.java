package com.research.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;

@Service
public class ResearchService {
    @Value("${gemini.api.url}")
    private String GeminiApiUrl;
    @Value("${gemini.api.key}")
    private String GeminiApiKey;

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public ResearchService(ObjectMapper objectMapper, WebClient.Builder webClientBuilder) {
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.build();
    }
    public String processContent(ResearchRequest researchRequest) {

        //Build The Prompt
        String prompt = buildPrompt(researchRequest);
        //Query the  AI Model API
//        Map<String, Object>  requestBody = Map.of(
//                "content", new Object[]{
//                        Map.of("parts", new Object[]{
//                                Map.of("text", prompt)
//                        })
//                }
//        );
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt),
                        })
                }
        );

        String response = webClient.post().uri(GeminiApiUrl + GeminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        //Parse the Response

        //Return the Response
        return extractTextFromResponse(response);
    }

    private String extractTextFromResponse(String response) {
        try {
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            System.out.println(geminiResponse);
            if(geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()){
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if(firstCandidate.getContent() != null && firstCandidate.getContent().getParts() != null && !firstCandidate.getContent().getParts().isEmpty()){
                    return firstCandidate.getContent().getParts().get(0).getText();
                } else {
                    return "No content parts found in the response.";
                }
            } else {
                return "No candidates found in the response.";

            }
        } catch (Exception e) {
            return "Error Parsing: " + e.getMessage();
        }
    }

    private String buildPrompt(ResearchRequest researchRequest) {

        StringBuilder prompt = new StringBuilder();
            switch (researchRequest.getOperation()){
                case "summarize":
                    prompt.append("Provide a concise summary of the following text in a few sentences:\n\n");
                    break;
                case "suggest":
                    prompt.append("Based on the following contend: suggest related topics and further reading. Format the response with clear heading and bullet points:\n\n");
                    break;
                default:
                   throw new IllegalArgumentException("Unknown operation: " + researchRequest.getOperation());
            }
            prompt.append(researchRequest.getContent());
        return prompt.toString();
    }
}
