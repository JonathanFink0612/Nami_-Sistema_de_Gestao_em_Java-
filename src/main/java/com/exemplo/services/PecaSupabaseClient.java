package com.exemplo.services;

import com.exemplo.models.Peca;
import org.json.JSONArray;
import org.json.JSONObject;
import com.exemplo.models.UserProfile;


import java.io.File;
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
import java.util.stream.Collectors;

/**
 * Cliente de serviço para interagir com as tabelas de peças no Supabase.
 * Esta classe encapsula toda a lógica de comunicação HTTP para criar, ler,
 * atualizar e apagar dados de produtos.
 */
public class PecaSupabaseClient {

    private static final String SUPABASE_URL = "https://aeogsdzptuphlctfiwbo.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFlb2dzZHpwdHVwaGxjdGZpd2JvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5ODkyNTAsImV4cCI6MjA2NDU2NTI1MH0.E3MaMo196rv1jwTNRITOP6p4Xt4RTfUTaULH1cDwWIk";
    private static final String BUCKET_NAME = "produtos";
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Busca produtos com filtros, incluindo a opção de ver produtos inativos.
     * @param filtros Mapa de filtros.
     * @param incluirInativos Se true, busca todos; se false, busca apenas os ativos.
     */
    public List<Peca> fetchProdutosComFiltros(Map<String, Object> filtros, boolean incluirInativos) throws Exception {
        StringBuilder endpointBuilder = new StringBuilder(SUPABASE_URL + "/rest/v1/peca_padrao?select=*,imagens_peca(url_imagem)");

        if (!incluirInativos) {
            endpointBuilder.append("&ativo=eq.true");
        }

        for (Map.Entry<String, Object> filtro : filtros.entrySet()) {
            String chaveCompleta = filtro.getKey();
            Object valor = filtro.getValue();

            if (valor == null || valor.toString().trim().isEmpty()) continue;

            if (chaveCompleta.equals("termo_busca_geral")) {
                String termo = valor.toString().trim();
                List<String> orClauses = new ArrayList<>();
                orClauses.add("nome_peca.ilike.%" + termo + "%");
                orClauses.add("codigo_de_barras.eq." + termo);
                if (termo.matches("\\d+")) {
                    orClauses.add("peca_id.eq." + termo);
                }
                String orFilterValue = "(" + String.join(",", orClauses) + ")";
                endpointBuilder.append("&or=").append(URLEncoder.encode(orFilterValue, StandardCharsets.UTF_8));

            } else if (chaveCompleta.equals("tipo_peca") && valor instanceof List && !((List<?>) valor).isEmpty()) {
                List<?> listaValores = (List<?>) valor;
                String valoresFormatados = listaValores.stream()
                        .map(v -> "\"" + v.toString() + "\"")
                        .collect(Collectors.joining(","));
                String valorDoFiltro = "(" + valoresFormatados + ")";
                endpointBuilder.append("&").append(chaveCompleta).append("=in.").append(URLEncoder.encode(valorDoFiltro, StandardCharsets.UTF_8));

            } else if (chaveCompleta.contains(".")) {
                String[] partes = chaveCompleta.split("\\.");
                if (partes.length == 2) {
                    String coluna = partes[0];
                    String operador = partes[1];
                    endpointBuilder.append("&").append(coluna).append("=").append(operador).append(".").append(valor);
                }
            }
        }

        String endpoint = endpointBuilder.toString();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("apikey", SUPABASE_ANON_KEY).header("Authorization", "Bearer " + SUPABASE_ANON_KEY).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Falha ao buscar produtos com filtros. Resposta: " + response.body());
        }

