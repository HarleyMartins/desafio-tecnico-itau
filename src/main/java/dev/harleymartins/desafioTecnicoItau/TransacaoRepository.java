package dev.harleymartins.desafioTecnicoItau;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class TransacaoRepository {

    private final List<TransacaoRequest> transacoes = new CopyOnWriteArrayList<>();

    // salvar os dados em uma lista
    public void salvar(TransacaoRequest transacao){
        transacoes.add(transacao);
    }


    public List<TransacaoRequest> listar(){
        return List.copyOf(transacoes);
    }

    public void limpar(){
        transacoes.clear();
    }
}
