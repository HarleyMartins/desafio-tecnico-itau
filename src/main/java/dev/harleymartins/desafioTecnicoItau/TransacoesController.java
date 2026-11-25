package dev.harleymartins.desafioTecnicoItau;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransacoesController {

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @PostMapping("/transacao")
    public ResponseEntity<Void> adicionar(@RequestBody TransacaoRequest transacaoRequest) {
        try {
            transacaoService.validarTransacao(transacaoRequest);
            transacaoRepository.salvar(transacaoRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }
    }

    @DeleteMapping("/transacao")
    public ResponseEntity<Void> limpar() {
        transacaoRepository.limpar();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/estatistica")
    public ResponseEntity<EstatisticaDTO> estatisticas() {
        return ResponseEntity.ok(transacaoService.calcularEstatisticas());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Void> requisicaoInvalida() {
        return ResponseEntity.badRequest().build();
    }
}