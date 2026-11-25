package dev.harleymartins.desafioTecnicoItau;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class TransacaoService {

    public void validarTransacao(TransacaoRequest transacaoRequest){

        // VALOR MAIOR OU IGUAL A 0
        if(transacaoRequest.getValor().compareTo(BigDecimal.ZERO) < 0 ){
            throw new IllegalArgumentException("Erro: Isso não é uma transação válida, transações devem ter valor maior do que zero");

        } else{

        }


        //DATA MENOR OU IGUAL A DATA DE HOJE
        if(transacaoRequest.getDataHora().isAfter(OffsetDateTime.now())){
            throw new IllegalArgumentException("Erro: A data da transação não pode ser no futuro");
        }


        //VALOR PRESENTE
        if(transacaoRequest.getValor() == null){
            throw new IllegalArgumentException("Erro: o valor não pode ser nulo");
        }

        //DATAHORA PRESENTE
        if(transacaoRequest.getDataHora() == null){
            throw new IllegalArgumentException("Erro: a data e hora não podem ser nulas");
        }

    }

}
