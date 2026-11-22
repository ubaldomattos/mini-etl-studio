# Documento de Requisitos — Mini ETL Studio

## 1. Objetivo do sistema

Permitir que um usuário importe arquivos CSV com dados de clientes, execute as etapas de
**extração**, **transformação/validação** e **carga** (ETL), acompanhe visualmente os resultados e
armazene em SQLite apenas os registros válidos e sem duplicidade. O sistema preserva os dados
originais de cada registro para permitir a exportação dos registros rejeitados e do processamento
completo, inclusive de importações antigas.

## 2. Escopo

**Dentro do escopo**

- Upload de CSV, validação de formato/estrutura e prévia dos dados.
- Extração, padronização, validação e identificação de duplicidades.
- Carga transacional dos registros válidos no SQLite.
- Dashboard com indicadores e gráficos.
- Consulta dos clientes armazenados com pesquisa por nome ou e-mail.
- Histórico de processamentos e três modalidades de exportação CSV.
- Interface responsiva com modo claro e escuro.

**Fora do escopo (nesta versão)**

- Autenticação e controle de acesso.
- Integração com serviços externos, filas ou processamento distribuído.
- Edição ou exclusão manual de clientes e de processamentos.
- Armazenamento do arquivo CSV original.

## 3. Requisitos funcionais

| ID | Requisito |
|---|---|
| RF01 | O sistema deve permitir selecionar um arquivo CSV no computador (clique ou arrastar e soltar). |
| RF02 | O sistema deve aceitar apenas arquivos com extensão `.csv`, com até 5 MB (configurável). |
| RF03 | O sistema deve identificar o delimitador (vírgula, ponto e vírgula ou tabulação) e os cabeçalhos. |
| RF04 | O arquivo deve conter as colunas obrigatórias **nome**, **email**, **telefone** e **cidade**, em qualquer ordem. Cabeçalhos são comparados sem acento, sem pontuação e sem diferenciar maiúsculas (`E-mail` = `email`). |
| RF05 | O sistema deve informar claramente quando o arquivo for inválido (vazio, binário, sem cabeçalho, sem registros, sem colunas obrigatórias ou malformado). |
| RF06 | O sistema deve apresentar uma prévia dos registros (primeiras 50 linhas, configurável) antes do processamento, sem gravar dados. |
| RF07 | A extração deve ler todos os registros, convertê-los para uma estrutura interna e informar a quantidade extraída. |
| RF08 | A extração deve identificar registros com problema de estrutura (quantidade de colunas diferente do cabeçalho). |
| RF09 | A transformação deve remover espaços desnecessários no início, no fim e repetidos em todos os campos. |
| RF10 | A transformação deve padronizar nomes em “Title Case” respeitando partículas (da, de, do, das, dos, e). |
| RF11 | A transformação deve converter e-mails para minúsculas. |
| RF12 | A transformação deve padronizar telefones mantendo somente dígitos e removendo o DDI 55. |
| RF13 | A transformação deve normalizar nomes de cidades (mesma regra de capitalização dos nomes). |
| RF14 | O sistema deve identificar e-mails inválidos. |
| RF15 | O sistema deve identificar campos obrigatórios ausentes. |
| RF16 | O sistema deve identificar telefones inválidos (fora do padrão DDD + 8 ou 9 dígitos). |
| RF17 | O sistema deve identificar registros duplicados pelo e-mail padronizado, dentro do arquivo e em relação aos clientes já existentes no banco. |
| RF18 | Cada registro deve ser classificado como **VALIDO**, **INVALIDO** ou **DUPLICADO**, com o motivo de cada problema. |
| RF19 | O sistema deve preservar os dados originais de cada registro, separados dos dados transformados. |
| RF20 | O usuário deve poder carregar os registros válidos de um processamento no banco de dados. |
| RF21 | A carga não deve inserir registros inválidos nem duplicados e deve informar quantos registros foram armazenados. |
| RF22 | Um mesmo processamento não pode ser carregado mais de uma vez. |
| RF23 | O dashboard deve exibir totais de registros extraídos, válidos, inválidos, duplicados e carregados, em cards e gráficos. |
| RF24 | O sistema deve permitir consultar os clientes armazenados, pesquisar por nome ou e-mail e visualizar os dados de cada cliente. |
| RF25 | O sistema deve manter o histórico de processamentos (identificador, arquivo, data/hora e quantidades). |
| RF26 | O usuário deve poder consultar os registros de processamentos anteriores, filtrando por situação. |
| RF27 | O sistema deve disponibilizar exatamente três exportações por processamento: CSV tratado, CSV de rejeitados e CSV completo. |
| RF28 | As exportações devem estar disponíveis após o processamento e também a partir do histórico. |
| RF29 | O sistema deve oferecer um arquivo CSV de exemplo para download. |
| RF30 | O usuário deve poder alternar entre modo claro e escuro, com a preferência preservada no navegador. |

