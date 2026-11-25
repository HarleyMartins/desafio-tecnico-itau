package dev.harleymartins.desafioTecnicoItau;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DesafioTecnicoItauApplicationTests {

	private static final String DATA_PASSADA = "2020-08-07T12:34:56.789-03:00";

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	void limparTransacoes() throws Exception {
		mockMvc.perform(delete("/transacao")).andExpect(status().isOk());
	}

	private ResultActions enviar(String corpo) throws Exception {
		return mockMvc.perform(post("/transacao")
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo));
	}

	private String transacao(String valor, String dataHora) {
		return "{\"valor\": %s, \"dataHora\": \"%s\"}".formatted(valor, dataHora);
	}

	@Test
	void contextLoads() {
	}

	// ---------- POST /transacao ----------

	@Test
	void transacaoValidaRetorna201SemCorpo() throws Exception {
		enviar(transacao("123.45", DATA_PASSADA))
				.andExpect(status().isCreated())
				.andExpect(content().string(""));
	}

	@Test
	void valorZeroEhAceito() throws Exception {
		enviar(transacao("0", DATA_PASSADA))
				.andExpect(status().isCreated());
	}

	@Test
	void valorNegativoRetorna422SemCorpo() throws Exception {
		enviar(transacao("-1", DATA_PASSADA))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().string(""));
	}

	@Test
	void dataNoFuturoRetorna422() throws Exception {
		enviar(transacao("10", OffsetDateTime.now().plusDays(1).toString()))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void camposFaltandoRetorna422() throws Exception {
		enviar("{}").andExpect(status().isUnprocessableEntity());
		enviar("{\"valor\": 10}").andExpect(status().isUnprocessableEntity());
		enviar("{\"dataHora\": \"" + DATA_PASSADA + "\"}").andExpect(status().isUnprocessableEntity());
	}

	@Test
	void jsonInvalidoRetorna400SemCorpo() throws Exception {
		enviar("{valor:")
				.andExpect(status().isBadRequest())
				.andExpect(content().string(""));
	}

	@Test
	void tipoErradoRetorna400() throws Exception {
		enviar(transacao("\"abc\"", DATA_PASSADA))
				.andExpect(status().isBadRequest());
	}

	@Test
	void dataForaDoPadraoIsoRetorna400() throws Exception {
		enviar(transacao("10", "ontem"))
				.andExpect(status().isBadRequest());
	}

	// ---------- DELETE /transacao ----------

	@Test
	void deleteRetorna200SemCorpoEApagaTudo() throws Exception {
		enviar(transacao("10", OffsetDateTime.now().toString()))
				.andExpect(status().isCreated());

		mockMvc.perform(delete("/transacao"))
				.andExpect(status().isOk())
				.andExpect(content().string(""));

		mockMvc.perform(get("/estatistica"))
				.andExpect(jsonPath("$.count").value(0));
	}

	// ---------- GET /estatistica ----------

	@Test
	void semTransacoesTodosOsValoresSaoZero() throws Exception {
		mockMvc.perform(get("/estatistica"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(0))
				.andExpect(jsonPath("$.sum").value(0.0))
				.andExpect(jsonPath("$.avg").value(0.0))
				.andExpect(jsonPath("$.min").value(0.0))
				.andExpect(jsonPath("$.max").value(0.0));
	}

	@Test
	void calculaEstatisticasDosUltimos60Segundos() throws Exception {
		enviar(transacao("100.50", OffsetDateTime.now().toString()));
		enviar(transacao("50", OffsetDateTime.now().toString()));

		mockMvc.perform(get("/estatistica"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(2))
				.andExpect(jsonPath("$.sum").value(150.5))
				.andExpect(jsonPath("$.avg").value(75.25))
				.andExpect(jsonPath("$.min").value(50.0))
				.andExpect(jsonPath("$.max").value(100.5));
	}

	@Test
	void ignoraTransacoesComMaisDe60Segundos() throws Exception {
		enviar(transacao("10", OffsetDateTime.now().toString()));
		enviar(transacao("999", OffsetDateTime.now().minusSeconds(120).toString()))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/estatistica"))
				.andExpect(jsonPath("$.count").value(1))
				.andExpect(jsonPath("$.sum").value(10.0))
				.andExpect(jsonPath("$.max").value(10.0));
	}
}