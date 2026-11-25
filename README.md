# Desafio Técnico Itaú Unibanco: API de Transações e Estatísticas

API REST desenvolvida em **Java** com **Spring Boot** que recebe transações financeiras e calcula estatísticas sobre as transações dos últimos 60 segundos.

Todos os dados ficam armazenados **em memória**. Não há banco de dados nem cache.

## Tecnologias

- Java 21
- Spring Boot 4.2.0 (Spring Web MVC)
- Maven (wrapper incluído, não precisa instalar o Maven)
- Lombok
- JUnit 5 e MockMvc (testes)

## Pré-requisitos

- **JDK 21** instalado e configurado (`java -version` deve mostrar a versão 21)

## Como executar

1. Clone o repositório e entre na pasta do projeto:

   ```bash
   git clone https://github.com/HarleyMartins/desafio-tecnico-itau.git
   cd desafio-tecnico-itau
   ```

2. Inicie a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

   No Windows (prompt de comando ou PowerShell):

   ```bash
   mvnw.cmd spring-boot:run
   ```

3. A API fica disponível em `http://localhost:8080`.

Para gerar o `.jar` e executá-lo:

```bash
./mvnw clean package
java -jar target/desafioTecnicoItau-0.0.1-SNAPSHOT.jar
```

## Como executar os testes

```bash
./mvnw test
```

A suíte tem 13 testes automatizados, que sobem a aplicação e fazem requisições HTTP simuladas com `MockMvc`. Eles cobrem os três endpoints, tanto os casos de sucesso quanto os de erro:

- transação válida, valor zero, valor negativo e data no futuro
- campos faltando, JSON inválido, tipo errado e data fora do padrão ISO 8601
- limpeza das transações com `DELETE`
- estatísticas zeradas quando não há transações
- cálculo correto de `count`, `sum`, `avg`, `min` e `max`
- exclusão das transações com mais de 60 segundos

## Endpoints

### `POST /transacao`: receber transação

Corpo da requisição:

```json
{
   "valor": 123.45,
   "dataHora": "2020-08-07T12:34:56.789-03:00"
}
```

| Campo      | Descrição                                         | Obrigatório |
|------------|---------------------------------------------------|-------------|
| `valor`    | Valor decimal da transação                        | Sim         |
| `dataHora` | Data e hora da transação, no padrão ISO 8601      | Sim         |

A transação só é aceita se:

1. os campos `valor` e `dataHora` estiverem preenchidos;
2. a transação **não** tiver acontecido no futuro;
3. o valor for igual ou maior que `0` (zero).

Respostas, todas **sem corpo**:

| Status                     | Quando                                                                     |
|----------------------------|----------------------------------------------------------------------------|
| `201 Created`              | A transação foi validada e registrada                                      |
| `422 Unprocessable Entity` | Algum critério de aceite não foi atendido (campo faltando, valor negativo, data no futuro) |
| `400 Bad Request`          | A requisição não pôde ser compreendida (JSON inválido, tipo errado, data fora do padrão ISO 8601) |

### `DELETE /transacao`: limpar transações

Apaga todas as transações armazenadas.

| Status   | Quando                                   |
|----------|------------------------------------------|
| `200 OK` | Todas as transações foram apagadas (sem corpo) |

### `GET /estatistica`: calcular estatísticas

Retorna as estatísticas das transações que aconteceram nos **últimos 60 segundos**.

Resposta `200 OK`:

```json
{
   "count": 10,
   "sum": 1234.56,
   "avg": 123.456,
   "min": 12.34,
   "max": 123.56
}
```

| Campo   | Descrição                                          |
|---------|----------------------------------------------------|
| `count` | Quantidade de transações nos últimos 60 segundos   |
| `sum`   | Soma dos valores nos últimos 60 segundos           |
| `avg`   | Média dos valores nos últimos 60 segundos          |
| `min`   | Menor valor nos últimos 60 segundos                |
| `max`   | Maior valor nos últimos 60 segundos                |

Quando não há transações nos últimos 60 segundos, todos os campos retornam `0`.

## Exemplos de uso

```bash
# Registrar uma transação
curl -i -X POST http://localhost:8080/transacao \
  -H "Content-Type: application/json" \
  -d '{"valor": 123.45, "dataHora": "2020-08-07T12:34:56.789-03:00"}'

# Consultar as estatísticas
curl -i http://localhost:8080/estatistica

# Apagar todas as transações
curl -i -X DELETE http://localhost:8080/transacao
```

Também é possível testar pelo Postman: nas requisições `POST`, use **Body > raw > JSON**. A variável dinâmica `{{$isoTimestamp}}` gera a data e hora atuais, o que é útil para testar a janela de 60 segundos:

```json
{"valor": 100.50, "dataHora": "{{$isoTimestamp}}"}
```

## Estrutura do projeto

```
src/main/java/dev/harleymartins/desafioTecnicoItau/
├── DesafioTecnicoItauApplication.java   # Classe principal
├── TransacoesController.java            # Endpoints REST
├── TransacaoService.java                # Validação e cálculo das estatísticas
├── TransacaoRepository.java             # Armazenamento em memória
├── TransacaoRequest.java                # Objeto recebido no POST
└── EstatisticaDTO.java                  # Objeto retornado no GET /estatistica

src/test/java/dev/harleymartins/desafioTecnicoItau/
└── DesafioTecnicoItauApplicationTests.java   # Testes automatizados
```

## Decisões de projeto

- **Camadas separadas.** O controller trata o HTTP, o service concentra as regras de negócio e o repository cuida do armazenamento.
- **Armazenamento em memória.** O repository usa uma `CopyOnWriteArrayList`, que é segura para requisições simultâneas, e não há banco de dados nem cache.
- **Valores monetários.** O campo `valor` é lido como `BigDecimal`, o que evita erros de arredondamento na comparação com zero. O cálculo das estatísticas converte os valores para `double`.
- **Datas.** O campo `dataHora` usa `OffsetDateTime`, que já interpreta o padrão ISO 8601 com fuso horário.
- **Janela de 60 segundos.** As estatísticas são calculadas no momento da consulta, considerando apenas as transações cuja `dataHora` não seja anterior a `agora - 60 segundos`. Transações antigas não entram na conta, mas continuam em memória até a chamada de `DELETE /transacao` ou até a aplicação ser reiniciada.
- **Estatísticas.** O cálculo usa `DoubleSummaryStatistics`. Quando não há transações na janela, a resposta é devolvida com todos os campos em `0`.
- **Tratamento de 400.** Uma requisição que o Spring não consegue ler (JSON inválido, tipo errado, data fora do padrão) é tratada no controller e devolve `400` sem corpo.

## Autor

Harley Martins