## 4. Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF01 | Backend em Java 21 com Spring Boot 3 e Maven. |
| RNF02 | Persistência em SQLite via Spring Data JPA/Hibernate; nenhum SGBD externo necessário. |
| RNF03 | O banco deve ser criado automaticamente na primeira execução e ter sua localização configurável (`APP_DB_PATH`). |
| RNF04 | O esquema deve ser versionado (Flyway) para permitir evoluções do modelo. |
| RNF05 | A interface deve ser responsiva (computador, tablet e smartphone). |
| RNF06 | A aplicação deve funcionar sem acesso à internet (bibliotecas front-end empacotadas via WebJars). |
| RNF07 | O processamento é local e síncrono, adequado a arquivos de pequeno porte. |
| RNF08 | Os CSVs exportados devem usar UTF-8 com BOM, cabeçalhos consistentes e tratamento correto de aspas, vírgulas, acentos e quebras de linha, usando biblioteca (OpenCSV). |
| RNF09 | Os arquivos exportados são gerados dinamicamente, sem armazenamento permanente no servidor. |
| RNF10 | Erros devem ser tratados de forma centralizada, com mensagens claras ao usuário e registro em log. |
| RNF11 | A carga deve ser transacional: em caso de falha, nenhum registro é gravado. |
| RNF12 | O código deve seguir separação em camadas, SOLID e possuir testes automatizados (JUnit 5, Mockito). |
| RNF13 | Nenhuma credencial, banco local ou dado pessoal real deve ser versionado. |

## 5. Regras de negócio

| ID | Regra |
|---|---|
| RN01 | Linhas totalmente vazias são ignoradas e não contam como registros extraídos. |
| RN02 | O número da linha corresponde à linha física no arquivo (o cabeçalho é a linha 1). |
| RN03 | Colunas extras são ignoradas (com aviso), mas preservadas na linha bruta do registro. |
| RN04 | Um registro é **INVALIDO** quando qualquer regra de validação reprova: estrutura, campos obrigatórios, formato de e-mail ou formato de telefone. Todos os motivos são registrados. |
| RN05 | O e-mail é válido quando segue o formato `local@dominio.tld` (sem espaços, sem pontos consecutivos, domínio com ao menos um ponto) e tem até 254 caracteres. |
| RN06 | O telefone, após padronização, deve ter DDD (11 a 99) seguido de 8 dígitos iniciando em 2–5 (fixo) ou 9 dígitos iniciando em 9 (celular). |
| RN07 | Somente registros válidos participam da verificação de duplicidade. Um registro inválido nunca é considerado “original” de um duplicado. |
| RN08 | O primeiro registro válido com um e-mail é mantido; os seguintes com o mesmo e-mail padronizado são **DUPLICADO** e referenciam a linha do primeiro. |
| RN09 | Um registro válido cujo e-mail já existe na tabela de clientes é **DUPLICADO**, com referência ao cliente existente. |
| RN10 | Se, entre o processamento e a carga, o e-mail de um registro válido passar a existir no banco, o registro é reclassificado como **DUPLICADO** durante a carga e as quantidades do processamento são atualizadas. |
| RN11 | A carga grava os dados **transformados**. Um processamento com status `CARREGADO` não pode ser carregado novamente. |
| RN12 | O e-mail do cliente é único no banco (índice único), como proteção adicional contra duplicidade. |

## 6. Modalidades de exportação

As exportações usam os registros do processamento selecionado (tabela `registro_processamento`),
e não a tabela de clientes. Por isso funcionam para registros inválidos e duplicados e para
processamentos antigos. Todas usam UTF-8 com BOM e separador vírgula (configurável por
`APP_EXPORT_DELIMITER`).

### 6.1 CSV tratado — `tratado_proc{id}.csv`

| Coluna | Conteúdo |
|---|---|
| `nome`, `email`, `telefone`, `cidade` | Valores **transformados** |

Critérios:

