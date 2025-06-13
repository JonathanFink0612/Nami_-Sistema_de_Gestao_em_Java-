package com.exemplo.services;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class SupabaseService {

    // ####################################################################
    // ##      PARTE MAIS IMPORTANTE: SUBSTITUA OS VALORES ABAIXO        ##
    // ####################################################################
    private static final String SUPABASE_URL = "https://aeogsdzptuphlctfiwbo.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFlb2dzZHpwdHVwaGxjdGZpd2JvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5ODkyNTAsImV4cCI6MjA2NDU2NTI1MH0.E3MaMo196rv1jwTNRITOP6p4Xt4RTfUTaULH1cDwWIk";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String pkceCodeVerifier; // Armazena o código secreto para a segunda etapa do login com Google

    /**
     * Cadastra um novo usuário usando o sistema de autenticação do Supabase.
     */
    public boolean cadastrarUsuario(String email, String senha) throws Exception {
        String authUrl = SUPABASE_URL + "/auth/v1/signup";
        String json = new JSONObject().put("email", email).put("password", senha).toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    /**
     * Autentica um usuário com e-mail e senha via Supabase Auth.
     */
    public boolean autenticarUsuario(String email, String senha) throws Exception {
        String authUrl = SUPABASE_URL + "/auth/v1/token?grant_type=password";
        String json = new JSONObject().put("email", email).put("password", senha).toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }

    /**
     * Gera e retorna a URL completa para o login com Google, incluindo os parâmetros de segurança PKCE.
     */
    public String getGoogleSignInUrl() throws NoSuchAlgorithmException {
        this.pkceCodeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(pkceCodeVerifier);
        String callbackUrl = "http://localhost:9999/callback";

        return String.format("%s/auth/v1/authorize?provider=google&redirect_to=%s&code_challenge=%s&code_challenge_method=S256",
                SUPABASE_URL, callbackUrl, codeChallenge);
    }

    /**
     * Troca o código de autorização pela sessão final, enviando também o verificador PKCE.
     */
    public CompletableFuture<Boolean> exchangeCodeForSession(String authCode) {
        String url = String.format("%s/auth/v1/token?grant_type=pkce", SUPABASE_URL);
        String json = new JSONObject().put("auth_code", authCode).put("code_verifier", this.pkceCodeVerifier).toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200);
    }

    // --- Métodos Auxiliares de Segurança PKCE ---

    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifierBytes = new byte[32];
        secureRandom.nextBytes(codeVerifierBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifierBytes);
    }

    private String generateCodeChallenge(String verifier) throws NoSuchAlgorithmException {
        byte[] bytes = verifier.getBytes(StandardCharsets.UTF_8);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    /**
     * Inicia o fluxo de redefinição de senha para um determinado e-mail.
     */
    public boolean enviarEmailRedefinicaoSenha(String email) throws Exception {
        String authUrl = SUPABASE_URL + "/auth/v1/recover";

        // A API de recuperação espera o e-mail em um corpo JSON.
        String json = new JSONObject()
                .put("email", email)
                .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Por segurança, o Supabase sempre retorna 200 OK, mesmo que o e-mail não exista,
        // para evitar que pessoas descubram quais e-mails estão cadastrados.
        return response.statusCode() == 200;
    }
    /**
     * Atualiza a senha do usuário usando um token de acesso válido.
     */
    public boolean atualizarSenhaComToken(String accessToken, String novaSenha) throws Exception {
        // Este é o endpoint para atualizar os dados do usuário autenticado
        String authUrl = SUPABASE_URL + "/auth/v1/user";

        String json = new JSONObject()
                .put("password", novaSenha)
                .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                // A autorização é feita com o token de acesso no header
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)) // Usa o método PUT para atualizar
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.statusCode() == 200;
    }

}
