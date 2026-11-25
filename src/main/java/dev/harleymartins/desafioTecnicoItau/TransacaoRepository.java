package dev.harleymartins.desafioTecnicoItau;

import java.util.ArrayList;
import java.util.List;


public class TransacaoRepository {

    List<TransacaoRequest> listaDeTransacoes = new ArrayList<>();

    // salvar os dados em uma lista
    public void salvarDados(TransacaoRequest transacaoRequest){
        listaDeTransacoes.add(transacaoRequest);
    }


    // apaagar a lista depois de 60s
    public void limparDados(TransacaoRequest transacaoRequest){
        listaDeTransacoes.clear();
    }

    // apagar todas as transacoes da lista
    public void deletarDados(){

    }
}
