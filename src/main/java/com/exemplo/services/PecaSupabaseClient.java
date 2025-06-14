package com.exemplo.services;

import com.exemplo.models.Peca;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PecaSupabaseClient {

    private static final String SUPABASE_URL = "https://aeogsdzptuphlctfiwbo.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFlb2dzZHpwdHVwaGxjdGZpd2JvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5ODkyNTAsImV4cCI6MjA2NDU2NTI1MH0.E3MaMo196rv1jwTNRITOP6p4Xt4RTfUTaULH1cDwWIk";


    private static final String BUCKET_NAME = "produtos";

    private final HttpClient client = HttpClient.newHttpClient();

    public List<Peca> fetchAllProdutos(String nameFilter) throws Exception {
        // A query com `select=*` já busca todas as colunas.
        String endpoint = SUPABASE_URL + "/rest/v1/peca_padrao?select=*,imagens_peca(url_imagem)";
        if (nameFilter != null && !nameFilter.trim().isEmpty()) {
            String encodedFilter = URLEncoder.encode("%" + nameFilter.trim() + "%", StandardCharsets.UTF_8);
            endpoint += "&nome_peca=ilike." + encodedFilter;
        }

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("apikey", SUPABASE_ANON_KEY).header("Authorization", "Bearer " + SUPABASE_ANON_KEY).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Falha ao buscar produtos. Resposta: " + response.body());
        }

        List<Peca> produtos = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(response.body());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);

            // CORREÇÃO: Adicionado 'codigo_de_barras' na criação do objeto Peca.
            Peca peca = new Peca(
                    obj.getInt("peca_id"),
                    obj.getString("nome_peca"),
                    obj.optString("descricao_peca", ""),
                    obj.optString("tipo_peca", null),
                    obj.optInt("poder_computacional", 0),
                    obj.optDouble("preco", 0.0),
                    obj.optString("codigo_de_barras", null) // NOVO: Campo de código de barras
            );

            JSONArray imagensArray = obj.getJSONArray("imagens_peca");
            for (int j = 0; j < imagensArray.length(); j++) {
                peca.addImageUrl(imagensArray.getJSONObject(j).getString("url_imagem"));
            }
            produtos.add(peca);
        }
        return produtos;
    }

    public int salvarPecaCompleta(Peca peca) throws Exception {
        int pecaId = inserirPecaPadrao(peca);
        if (peca.getCamposAdicionais() != null && !peca.getCamposAdicionais().isEmpty() && !"Peça Genérica".equals(peca.getTipoPeca())) {
            String nomeTabelaEspecializada = mapearTipoParaNomeTabela(peca.getTipoPeca());
            inserirPecaEspecializada(pecaId, nomeTabelaEspecializada, peca.getCamposAdicionais());
        }
        return pecaId;
    }

    private int inserirPecaPadrao(Peca peca) throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/peca_padrao";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("nome_peca", peca.getNome());
        jsonBody.put("preco", peca.getPreco());
        jsonBody.put("codigo_de_barras", peca.getCodigoDeBarras()); // NOVO: Adiciona o código de barras
        jsonBody.put("descricao_peca", peca.getDescricao());
        jsonBody.put("poder_computacional", peca.getPoderComputacional());
        Object tipoPecaParaDb = mapearTipoParaEnumDB(peca.getTipoPeca());
        jsonBody.put("tipo_peca", tipoPecaParaDb == null ? JSONObject.NULL : tipoPecaParaDb);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("apikey", SUPABASE_ANON_KEY).header("Authorization", "Bearer " + SUPABASE_ANON_KEY).header("Content-Type", "application/json").header("Prefer", "return=representation").POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString())).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) throw new Exception("Falha ao inserir na tabela 'peca_padrao'. Resposta: " + response.body());
        return new JSONArray(response.body()).getJSONObject(0).getInt("peca_id");
    }


    public void uploadImagemESalvarUrl(File imagem, int pecaId) throws Exception {
        String pathNoBucket = pecaId + "/" + System.currentTimeMillis() + "_" + imagem.getName();
        String uploadEndpoint = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + pathNoBucket;

        String contentType = Files.probeContentType(imagem.toPath());

        HttpRequest uploadRequest = HttpRequest.newBuilder().uri(URI.create(uploadEndpoint)).header("apikey", SUPABASE_ANON_KEY).header("Authorization", "Bearer " + SUPABASE_ANON_KEY).header("Content-Type", contentType).POST(HttpRequest.BodyPublishers.ofFile(imagem.toPath())).build();
        HttpResponse<String> uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());

        if (uploadResponse.statusCode() != 200) {
            throw new Exception("Falha no upload da imagem para o Storage. Resposta: " + uploadResponse.body());
        }


        String publicUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + pathNoBucket;
        inserirUrlImagemNoDB(pecaId, publicUrl);
    }

    private void inserirUrlImagemNoDB(int pecaId, String urlImagem) throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/imagens_peca";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("peca_id", pecaId);
        jsonBody.put("url_imagem", urlImagem);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("apikey", SUPABASE_ANON_KEY).header("Authorization", "Bearer " + SUPABASE_ANON_KEY).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString())).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) throw new Exception("Falha ao salvar URL da imagem no banco. Resposta: " + response.body());
    }

    public void inserirPecaEspecializada(int pecaId, String nomeTabela, Map<String, Object> camposAdicionais) throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/" + nomeTabela;
        JSONObject jsonBody = new JSONObject(camposAdicionais);
        jsonBody.put("peca_id", pecaId);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("apikey", SUPABASE_ANON_KEY).header("Authorization", "Bearer " + SUPABASE_ANON_KEY).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString())).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) throw new Exception("Falha ao inserir na tabela '" + nomeTabela + "'. Resposta: " + response.body());
    }

    private Object mapearTipoParaEnumDB(String tipoPeca) {
        switch (tipoPeca) { case "Fonte de Alimentação": return "FonteDeAlimentacao"; case "Placa Mãe": return "PlacaMae"; case "Processador": return "Processador"; case "RAM": return "Ram"; case "Placa de Vídeo": return "PlacaDeVideo"; case "Peça Genérica": return null; default: throw new IllegalArgumentException("Tipo de peça desconhecido: " + tipoPeca); }
    }

    private String mapearTipoParaNomeTabela(String tipoPeca) {
        switch (tipoPeca) { case "Fonte de Alimentação": return "fonte_de_alimentacao"; case "Placa Mãe": return "placa_mae"; case "Processador": return "processador"; case "RAM": return "ram"; case "Placa de Vídeo": return "placa_de_video"; case "Peça Genérica": return ""; default: throw new IllegalArgumentException("Tipo de peça desconhecido: " + tipoPeca); }
    }
}