        return parsePecasFromJson(response.body());
    }

    private List<Peca> parsePecasFromJson(String json) {
        List<Peca> produtos = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Peca peca = new Peca(
                    obj.getInt("peca_id"),
                    obj.getString("nome_peca"),
                    obj.optString("descricao_peca", ""),
                    obj.optString("tipo_peca", null),
                    obj.optInt("poder_computacional", 0),
                    obj.optDouble("preco", 0.0),
                    obj.optString("codigo_de_barras", null),
                    obj.optInt("quantidade", 0),
                    obj.optBoolean("ativo", true)
            );
            JSONArray imagensArray = obj.optJSONArray("imagens_peca");
            if (imagensArray != null) {
                for (int j = 0; j < imagensArray.length(); j++) {
                    peca.addImageUrl(imagensArray.getJSONObject(j).getString("url_imagem"));
                }
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
        jsonBody.put("codigo_de_barras", peca.getCodigoDeBarras());
        jsonBody.put("descricao_peca", peca.getDescricao());
        jsonBody.put("poder_computacional", peca.getPoderComputacional());
        jsonBody.put("quantidade", peca.getQuantidade());
        jsonBody.put("ativo", peca.isAtivo());

        Object tipoPecaParaDb = mapearTipoParaEnumDB(peca.getTipoPeca());
        jsonBody.put("tipo_peca", tipoPecaParaDb == null ? JSONObject.NULL : tipoPecaParaDb);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("apikey", SUPABASE_ANON_KEY).header("Authorization", "Bearer " + SUPABASE_ANON_KEY).header("Content-Type", "application/json").header("Prefer", "return=representation").POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString())).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) throw new Exception("Falha ao inserir na tabela 'peca_padrao'. Resposta: " + response.body());
        return new JSONArray(response.body()).getJSONObject(0).getInt("peca_id");
    }

    public void atualizarPeca(Peca peca) throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/peca_padrao?peca_id=eq." + peca.getPecaId();
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("nome_peca", peca.getNome());
        jsonBody.put("descricao_peca", peca.getDescricao());
        jsonBody.put("preco", peca.getPreco());
        jsonBody.put("poder_computacional", peca.getPoderComputacional());
        jsonBody.put("tipo_peca", peca.getTipoPeca());

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("apikey", SUPABASE_ANON_KEY).header("Authorization", "Bearer " + SUPABASE_ANON_KEY).header("Content-Type", "application/json").method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody.toString())).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new Exception("Falha ao atualizar a peça: " + response.body());
        }
    }

    public void desativarPeca(int pecaId) throws Exception {
        alterarStatusAtivoPeca(pecaId, false);
    }

    public void reativarPeca(int pecaId) throws Exception {
        alterarStatusAtivoPeca(pecaId, true);
    }

    private void alterarStatusAtivoPeca(int pecaId, boolean novoStatus) throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/peca_padrao?peca_id=eq." + pecaId;
        JSONObject jsonBody = new JSONObject().put("ativo", novoStatus);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("apikey", SUPABASE_ANON_KEY).header("Content-Type", "application/json").method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody.toString())).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new Exception("Falha ao alterar status da peça: " + response.body());
        }
    }

    public void adicionarEstoque(int pecaId, int quantidadeAdicional) throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/rpc/adicionar_estoque_e_reativar";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("p_peca_id", pecaId);
        jsonBody.put("p_quantidade_adicional", quantidadeAdicional);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("apikey", SUPABASE_ANON_KEY).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString())).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new Exception("Falha ao adicionar estoque via RPC: " + response.body());
        }
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
        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + pathNoBucket;
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

    public void atualizarEstoquePeca(int pecaId, int quantidadeVendida) throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/rpc/atualizar_estoque";
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("p_peca_id", pecaId);
        jsonBody.put("p_quantidade_vendida", quantidadeVendida);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new Exception("Falha ao atualizar estoque via RPC. Resposta: " + response.body());
        }
    }

    private Object mapearTipoParaEnumDB(String tipoPeca) {
        switch (tipoPeca) { case "Fonte de Alimentação": return "FonteDeAlimentacao"; case "Placa Mãe": return "PlacaMae"; case "Processador": return "Processador"; case "RAM": return "Ram"; case "Placa de Vídeo": return "PlacaDeVideo"; case "Peça Genérica": return null; default: return null; }
    }

    private String mapearTipoParaNomeTabela(String tipoPeca) {
        switch (tipoPeca) { case "Fonte de Alimentação": return "fonte_de_alimentacao"; case "Placa Mãe": return "placa_mae"; case "Processador": return "processador"; case "RAM": return "ram"; case "Placa de Vídeo": return "placa_de_video"; default: return ""; }
    }


    public String fetchUserRole(String userId) throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/profiles?select=role&id=eq." + userId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Falha ao buscar o cargo do utilizador: " + response.body());
        }

        JSONArray jsonArray = new JSONArray(response.body());
        if (jsonArray.isEmpty()) {
            return "ESTAGIARIO"; // Retorna o cargo padrão se não encontrar perfil
        }
        return jsonArray.getJSONObject(0).getString("role");
    }
    public List<UserProfile> fetchAllProfiles() throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/profiles?select=id,email,role";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("apikey", SUPABASE_ANON_KEY).header("Authorization", "Bearer " + SUPABASE_ANON_KEY).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Falha ao buscar perfis de utilizador: " + response.body());
        }

        List<UserProfile> profiles = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(response.body());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            profiles.add(new UserProfile(obj.getString("id"), obj.getString("email"), obj.getString("role")));
        }
        return profiles;
    }

    /**
     * NOVO MÉTODO: Atualiza o cargo de um utilizador específico.
     * @param userId O ID do utilizador a ser atualizado.
     * @param newRole O novo cargo a ser atribuído (ex: "NIVEL1").
     */
    public void updateUserRole(String userId, String newRole) throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/profiles?id=eq." + userId;
        JSONObject jsonBody = new JSONObject().put("role", newRole);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            throw new Exception("Falha ao atualizar o cargo do utilizador: " + response.body());
        }
    }
}
