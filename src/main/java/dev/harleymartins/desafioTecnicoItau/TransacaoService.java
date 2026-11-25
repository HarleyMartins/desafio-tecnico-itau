package dev.harleymartins.desafioTecnicoItau;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.DoubleSummaryStatistics;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    public void validarTransacao(TransacaoRequest transacao) {

        if (transacao.getValor() == null) {
            throw new IllegalArgumentException("O valor não pode ser nulo");
        }
        if (transacao.getDataHora() == null) {
            throw new IllegalArgumentException("A data e hora não podem ser nulas");
        }

        if (transacao.getValor().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O valor não pode ser negativo");
        }
        if (transacao.getDataHora().isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException("A data da transação não pode ser no futuro");
        }
    }

    public EstatisticaDTO calcularEstatisticas() {
        OffsetDateTime limite = OffsetDateTime.now().minusSeconds(60);

        DoubleSummaryStatistics stats = transacaoRepository.listar().stream()
                .filter(t -> !t.getDataHora().isBefore(limite))
                .mapToDouble(t -> t.getValor().doubleValue())
                .summaryStatistics();

        if (stats.getCount() == 0) {
            return new EstatisticaDTO(0, 0.0, 0.0, 0.0, 0.0);
        }

        return new EstatisticaDTO(
                stats.getCount(),
                stats.getSum(),
                stats.getAverage(),
                stats.getMin(),
                stats.getMax());
    }
}