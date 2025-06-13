package com.exemplo.services; // ou com.exemplo.services

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServiceCadastroLogin {

    private static final String SUPABASE_URL = "https://aeogsdzptuphlctfiwbo.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFlb2dzZHpwdHVwaGxjdGZpd2JvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5ODkyNTAsImV4cCI6MjA2NDU2NTI1MH0.E3MaMo196rv1jwTNRITOP6p4Xt4RTfUTaULH1cDwWIk";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean cadastrarUsuario(String email, String senha) throws Exception {
        String authUrl = SUPABASE_URL + "/auth/v1/signup";
        String json = new JSONObject()
                .put("email", email)
                .put("password", senha)
                .toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    public boolean autenticarUsuario(String email, String senha) throws Exception {
        String authUrl = SUPABASE_URL + "/auth/v1/token?grant_type=password";
        String json = new JSONObject()
                .put("email", email)
                .put("password", senha)
                .toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("Autenticado com sucesso via Supabase Auth!");
            return true;
        } else {
            System.err.println("Falha na autenticação: " + response.body());
            return false;
        }
    }


    public boolean enviarEmailRedefinicaoSenha(String email, String redirectTo) throws Exception {
        // A URL correta inclui o redirectTo como query param
        String recoverUrl = SUPABASE_URL + "/auth/v1/recover?redirect_to=" + URI.create(redirectTo);

        // O corpo da requisição deve conter apenas o email
        String json = new JSONObject()
                .put("email", email)
                .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(recoverUrl))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("Solicitação de redefinição de senha enviada com sucesso para: " + email);
            return true;
        } else {
            System.err.println("Falha ao solicitar redefinição de senha: " + response.statusCode() + " - " + response.body());
            return false;
        }
    }

}