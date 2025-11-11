package com.bajajfinserv.hiring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private RestTemplate restTemplate;

    private final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private final String TEST_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Starting Bajaj Finserv Health Hiring Solution ===");
        
        // Step 1: Generate Webhook
        System.out.println("Step 1: Generating webhook...");
        WebhookResponse webhookResponse = generateWebhook();
        
        if (webhookResponse != null) {
            System.out.println("✓ Webhook generated successfully");
            System.out.println("Webhook URL: " + webhookResponse.getWebhook());
            System.out.println("Access Token: " + webhookResponse.getAccessToken());
            
            // Step 2: Solve SQL Problem
            System.out.println("\nStep 2: Solving SQL problem...");
            String finalQuery = solveSQLProblem();
            System.out.println("Final SQL Query: " + finalQuery);
            
            // Step 3: Submit Solution
            System.out.println("\nStep 3: Submitting solution...");
            submitSolution(webhookResponse.getWebhook(), webhookResponse.getAccessToken(), finalQuery);
        } else {
            System.out.println("✗ Failed to generate webhook");
        }
        
        System.out.println("=== Process Completed ===");
    }

    private WebhookResponse generateWebhook() {
        try {
            // Create request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", "John Doe");
            requestBody.put("regNo", "REG12347");
            requestBody.put("email", "john@example.com");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<WebhookResponse> response = restTemplate.exchange(
                GENERATE_WEBHOOK_URL,
                HttpMethod.POST,
                requestEntity,
                WebhookResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.out.println("Error generating webhook: " + e.getMessage());
        }
        return null;
    }

    private String solveSQLProblem() {
        // SQL Query for Question 2 (Even registration number)
        String sqlQuery = "WITH EmployeeAges AS (" +
            "    SELECT " +
            "        e.EMP_ID, " +
            "        e.FIRST_NAME, " +
            "        e.LAST_NAME, " +
            "        d.DEPARTMENT_NAME, " +
            "        e.DOB, " +
            "        DATEDIFF(YEAR, e.DOB, GETDATE()) AS AGE " +
            "    FROM EMPLOYEE e " +
            "    JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID" +
            "), " +
            "YoungerCounts AS (" +
            "    SELECT " +
            "        ea1.EMP_ID, " +
            "        COUNT(ea2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
            "    FROM EmployeeAges ea1 " +
            "    LEFT JOIN EmployeeAges ea2 ON " +
            "        ea1.DEPARTMENT_NAME = ea2.DEPARTMENT_NAME " +
            "        AND ea2.AGE < ea1.AGE " +
            "    GROUP BY ea1.EMP_ID" +
            ") " +
            "SELECT " +
            "    ea.EMP_ID, " +
            "    ea.FIRST_NAME, " +
            "    ea.LAST_NAME, " +
            "    ea.DEPARTMENT_NAME, " +
            "    COALESCE(yc.YOUNGER_EMPLOYEES_COUNT, 0) AS YOUNGER_EMPLOYEES_COUNT " +
            "FROM EmployeeAges ea " +
            "LEFT JOIN YoungerCounts yc ON ea.EMP_ID = yc.EMP_ID " +
            "ORDER BY ea.EMP_ID DESC";

        return sqlQuery;
    }

    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        try {
            // Create request body
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("finalQuery", finalQuery);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", accessToken);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                TEST_WEBHOOK_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✓ Solution submitted successfully!");
                System.out.println("Response: " + response.getBody());
            } else {
                System.out.println("✗ Failed to submit solution. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Error submitting solution: " + e.getMessage());
        }
    }
}