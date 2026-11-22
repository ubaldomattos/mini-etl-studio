# Manual do Usuário: Mini ETL Studio

## 1. Apresentação da interface

A barra superior tem quatro áreas e o seletor de tema:

| Menu | Para que serve |
|---|---|
| **Dashboard** | Resumo de todas as importações, com indicadores e gráficos. |
| **Importar** | Envio de um novo arquivo CSV, prévia e processamento. |
| **Clientes** | Consulta dos clientes gravados no banco de dados. |
| **Histórico** | Lista de todas as importações e acesso às exportações. |
| Ícone de lua/sol | Alterna entre modo claro e escuro. |

Em telas pequenas (smartphones), o menu fica recolhido no botão ☰ no canto superior direito.

As etapas do fluxo ETL aparecem como três blocos numerados: **Extract**, **Transform** e **Load**.
Um bloco colorido com ✓ indica etapa concluída. Um bloco destacado indica a etapa atual. Um
bloco esmaecido ainda não foi executado.

Os registros têm cores fixas por situação, em todas as telas:

| Situação | Cor | Significado |
|---|---|---|
| **Válido** | Verde | Passou em todas as validações e pode ser carregado. |
| **Inválido** | Vermelho | Falhou em ao menos uma validação. |
| **Duplicado** | Laranja | É válido, mas o e-mail já apareceu antes no arquivo ou já existe no banco. |

## 2. Importação de arquivos CSV

### 2.1 Formato esperado

O arquivo deve ter um cabeçalho com as colunas **nome**, **email**, **telefone** e **cidade**:

```csv
Nome,E-mail,Telefone,Cidade
Maria da Silva,maria@exemplo.com,(31) 98765-4321,Belo Horizonte
```

- As colunas podem estar em qualquer ordem. Variações como `E-mail`, `EMAIL` ou `Telefone` são reconhecidas.
- O separador pode ser vírgula, ponto e vírgula ou tabulação. A detecção é automática.
- Valores com vírgula ou quebra de linha devem estar entre aspas (padrão CSV).
- Colunas extras são ignoradas, com um aviso na prévia.
- Codificação recomendada: UTF-8. Arquivos salvos pelo Excel em Windows-1252 também são aceitos, com aviso.
- Tamanho máximo: 5 MB.

Para testar, baixe o **CSV de exemplo** pelo link na tela *Importar* ou no Dashboard. Ele tem
14 registros com casos válidos, inválidos e duplicados.

### 2.2 Passo a passo

1. Abra **Importar**.
2. Clique na área pontilhada e escolha o arquivo, ou arraste o arquivo para dentro dela.
3. A **prévia** aparece automaticamente. Nada é gravado nesta etapa.
4. Confira os dados e clique em **Processar (Extract + Transform)**.
5. O sistema abre a tela do processamento com os resultados.

### 2.3 Mensagens de arquivo inválido

| Mensagem | O que fazer |
|---|---|
| “Formato inválido: selecione um arquivo com extensão .csv” | Salve o arquivo como CSV. |
| “O arquivo está vazio” | Selecione um arquivo com conteúdo. |
| “Colunas obrigatórias ausentes: …” | Inclua as colunas indicadas no cabeçalho. |
| “O arquivo possui cabeçalho, mas nenhum registro de dados” | Adicione linhas de dados abaixo do cabeçalho. |
| “O arquivo não parece ser um CSV de texto” | O arquivo é binário (ex.: `.xlsx` renomeado). Exporte como CSV no Excel. |
| “O arquivo excede o tamanho máximo permitido” | Divida o arquivo ou peça para aumentar o limite. |

## 3. Visualização dos dados importados (prévia)

A prévia mostra:

- O nome do arquivo, a quantidade de registros extraídos, o delimitador detectado e as colunas encontradas.
- As primeiras 50 linhas **exatamente como estão no arquivo**, sem correções.
- Na coluna **Estrutura**, “OK” ou o problema da linha (ex.: “esperado 4 colunas, encontrado 3”).
- Avisos, como colunas extras ignoradas ou linhas em branco descartadas.

## 4. Funcionamento das etapas ETL

### Extract (extração)

O arquivo é lido e cada linha vira um registro com seu número de linha original. O cabeçalho
é a linha 1. Linhas com quantidade de colunas diferente do cabeçalho ficam marcadas.

### Transform (transformação e validação)

Cada registro é padronizado:

| Campo | Exemplo original | Após a transformação |
|---|---|---|
| Nome | `  maria   DA silva ` | `Maria da Silva` |
| E-mail | ` MARIA@Exemplo.COM ` | `maria@exemplo.com` |
| Telefone | `+55 (31) 98765-4321` | `31987654321` |
| Cidade | `SÃO PAULO` | `São Paulo` |

Em seguida, o registro passa pelas validações:

| Validação | Reprova quando |
|---|---|
| Estrutura | A linha tem mais ou menos colunas que o cabeçalho. |
| Campos obrigatórios | Nome, e-mail, telefone ou cidade estão vazios. |
| Formato do e-mail | O e-mail não segue o formato `nome@dominio.com`. |
| Formato do telefone | Não tem DDD + 8 dígitos (fixo) ou DDD + 9 dígitos começando com 9 (celular). |
| Duplicidade | O e-mail repete um registro válido anterior do arquivo ou já existe no banco. |

Todos os problemas de um registro são informados, não só o primeiro.

### Load (carga)

