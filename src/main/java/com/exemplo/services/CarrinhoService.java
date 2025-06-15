package com.exemplo.services;

import com.exemplo.models.Peca;
import com.exemplo.models.VendaItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.Optional;

public final class CarrinhoService {

    private static final CarrinhoService instance = new CarrinhoService();
    private final ObservableList<VendaItem> itens;

    private CarrinhoService() {
        this.itens = FXCollections.observableArrayList();
    }

    public static CarrinhoService getInstance() {
        return instance;
    }

    public void adicionarItem(Peca peca) {
        Optional<VendaItem> itemExistente = itens.stream()
                .filter(item -> item.getPeca().getPecaId() == peca.getPecaId())
                .findFirst();

        if (itemExistente.isPresent()) {
            VendaItem item = itemExistente.get();
            item.setQuantidade(item.getQuantidade() + 1);
            int index = itens.indexOf(item);
            itens.set(index, item);
        } else {
            itens.add(new VendaItem(peca, 1));
        }
    }

    public void removerItem(VendaItem item) {
        itens.remove(item);
    }

    public ObservableList<VendaItem> getItens() {
        return itens;
    }

    public void limparCarrinho() {
        itens.clear();
    }

    /**
     * MÉTODO ADICIONADO
     * Retorna uma cópia da lista de itens. É usado ao finalizar a venda
     * para garantir que a transação seja processada com os itens exatos daquele momento,
     * mesmo que o carrinho principal seja alterado logo em seguida.
     * @return Uma cópia da lista de itens.
     */
    public ArrayList<VendaItem> getItensCopia() {
        return new ArrayList<>(this.itens);
    }
}