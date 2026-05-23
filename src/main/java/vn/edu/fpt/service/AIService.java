package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public String getFinancialAdvice(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        // Cấu trúc Request Body theo chuẩn của Gemini API
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        String urlWithKey = apiUrl + "?key=" + apiKey;

        try {
            Map<String, Object> response = restTemplate.postForObject(urlWithKey, requestBody, Map.class);

            // Bóc tách dữ liệu từ JSON trả về của Gemini
            List candidates = (List) response.get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map part = (Map) parts.get(0);

            return (String) part.get("text");
        } catch (Exception e) {
            return "AI đang bận, ông thử lại sau nhé! Lỗi: " + e.getMessage();
        }
    }
}