Na tela do processamento, clique em **Carregar válidos no banco (Load)**. Somente os registros
**válidos** são gravados, com os dados já corrigidos. Ao final, o sistema informa quantos
foram armazenados. Um processamento só pode ser carregado uma vez. Se ocorrer qualquer
erro, nada é gravado.

## 5. Interpretação dos resultados

A tela do processamento mostra:

- **Cards**: extraídos, válidos, inválidos, duplicados e carregados no banco.
- **Etapas ETL**: “Aguardando carga” antes do Load, e as três concluídas depois.
- **Tabela de registros**, com filtros **Todos / Válido / Inválido / Duplicado**:
  - a coluna *Motivo / observação* explica cada rejeição;
  - o botão ⇄ abre os **dados originais lado a lado com os transformados** e o resultado de cada validação.

Exemplo com o arquivo de demonstração: **14 extraídos, 6 válidos, 6 inválidos, 2 duplicados**.
Depois da carga: **6 carregados**.

Por que um registro **válido** pode virar **duplicado** na carga? Se outra importação com o
mesmo e-mail foi carregada antes, o sistema impede a duplicação no banco e reclassifica o
registro, informando o cliente já existente.

## 6. Consulta dos dados armazenados

1. Abra **Clientes**.
2. Digite parte do nome ou do e-mail no campo de pesquisa. A lista é filtrada enquanto você digita, e o botão **Pesquisar** também funciona.
3. Clique em **Detalhes** para ver o cliente completo, inclusive a data de criação.

A pesquisa não diferencia maiúsculas de minúsculas.

## 7. Exportação dos dados

A área **Exportação** aparece na tela de cada processamento, com três opções. Clique no botão
desejado e o arquivo é baixado na hora. Todos os arquivos usam UTF-8 com BOM, o que ajuda o
Excel a reconhecer os acentos.

### 7.1 Diferenças entre as modalidades

| | **CSV tratado** | **CSV de rejeitados** | **CSV completo** |
|---|---|---|---|
| Registros incluídos | Somente válidos | Inválidos e duplicados | Todos |
| Valores dos campos | **Corrigidos** (transformados) | **Originais** | **Originais** |
| Colunas adicionais | n/a | `numero_linha`, `tipo_rejeicao`, `motivo_rejeicao` | `numero_linha`, `situacao`, `resultado_validacao` |
| Nome do arquivo | `tratado_proc{N}.csv` | `rejeitados_proc{N}.csv` | `completo_proc{N}.csv` |

### 7.2 Exemplos de utilização

**CSV tratado: usar os dados limpos em outro sistema.**
Você precisa enviar a lista de clientes para uma ferramenta de e-mail marketing. O CSV tratado
traz só registros válidos, sem duplicidade e já padronizados:

```csv
nome,email,telefone,cidade
Maria da Silva,maria.silva@exemplo.com,31987654321,Belo Horizonte
"Oliveira, Beatriz",beatriz.oliveira@exemplo.com,51988887777,Porto Alegre
```

**CSV de rejeitados: corrigir na origem.**
Devolva o arquivo para a equipe que gerou os dados. Ele mostra o valor exatamente como foi
enviado, a linha onde está e o motivo:

```csv
numero_linha,nome,email,telefone,cidade,tipo_rejeicao,motivo_rejeicao
4,ana costa,ana.costa@exemplo,(21) 99876-5432,rio de janeiro,VALIDACAO,E-mail inválido: 'ana.costa@exemplo'
7,Maria S. Silva,maria.silva@exemplo.com,31 98765 4321,Belo Horizonte,DUPLICIDADE,E-mail duplicado: já informado na linha 2
```

Corrija as linhas e importe um novo arquivo só com elas. `tipo_rejeicao` separa erros de
dados (`VALIDACAO`) de repetições (`DUPLICIDADE`).

**CSV completo: auditoria e conferência.**
Para documentar tudo o que aconteceu na importação, o CSV completo lista cada linha original
com sua situação e o resultado de cada validação:

```csv
numero_linha,nome,email,telefone,cidade,situacao,resultado_validacao
2,  maria da silva  ,MARIA.SILVA@EXEMPLO.COM,(31) 98765-4321,belo horizonte,VALIDO,Todas as validações foram aprovadas. ESTRUTURA=APROVADA; ...
4,ana costa,ana.costa@exemplo,(21) 99876-5432,rio de janeiro,INVALIDO,Validação reprovada. ...; EMAIL_FORMATO=REPROVADA (E-mail inválido: 'ana.costa@exemplo'); ...
```

### 7.3 Abrindo no Excel

Dê dois cliques no arquivo. Se as colunas aparecerem juntas em uma só, use
**Dados → De Texto/CSV**, escolha o delimitador **Vírgula** e a origem **UTF-8**. Outra opção
é pedir ao administrador para configurar o separador `;` (`APP_EXPORT_DELIMITER=;`).

## 8. Consulta do histórico de processamento

1. Abra **Histórico**.
2. A tabela lista cada importação: número, arquivo, data e hora, quantidades e status (*Processado* ou *Carregado*).
3. Clique em **Abrir** para ver os registros, carregar os válidos (se ainda não carregados) e **exportar os três CSVs**.

As exportações de importações antigas continuam disponíveis mesmo depois de reiniciar a
aplicação, porque o sistema guarda os dados originais e os resultados de cada registro.

## 9. Alternância entre modo claro e escuro

Clique no ícone de **lua** (para o modo escuro) ou de **sol** (para o modo claro), na barra
superior. A escolha fica salva no navegador e vale nas próximas visitas. No primeiro acesso,
o sistema segue a preferência do sistema operacional.