- Contém somente registros com situação `VALIDO`.
- Não contém inválidos nem duplicados; os e-mails são únicos.
- Os dados correspondem exatamente aos registros elegíveis para carga (e carregados, se a carga foi executada).

### 6.2 CSV de rejeitados — `rejeitados_proc{id}.csv`

| Coluna | Conteúdo |
|---|---|
| `numero_linha` | Linha do registro no arquivo original |
| `nome`, `email`, `telefone`, `cidade` | Valores **originais**, exatamente como lidos |
| `tipo_rejeicao` | `VALIDACAO` (erro de validação) ou `DUPLICIDADE` |
| `motivo_rejeicao` | Todos os motivos, separados por `; ` |

Critérios:

- Contém todos os registros `INVALIDO` e `DUPLICADO`, e somente eles.
- Os valores originais não são substituídos pelos transformados (espaços e maiúsculas preservados).
- Para duplicados, o motivo indica a linha original ou o cliente já cadastrado.

### 6.3 CSV completo — `completo_proc{id}.csv`

| Coluna | Conteúdo |
|---|---|
| `numero_linha` | Linha do registro no arquivo original |
| `nome`, `email`, `telefone`, `cidade` | Valores **originais** |
| `situacao` | `VALIDO`, `INVALIDO` ou `DUPLICADO` |
| `resultado_validacao` | Resumo e resultado de cada validação |

Formato de `resultado_validacao`:

```
Todas as validações foram aprovadas. ESTRUTURA=APROVADA; CAMPOS_OBRIGATORIOS=APROVADA; EMAIL_FORMATO=APROVADA; TELEFONE_FORMATO=APROVADA; DUPLICIDADE=APROVADA
Validação reprovada. ESTRUTURA=APROVADA; CAMPOS_OBRIGATORIOS=APROVADA; EMAIL_FORMATO=REPROVADA (E-mail inválido: 'ana@exemplo'); TELEFONE_FORMATO=APROVADA; DUPLICIDADE=NAO_APLICAVEL
Duplicidade identificada. ...; DUPLICIDADE=REPROVADA (E-mail duplicado: já informado na linha 2)
```

Critérios:

- Contém todos os registros extraídos, na ordem do arquivo.
- Preserva os dados originais.
- Para válidos, informa que as validações foram aprovadas; para inválidos, os motivos; para duplicados, a duplicidade e a linha/cliente de origem.

## 7. Critérios de aceitação

| ID | Critério |
|---|---|
| CA01 | Ao selecionar o `clientes_exemplo.csv`, a prévia é exibida com 14 registros e nenhum dado é gravado. |
| CA02 | Um arquivo sem a coluna `cidade` exibe “Colunas obrigatórias ausentes: Cidade”. |
| CA03 | Após processar o arquivo de exemplo, o detalhe mostra 14 extraídos, 6 válidos, 6 inválidos e 2 duplicados. |
| CA04 | A carga grava os 6 válidos, informa “6 registro(s) carregado(s)” e o status passa para `CARREGADO`. |
| CA05 | Tentar carregar o mesmo processamento novamente exibe “já foi carregado”. |
| CA06 | Reimportar o mesmo arquivo classifica como duplicados os e-mails já existentes; nenhum cliente é duplicado. |
| CA07 | O CSV tratado contém apenas os 6 válidos, com dados padronizados. |
| CA08 | O CSV de rejeitados contém os 8 rejeitados com os valores originais e `motivo_rejeicao`. |
| CA09 | O CSV completo contém os 14 registros com `situacao` e `resultado_validacao`. |
| CA10 | As três exportações funcionam a partir do histórico, inclusive após reiniciar a aplicação. |
| CA11 | Os dados permanecem após reiniciar a aplicação. |
| CA12 | A interface funciona em larguras de smartphone (≥ 360 px) e o tema escolhido persiste ao recarregar. |
| CA13 | `mvn test` executa todos os testes com sucesso. |

## 8. Restrições e premissas

- Java 21 (JDK 21.0.2) e Maven 3.9+ disponíveis na máquina.
- Aplicação de uso local, monousuário e sem autenticação.
- Arquivos de pequeno porte (até 5 MB por padrão); o processamento ocorre em memória.
- O padrão de telefone adotado é o brasileiro.
- A codificação esperada é UTF-8; arquivos em Windows-1252 são aceitos com aviso.
- O SQLite admite um escritor por vez; o pool de conexões tem tamanho 1.
