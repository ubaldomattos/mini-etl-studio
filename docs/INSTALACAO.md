# Manual de Instalação: Mini ETL Studio

Este guia leva você do zero até a aplicação rodando em `http://localhost:8080`. Não é
preciso instalar nenhum banco de dados: o SQLite é embutido e o arquivo do banco é criado
automaticamente.

## 1. Pré-requisitos

| Item | Versão | Como verificar |
|---|---|---|
| JDK (Java Development Kit) | **21** | `java -version` e `javac -version` |
| Apache Maven | 3.9 ou superior | `mvn -v` |
| Git (opcional, para clonar) | qualquer | `git --version` |
| Navegador | Chrome, Edge, Firefox ou Safari recentes | n/a |

O download das dependências exige acesso à internet (Maven Central), mas só na primeira
compilação. Depois disso a aplicação funciona offline.

## 2. Instalação do Java 21 (se necessário)

Se `java -version` não mostrar a versão 21, instale um JDK 21:

**Windows**

1. Baixe o instalador do JDK 21 (Eclipse Temurin: <https://adoptium.net/temurin/releases/?version=21>, ou Oracle JDK).
2. Execute o instalador e marque a opção para configurar `JAVA_HOME` e o `PATH`.
3. Ou, pelo terminal: `winget install EclipseAdoptium.Temurin.21.JDK`.
4. Abra um **novo** terminal e confirme com `java -version`.

**Linux (Debian/Ubuntu)**

```bash
sudo apt update && sudo apt install openjdk-21-jdk
```

**macOS (Homebrew)**

```bash
brew install openjdk@21
```

### Instalação do Maven (se necessário)

- Windows: `winget install Apache.Maven`, ou baixe em <https://maven.apache.org/download.cgi>, extraia e adicione a pasta `bin` ao `PATH`.
- Linux: `sudo apt install maven`
- macOS: `brew install maven`

Confirme com `mvn -v`. A saída deve mostrar `Java version: 21...`. Se mostrar outra versão,
ajuste a variável `JAVA_HOME` para a pasta do JDK 21.

## 3. Obtendo o código

```bash
git clone https://github.com/ubaldomattos/mini-etl-studio.git
cd mini-etl-studio
```

Outra opção é baixar o ZIP pelo GitHub (botão **Code → Download ZIP**) e extraí-lo.

## 4. Configuração

Todas as configurações têm valores padrão, então nenhuma alteração é obrigatória. Para
personalizar, defina variáveis de ambiente antes de executar:

| Variável | Padrão | Descrição |
|---|---|---|
| `APP_DB_PATH` | `./data/mini-etl.db` | Caminho do arquivo SQLite. A pasta é criada automaticamente. |
| `APP_PORT` | `8080` | Porta HTTP |
| `APP_MAX_UPLOAD` | `5MB` | Tamanho máximo do arquivo enviado |
| `APP_PREVIEW_LINHAS` | `50` | Linhas exibidas na prévia |
| `APP_EXPORT_DELIMITER` | `,` | Separador dos CSVs exportados. Use `;` para abrir direto no Excel em português. |

Exemplos:

```powershell
# Windows PowerShell
$env:APP_DB_PATH = "C:\dados\mini-etl.db"
$env:APP_PORT = "9090"
```

```bash
# Linux / macOS
export APP_DB_PATH=/home/usuario/dados/mini-etl.db
export APP_PORT=9090
```

As mesmas propriedades podem ser passadas como argumentos:
`--app.db-path=...`, `--server.port=...`, `--app.export.delimiter=;`.

## 5. Compilação

Na pasta do projeto:

```bash
mvn clean package
```

Esse comando baixa as dependências, compila, executa os testes automatizados e gera
`target/mini-etl-studio-1.0.0.jar`. Para compilar sem rodar os testes:

```bash
mvn clean package -DskipTests
```

## 6. Execução

**Opção A: pelo Maven (desenvolvimento)**

```bash
mvn spring-boot:run
```

**Opção B: pelo JAR gerado**

```bash
java -jar target/mini-etl-studio-1.0.0.jar
```

Quando aparecer no console a linha `Started MiniEtlStudioApplication`, abra
<http://localhost:8080> no navegador. Para encerrar, pressione `Ctrl + C` no terminal.

### Executando os testes

```bash
mvn test
```

Os testes usam bancos SQLite temporários em `target/test-db/` e não alteram o banco da aplicação.

## 7. Localização do banco SQLite

- Padrão: `data/mini-etl.db`, relativo à pasta de onde a aplicação foi iniciada.
- O caminho absoluto aparece no log de inicialização: `Banco SQLite em: ...`.
- O arquivo, as tabelas e os índices são criados na primeira execução pelo Flyway. Os dados permanecem entre reinicializações.
- Arquivos auxiliares `mini-etl.db-wal` e `mini-etl.db-shm` podem aparecer ao lado do banco. Eles fazem parte do modo WAL do SQLite.
- Para começar do zero, encerre a aplicação e apague o arquivo `.db` e os auxiliares.
- Para fazer backup, encerre a aplicação e copie o arquivo `.db`.
- Para inspecionar os dados, use qualquer cliente SQLite (por exemplo, DB Browser for SQLite).

A pasta `data/` e os arquivos `*.db` estão no `.gitignore` e não são versionados.

## 8. Solução de problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| `java: error: release version 21 not supported` ou `invalid target release: 21` | O Maven está usando um JDK antigo | Aponte `JAVA_HOME` para o JDK 21 e confira com `mvn -v`. |
| `'mvn' não é reconhecido como comando` | Maven fora do `PATH` | Instale o Maven ou adicione a pasta `bin` ao `PATH` e abra um novo terminal. |
| `Port 8080 was already in use` | Outra aplicação usa a porta | Execute com `APP_PORT=9090` ou `--server.port=9090`. |
| `[SQLITE_BUSY] The database file is locked` | Duas instâncias usando o mesmo banco | Encerre a outra instância ou use outro `APP_DB_PATH`. |
| `Não foi possível criar a pasta do banco de dados` | Sem permissão de escrita no caminho | Escolha um `APP_DB_PATH` em uma pasta com permissão. |
| Falha ao baixar dependências | Sem internet, proxy ou um *mirror* corporativo no `~/.m2/settings.xml` | Verifique a conexão e o proxy, ou ajuste o `settings.xml` para permitir o Maven Central. |
| “O arquivo excede o tamanho máximo permitido” | Upload maior que o limite | Aumente `APP_MAX_UPLOAD` (ex.: `10MB`). |
| Acentos estranhos no Excel | O Excel abriu sem reconhecer UTF-8 | Os arquivos já têm BOM. Se o Excel não separar colunas, use `APP_EXPORT_DELIMITER=;` ou importe em **Dados → De Texto/CSV**. |
| Acentos estranhos no console do Windows | O console não está em UTF-8 | Afeta apenas a exibição do log. Rode `chcp 65001` antes de iniciar. |
| Migração Flyway falhou após editar o SQL | O banco existente tem um *checksum* diferente | Não edite migrações já aplicadas: crie uma nova `V2__...sql`. Em desenvolvimento, apague o banco. |
