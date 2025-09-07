# Spring-ia-TwelveDATAAPI-stock-analyzer

**Projeto para fins de aprendizado** — aplicação demonstrativa que integra Spring Boot, Spring Data JPA, Flyway, Twelve Data (para cotações) e Spring AI (modelo OpenAI) para analisar o valor de uma carteira e gerar relatórios automaticamente usando *tools*.

---

## Visão geral

Este repositório contém uma aplicação em **Spring Boot** cujo objetivo é: buscar preços de ativos (usando Twelve Data), armazenar/ler a carteira em banco de dados, expor endpoints REST e enviar prompts + *tools* para um modelo via **Spring AI** (OpenAI) para que o modelo realize a análise e retorne uma resposta formatada (tabelas, somas e explicações).

O projeto é educacional — serve para aprender integração entre APIs de mercado, persistência, migrações e o uso de ferramentas (*tools*) com o Spring AI. **Não é aconselhamento financeiro.**

---

## Tecnologias e dependências principais

O projeto utiliza (trecho do `pom.xml`):

- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.springframework.boot:spring-boot-starter-web`
- `org.flywaydb:flyway-core`
- `org.flywaydb:flyway-mysql`
- `org.springframework.ai:spring-ai-starter-model-openai` (e `spring-ai-bom` via dependencyManagement)
- `com.mysql:mysql-connector-j` (runtime)
- `org.springframework.boot:spring-boot-starter-test` (test)

Outras ferramentas usadas no projeto:
- Banco de dados: **MySQL** (pode adaptar para H2 em testes)
- Migrações: **Flyway**
- API de preços: **Twelve Data** (outra API de mercado que desejar)
- Modelo LLM: **OpenAI** via Spring AI

---

## Arquitetura (resumo)

- **Entidades / Repositórios**: persistem a carteira (ativos, quantidade, metadados) no MySQL.
- **Migrations (Flyway)**: controlam a criação das tabelas e versões do esquema.
- **StockTools / WalletTools**: implementações de *tools* que o modelo pode chamar (ex.: retornar quantidade de ações, preços mais recentes, cálculos agregados, histórico de preços).
- **ChatClient (Spring AI)**: ponto de entrada para criar prompts, associar *tools* e executar chamadas ao modelo.
- **Controllers**: endpoints REST para acionar a análise (ex.: `/ai/wallet`, `/ai/with-tools`, `/ai/highest-day/{days}`).

---

## Endpoints disponíveis (Controller: `WalletController`)

- `GET /ai/wallet`
  - Cria um `PromptTemplate` e usa `ToolCallingChatOptions.builder().toolNames(...)` para permitir que o modelo invoque as ferramentas **numberOfShares** e **latestStockPrices**.

- `GET /ai/with-tools`
  - Executa o prompt e registra localmente as *tools* (`stockTools`, `walletTools`) via `.tools(stockTools, walletTools)` antes de chamar o modelo.

- `GET /ai/highest-day/{days}`
  - Pergunta ao modelo: em qual dia durante os últimos `{days}` dias a carteira teve o maior valor (com base nos preços históricos diários). Recebe `days` como `@PathVariable`.

Exemplo de uso (curl):

```bash
curl -X GET http://localhost:8080/ai/wallet

curl -X GET http://localhost:8080/ai/with-tools

curl -X GET http://localhost:8080/ai/highest-day/30
```

---

## Fluxo de execução (simplificado)

1. Aplicação inicia e configura conexões com o banco e Flyway aplica migrações.
2. `StockTools` consulta a Twelve Data (ou outro provedor) para obter preços atuais ou históricos.
3. `WalletTools` consulta o banco (repositório) para recuperar as posições (ativos e quantidades).
4. O `ChatClient` (Spring AI) cria um prompt, registra as *tools* e invoca o modelo OpenAI.
5. O modelo pode requisitar chamadas às *tools* — por exemplo, pedir o número de ações ou os preços mais recentes — e então gerar uma resposta com a tabela e os cálculos.

---

## Exemplo de resposta esperada

A aplicação produz uma resposta textual formatada (exemplo):

> Aqui estão os preços atuais das ações na sua carteira e o valor total em dólares:

| Companhia  | Quantidade de Ações | Preço Atual (USD) | Valor Total (USD)      |
|------------|----------------------|-------------------|-------------------------|
| AAPL       | 100                  | 239.69            | 23,969.00               |
| NVDA       | 200                  | 167.02            | 33,404.00               |
| AMZN       | 300                  | 232.33            | 69,699.00               |
| BTC/USD    | 5                    | 111,012.55        | 555,062.75              |
| ETH/USD    | 10                   | 4,278.14          | 42,781.40               |
| PBR        | 500                  | 12.21             | 6,105.00                |
| VALE       | 400                  | 10.40             | 4,160.00                |

**Valor Total:** `734,181.15 USD`

> Observação: esse é um exemplo de saída gerada pelo fluxo — os valores reais dependem das cotações retornadas pela API de mercado no momento da execução.

---

## Configuração / Variáveis de ambiente

Adicione as credenciais e propriedades necessárias (exemplo `application.properties` / `application.yml`):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wallet_db
spring.datasource.username=root
spring.datasource.password=secret

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Twelve Data
twelvedata.api.key=${TWELVEDATA_API_KEY}

# Spring AI / OpenAI
spring.ai.openai.api-key=${OPENAI_API_KEY}
# (ou configure via variável de ambiente OPENAI_API_KEY conforme a sua preferência)

# Ajuste porta se quiser
server.port=8080
```

**Atenção:** nunca comite chaves reais no repositório.

---

## Como executar (local)

1. Configure MySQL e crie a base `wallet_db` (ou ajuste as propriedades).
2. Defina variáveis de ambiente: `TWELVEDATA_API_KEY`, `OPENAI_API_KEY`.
3. Build com Maven:

```bash
mvn clean package
```

4. Rode a aplicação:

```bash
java -jar target/seu-artifact.jar
```

5. Teste os endpoints via curl/Postman.

---

## Implementação das Tools (nota rápida)

- As *tools* (por ex. `StockTools`, `WalletTools`) devem ser beans que expõem operações que o modelo pode chamar. No Spring AI, você registra esses beans quando chama o `ChatClient` ou através da infraestrutura que o `spring-ai` fornece.
- Uma *tool* típica realiza: chamada à Twelve Data → parse do retorno → transformação em DTO → retorno de dados simples (JSON ou texto) que o modelo pode consumir.

---

## Boas práticas e pontos importantes

- Trate limites (rate limits) da API Twelve Data.
- Implemente caches para preços quando fizer sentido (reduz chamadas desnecessárias).
- Valide entradas do usuário e trate erros de integração com a API/DB.
- Sempre deixe claro ao usuário final que os resultados são informativos e **não constituem recomendação financeira**.
