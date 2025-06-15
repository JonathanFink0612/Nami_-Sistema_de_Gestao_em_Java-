package com.exemplo.services;

import com.exemplo.models.MovimentacaoEstoque;
import com.exemplo.models.Venda;
import com.exemplo.models.VendaItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TransacaoService {

    private static final String SUPABASE_URL = "https://aeogsdzptuphlctfiwbo.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFlb2dzZHpwdHVwaGxjdGZpd2JvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5ODkyNTAsImV4cCI6MjA2NDU2NTI1MH0.E3MaMo196rv1jwTNRITOP6p4Xt4RTfUTaULH1cDwWIk";
    private final HttpClient client = HttpClient.newHttpClient();
    private final PecaSupabaseClient pecaService = new PecaSupabaseClient();

    /**
     * Orquestra o processo completo de registo de uma venda.
     * @param venda O objeto Venda contendo todos os detalhes da transação.
     * @throws Exception Se qualquer etapa do processo falhar.
     */
    public void registrarVenda(Venda venda) throws Exception {
        // --- ETAPA 1: Registar a Venda Principal ---
        String endpointVenda = SUPABASE_URL + "/rest/v1/vendas";
        JSONObject vendaJson = new JSONObject();
        vendaJson.put("valor_total", venda.getValorTotal());
        vendaJson.put("metodo_pagamento", venda.getMetodoPagamento());

        if (venda.getObservacoes() != null && !venda.getObservacoes().isEmpty()) {
            vendaJson.put("observacoes", venda.getObservacoes());
        }

        String userId = SessionManager.getInstance().getUserId();
        if (userId != null) {
            vendaJson.put("user_uid", userId);
        }

        HttpRequest reqVenda = HttpRequest.newBuilder()
                .uri(URI.create(endpointVenda))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(vendaJson.toString()))
                .build();

        HttpResponse<String> respVenda = client.send(reqVenda, HttpResponse.BodyHandlers.ofString());
        if (respVenda.statusCode() != 201) {
            throw new Exception("Falha ao registar a venda principal: " + respVenda.body());
        }
        int vendaId = new JSONArray(respVenda.body()).getJSONObject(0).getInt("venda_id");
        venda.setVendaId(vendaId);

        // --- ETAPA 2: Registar TODOS os Itens da Venda de uma só vez (Batch Insert) ---
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            // Se, por algum motivo, não houver itens, não há mais nada a fazer.
            return;
        }

        JSONArray itensJsonArray = new JSONArray();
        for (VendaItem item : venda.getItens()) {
            JSONObject itemJson = new JSONObject();
            itemJson.put("venda_id", vendaId);
            itemJson.put("peca_id", item.getPeca().getPecaId());
            itemJson.put("quantidade", item.getQuantidade());
            itemJson.put("preco_unitario", item.getPrecoUnitario());
            itensJsonArray.put(itemJson);
        }

        String endpointItens = SUPABASE_URL + "/rest/v1/venda_itens";
        HttpRequest reqItens = HttpRequest.newBuilder()
                .uri(URI.create(endpointItens))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(itensJsonArray.toString()))
                .build();

        HttpResponse<String> respItens = client.send(reqItens, HttpResponse.BodyHandlers.ofString());
        if (respItens.statusCode() != 201) {
            // Se isto falhar, a venda principal foi criada mas os itens não.
            // O ideal seria reverter a venda, mas, por agora, lançamos um erro claro.
            throw new Exception("Falha ao registar os itens da venda (batch insert): " + respItens.body());
        }

        // --- ETAPA 3: Atualizar o Stock para Cada Item ---
        // Esta parte é feita por último para garantir que a venda foi registada com sucesso primeiro.
        for (VendaItem item : venda.getItens()) {
            registrarMovimentacaoEstoque(new MovimentacaoEstoque(item.getPeca().getPecaId(), MovimentacaoEstoque.Tipo.SAIDA, item.getQuantidade(), vendaId));
            pecaService.atualizarEstoquePeca(item.getPeca().getPecaId(), item.getQuantidade());
        }
    }

    // O método inserirVendaItem agora não é mais necessário, pois a sua lógica foi movida para dentro do loop em registrarVenda.
    // Você pode removê-lo se quiser.

    /**
     * Regista uma movimentação de stock (ENTRADA ou SAIDA) na tabela 'movimentacao_estoque'.
     * @param mov O objeto MovimentacaoEstoque a ser registado.
     */
    private void registrarMovimentacaoEstoque(MovimentacaoEstoque mov) throws Exception {
        String endpoint = SUPABASE_URL + "/rest/v1/movimentacao_estoque";
        JSONObject movJson = new JSONObject();
        movJson.put("peca_id", mov.getPecaId());
        movJson.put("tipo_movimentacao", mov.getTipoMovimentacao());
        movJson.put("quantidade", mov.getQuantidade());
        if (mov.getVendaId() != null) {
            movJson.put("venda_id", mov.getVendaId());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(movJson.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) {
            throw new Exception("Falha ao registar movimentação de stock: " + response.body());
        }
    }
}
