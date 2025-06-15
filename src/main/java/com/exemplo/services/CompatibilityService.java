package com.exemplo.services;

import com.exemplo.models.Peca;
import com.exemplo.models.VendaItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Serviço responsável por verificar a compatibilidade entre as peças de um computador.
 */
public class CompatibilityService {

    /**
     * Verifica a compatibilidade de uma lista de itens do carrinho.
     * @param itens A lista de VendaItem do carrinho.
     * @return Uma lista de strings contendo mensagens de sucesso, alerta ou erro.
     */
    public List<String> verificarCompatibilidade(List<VendaItem> itens) {
        List<String> resultados = new ArrayList<>();
        if (itens.size() < 2) {
            resultados.add("INFO: Adicione pelo menos duas peças para verificar a compatibilidade.");
            return resultados;
        }

        // Encontra as peças chave no carrinho
        Optional<Peca> placaMaeOpt = encontrarPecaPorTipo(itens, "PlacaMae");
        Optional<Peca> processadorOpt = encontrarPecaPorTipo(itens, "Processador");
        Optional<Peca> ramOpt = encontrarPecaPorTipo(itens, "Ram");
        // (Você pode adicionar mais peças como PlacaDeVideo e Fonte aqui)

        // ===== INÍCIO DA SÉRIE DE VERIFICAÇÕES (IFs) =====

        // 1. Compatibilidade entre Placa-Mãe e Processador (CPU)
        if (placaMaeOpt.isPresent() && processadorOpt.isPresent()) {
            Peca placaMae = placaMaeOpt.get();
            Peca processador = processadorOpt.get();

            // Assume que os dados especializados estão no mapa 'camposAdicionais'
            String socketPlacaMae = (String) placaMae.getCamposAdicionais().get("socket_processador");
            String socketProcessador = (String) processador.getCamposAdicionais().get("socket_processador");

            if (socketPlacaMae != null && socketProcessador != null) {
                if (socketPlacaMae.equalsIgnoreCase(socketProcessador)) {
                    resultados.add("SUCESSO: O Processador é compatível com o socket da Placa-Mãe (" + socketPlacaMae + ").");
                } else {
                    resultados.add("ERRO: O socket do Processador (" + socketProcessador + ") é INCOMPATÍVEL com o da Placa-Mãe (" + socketPlacaMae + ").");
                }
            } else {
                resultados.add("AVISO: Não foi possível verificar a compatibilidade de socket (dados em falta).");
            }
        }

        // 2. Compatibilidade entre Placa-Mãe e Memória RAM
        if (placaMaeOpt.isPresent() && ramOpt.isPresent()) {
            Peca placaMae = placaMaeOpt.get();
            Peca ram = ramOpt.get();

            String tipoRamPlacaMae = (String) placaMae.getCamposAdicionais().get("tipo_ram");
            String tipoRamMemoria = (String) ram.getCamposAdicionais().get("tipo_ram");

            if (tipoRamPlacaMae != null && tipoRamMemoria != null) {
                if (tipoRamPlacaMae.equalsIgnoreCase(tipoRamMemoria)) {
                    resultados.add("SUCESSO: A Memória RAM (" + tipoRamMemoria + ") é compatível com a Placa-Mãe.");
                } else {
                    resultados.add("ERRO: A Memória RAM (" + tipoRamMemoria + ") é INCOMPATÍVEL com o tipo suportado pela Placa-Mãe (" + tipoRamPlacaMae + ").");
                }
            } else {
                resultados.add("AVISO: Não foi possível verificar a compatibilidade de RAM (dados em falta).");
            }
        }

        // (Você pode adicionar mais regras aqui, como PCI Express, consumo de energia, etc.)

        if (resultados.isEmpty()) {
            resultados.add("INFO: Nenhuma regra de compatibilidade pôde ser aplicada às peças selecionadas.");
        }

        return resultados;
    }

    /**
     * Helper para encontrar uma peça de um tipo específico na lista de itens.
     */
    private Optional<Peca> encontrarPecaPorTipo(List<VendaItem> itens, String tipoPeca) {
        return itens.stream()
                .map(VendaItem::getPeca)
                .filter(peca -> tipoPeca.equalsIgnoreCase(peca.getTipoPeca()))
                .findFirst();
    }
}
