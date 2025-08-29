package com.project.webhooksolution.service;

import com.project.webhooksolution.request.InitialRequest;
import com.project.webhooksolution.request.SolutionRequest;
import com.project.webhooksolution.response.WebhookResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookService implements CommandLineRunner {

    private final RestTemplate restTemplate;

    public WebhookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) {
        System.out.println("Application started, beginning webhook process...");

        // 1. Generate the Webhook
        WebhookResponse webhookResponse = generateWebhook();

        if (webhookResponse != null && webhookResponse.getWebhookUrl() != null && webhookResponse.getAccessToken() != null) {
            
            // 2. Solve the SQL problem based on your registration number
            String regNo = "22BCE8572"; // Your registration number
            String finalQuery = solveSqlProblem(regNo);
            System.out.println("SQL Query to be submitted: \n" + finalQuery);

            // 3. Submit the solution
            submitSolution(webhookResponse.getWebhookUrl(), webhookResponse.getAccessToken(), finalQuery);
        } else {
            System.err.println("Failed to get webhook details. Halting process.");
        }
    }

    private WebhookResponse generateWebhook() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        // Your personal details are used here
        InitialRequest requestBody = new InitialRequest("Boddu SaiGanesh Reddy", "22BCE8572", "boddusaiganesh81@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<InitialRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println("Sending request to generate webhook...");
            ResponseEntity<WebhookResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, WebhookResponse.class);
            System.out.println("Webhook generated successfully: " + response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Error generating webhook. Status: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while generating webhook: " + e.getMessage());
        }
        return null;
    }

    private String solveSqlProblem(String regNo) {
        // Extract the last two digits from the registration number
        int numberPart = Integer.parseInt(regNo.replaceAll("[^0-9]", ""));
        int lastTwoDigits = numberPart % 100;

        if (lastTwoDigits % 2 != 0) {
            // Odd -> Question 1 Final Answer
            return "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, d.DEPARTMENT_NAME AS DEPARTMENT_NAME FROM PAYMENTS p JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID WHERE EXTRACT(DAY FROM p.PAYMENT_TIME) <> 1 ORDER BY p.AMOUNT DESC LIMIT 1;";
        } else {
            // Even -> Question 2 Final Answer
            return "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, (SELECT COUNT(*) FROM EMPLOYEE e2 WHERE e2.DEPARTMENT = e1.DEPARTMENT AND e2.DOB > e1.DOB) AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e1 JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID ORDER BY e1.EMP_ID DESC;";
        }
    }

    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        SolutionRequest solutionRequest = new SolutionRequest(finalQuery);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // This is the correct way to set the header for this specific API
        headers.set("Authorization", accessToken); 

        HttpEntity<SolutionRequest> entity = new HttpEntity<>(solutionRequest, headers);

        try {
            System.out.println("Submitting solution to: " + webhookUrl);
            ResponseEntity<String> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);
            System.out.println("Solution submitted successfully. Status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
        } catch (HttpClientErrorException e) {
            System.err.println("Error submitting solution. Status: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while submitting the solution: " + e.getMessage());
        }
    }
}