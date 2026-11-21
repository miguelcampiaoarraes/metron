<div align="center">

<img src="src/main/resources/static/assets/logo.svg" alt="METRON Logo" width="120" />

# METRON

### Plataforma Web de Planejamento e Organização Financeira Pessoal

*"Não desperdice seu tempo e seu dinheiro."*

<br/>

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![H2](https://img.shields.io/badge/H2_Database-dev-003B57?style=for-the-badge&logo=h2&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-prod-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)

</div>

---

## Sobre o Projeto

**METRON** é uma aplicação FinTech web completa que responde com precisão a uma pergunta simples mas poderosa:

> **Quanto posso gastar hoje sem comprometer minhas contas, poupança e metas?**

A plataforma calcula automaticamente sua sobra livre a partir do salário, deduz contas fixas e o valor que você quer guardar por mês, e divide o restante pelos dias até o próximo pagamento — entregando um **limite diário seguro** atualizado em tempo real.

---

## Funcionalidades

### Administração Geral (Dashboard)
- **Painel Hero** com 5 métricas em tempo real: Salário Base → + Ganhos Extras → − Contas Fixas → − Guardar no Mês → = Quanto Posso Gastar por Dia
- **Contas Fixas do Mês** — cadastro com nome, valor, categoria, dia de vencimento e duração (mensal ou por N meses); toggle de situação Pago / Pendente com 1 clique
- **Ganhos Extras no Mês** — registro de 13º salário, bônus, comissões, vendas, rendimentos e qualquer entrada além do salário
- **Gastos do Dia a Dia** — anotação rápida de supermercado, padaria, farmácia, transporte e imprevistos com forma de pagamento

### Metas e Relatórios
- **Metas de Economia** — acompanhamento de objetivos com cálculo de esforço mensal e progresso percentual
- **Relatório Mensal** — visão consolidada de receitas, despesas e saldo; exportação em PDF executivo com OpenPDF
- **Regra 50-30-20** — distribuição automática do orçamento entre necessidades, estilo de vida e metas

### Assistente Financeiro
- Diagnóstico estruturado em 6 pontos com análise da situação atual
- Sugestões personalizadas baseadas no padrão de gastos do usuário

### Segurança
- Autenticação stateless com **JWT (JSON Web Tokens)**
- Senhas criptografadas com **BCrypt**
- Isolamento estrito de dados — cada usuário acessa somente seus próprios registros
- Validação de entrada com **Bean Validation** em todas as rotas protegidas

---

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 17 (LTS) |
| Framework | Spring Boot 3.3.4 |
| API REST | Spring Web (MVC) |
| Segurança | Spring Security 6 + JWT (JJWT 0.12.5) |
| Persistência | Spring Data JPA + Hibernate 6 |
| Migrations | Flyway |
| BD Desenvolvimento | H2 (modo PostgreSQL em memória) |
| BD Produção | PostgreSQL 14+ |
| PDF | OpenPDF 1.3.40 |
| Build | Apache Maven 3.8+ |
| Frontend | HTML5 · CSS3 · JavaScript ES6+ · Chart.js · Fetch API |

---

## Como Executar

### Pré-requisitos
- **JDK 17** ou superior — [Download](https://adoptium.net/)
- **Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)

### 1. Clone o repositório

```bash
git clone https://github.com/MiguelArraes/metron.git
cd metron
```

### 2. Build do projeto

```bash
mvn clean package -DskipTests
```

### 3. Execute (perfil de desenvolvimento — banco H2 em memória)

```bash
java -jar target/metron-platform-1.0.0.jar --spring.profiles.active=dev
```

Acesse no navegador: **http://localhost:8081**

Crie uma conta em `/register` ou faça login em `/login`.

---

### Produção com PostgreSQL

Crie o banco e configure as variáveis de ambiente:

```bash
# Crie o banco no PostgreSQL
createdb metron

# Execute com perfil prod
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/metron
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=sua_senha

java -jar target/metron-platform-1.0.0.jar
```

> **Nota:** O Flyway aplicará automaticamente todas as migrations na inicialização.

---

### Testes Automatizados

```bash
mvn test
```

São executados 16 testes unitários e de integração validando:
- Precisão dos cálculos financeiros com `BigDecimal`
- Isolamento de dados entre usuários
- Simulador de impacto de compras
- Autenticação e hash de senhas com JWT + BCrypt

---

## Estrutura do Projeto

```
metron/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/metron/
    │   │   ├── MetronApplication.java
    │   │   ├── controller/          # 15 controllers REST
    │   │   ├── dto/                 # 20 DTOs de entrada e saída
    │   │   ├── exception/           # Tratamento global de erros
    │   │   ├── model/entity/        # 14 entidades JPA
    │   │   ├── repository/          # 7 repositórios Spring Data
    │   │   ├── security/            # JWT, BCrypt, SecurityConfig
    │   │   └── service/             # 14 serviços com regras de negócio
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       ├── db/migration/
    │       │   └── V1__init_schema.sql
    │       └── static/
    │           ├── dashboard.html   # Administração Geral
    │           ├── reports.html     # Metas e Relatórios
    │           ├── assistant.html   # Assistente Financeiro
    │           ├── login.html
    │           ├── register.html
    │           ├── css/styles.css
    │           ├── js/
    │           │   ├── api.js       # Cliente HTTP com JWT
    │           │   ├── navbar.js
    │           │   └── dashboard.js
    │           └── assets/
    │               ├── logo.svg
    │               ├── logo-white.svg
    │               └── favicon.svg
    └── test/java/com/metron/        # 16 testes automatizados
```

---

## API REST — Endpoints Principais

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/auth/register` | Cadastro de novo usuário |
| `POST` | `/api/auth/login` | Login — retorna token JWT |
| `GET` | `/api/users/me` | Perfil do usuário autenticado |
| `PUT` | `/api/users/me` | Atualiza salário, poupança e preferências |
| `GET` | `/api/daily-limit` | Limite diário seguro calculado |
| `GET` | `/api/expenses` | Lista despesas com filtros |
| `POST` | `/api/expenses` | Cadastra despesa (fixa ou pontual) |
| `PUT` | `/api/expenses/{id}` | Edita despesa |
| `DELETE` | `/api/expenses/{id}` | Remove despesa |
| `GET` | `/api/incomes` | Lista receitas |
| `POST` | `/api/incomes` | Cadastra ganho extra |
| `GET` | `/api/categories?type=EXPENSE` | Categorias de despesa |
| `GET` | `/api/goals` | Metas financeiras |
| `POST` | `/api/goals/{id}/deposit` | Aporte em meta |
| `GET` | `/api/reports/monthly/{year}/{month}/pdf` | Exporta relatório PDF |
| `POST` | `/api/assistant/ask` | Pergunta ao assistente financeiro |

Todas as rotas (exceto `/api/auth/*`) exigem header `Authorization: Bearer <token>`.

---

## Motor Financeiro

A fórmula central da METRON:

```
Sobra Livre = Salário + Ganhos Extras - Contas Fixas - Guardar no Mês

Limite Diário = Sobra Livre ÷ Dias até o Próximo Pagamento
```

Todos os cálculos monetários utilizam `BigDecimal` com arredondamento `HALF_UP` para garantir precisão absoluta — sem erros de ponto flutuante.

---

## Paleta de Cores

| Papel | Cor | Hex |
|-------|-----|-----|
| Fundo Principal | Navy Noturno | `#0F172A` |
| Fundo Profundo | Azul Nível 950 | `#020617` |
| Destaque Positivo | Verde Esmeralda | `#0D9488` |
| Destaque Secundário | Verde Menta | `#10B981` |
| Texto / Superfície | Ardósia Clara | `#F8FAFC` |

---

## Autor

**Miguel Campião Arraes**
Estudante de Desenvolvimento de Sistemas — SENAI
`miguel.arraes@aluno.senai.br`

---

<div align="center">

Feito com dedicação — METRON © 2026

</div>
