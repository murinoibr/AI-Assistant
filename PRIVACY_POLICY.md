# Política de Privacidade — AI Assistant

**Data de vigência:** 27 de setembro de 2026  
**Versão:** 2.0

Esta Política de Privacidade explica como o aplicativo **AI Assistant** (pacote Android `com.murinoi.aiassistant`, “Aplicativo”) trata informações. O Aplicativo permite conversar, por texto e voz, com agentes de inteligência artificial e criar agentes personalizados em português.

> **Importante:** esta política foi preparada com base no código atualmente disponível neste repositório. O desenvolvedor deve revisar e atualizar o documento se adicionar anúncios, analytics, login, pagamentos, Firebase, coleta de localização, câmera, contatos ou qualquer outro SDK/recurso.

## 1. Responsável e contato

**Responsável pelo tratamento:** Murino IBR  
**Desenvolvedor:** murinoibr  
**E-mail para privacidade e suporte:** `SUBSTITUA_PELO_SEU_EMAIL`  
**País/endereço do responsável:** `SUBSTITUA_PELO_SEU_PAÍS_E_ENDEREÇO_SE_APLICÁVEL`

O e-mail e, quando aplicável, a identificação do responsável devem ser preenchidos antes da publicação no Google Play Console.

## 2. Dados tratados

### 2.1 Mensagens e conteúdo fornecido pelo usuário

Quando você usa o Aplicativo, podemos tratar:

- mensagens digitadas;
- transcrições ou textos resultantes de recursos de voz, quando você os utiliza;
- instruções, descrições e prompts fornecidos para criar agentes;
- nomes, descrições, categorias, emojis e instruções de sistema dos agentes personalizados;
- respostas geradas pelos agentes;
- mensagens de erro exibidas pelo Aplicativo, quando incluírem informações relacionadas à solicitação.

Não envie senhas, chaves de API, dados bancários, documentos de identificação, informações médicas ou outros dados sensíveis nas conversas. O Aplicativo não precisa desses dados para funcionar.

### 2.2 Dados armazenados localmente no dispositivo

O Aplicativo usa um banco de dados local Android Room, chamado `vozia_database`, para armazenar:

- agentes padrão e agentes personalizados;
- nome, descrição, prompt, emoji, categoria e data de criação dos agentes;
- mensagens, remetente, agente associado e horário das mensagens.

Esses dados são armazenados no dispositivo e não exigem criação de conta. O Android pode incluir dados do Aplicativo em backup ou transferência do dispositivo conforme as configurações do sistema, pois as regras atuais permitem backup do banco de dados e das preferências.

Você pode apagar as mensagens do agente pela função de limpeza disponível no Aplicativo e excluir agentes personalizados pela função de exclusão. Para apagar completamente os dados locais, desinstale o Aplicativo ou use **Configurações do Android > Aplicativos > AI Assistant > Armazenamento > Limpar dados**, observadas as limitações de backups do Android.

### 2.3 Dados técnicos

Para realizar comunicações de rede, o Aplicativo e os provedores de infraestrutura podem receber dados técnicos normalmente incluídos em conexões HTTPS, como endereço IP, data e hora, informações do dispositivo, sistema operacional, versão do Aplicativo, status da rede e registros técnicos de requisições. O Aplicativo não implementa, no código analisado, uma conta própria, perfil de usuário, publicidade, analytics próprio ou coleta deliberada de localização, contatos ou câmera.

## 3. Microfone, voz e síntese de fala

O Aplicativo declara a permissão Android `RECORD_AUDIO`. O microfone é utilizado somente quando você inicia uma função de entrada por voz que o solicite. A permissão pode ser negada ou revogada nas configurações do Android; nesse caso, os recursos de voz podem não funcionar, mas o chat por texto continua disponível quando tecnicamente possível.

O texto obtido da voz pode ser tratado como mensagem e enviado aos provedores de IA descritos na seção 4. A implementação também usa a função de síntese de fala do Android (`TextToSpeech`) para ler respostas em voz alta. A síntese é executada pelo mecanismo de voz instalado no dispositivo; o Aplicativo não precisa gravar ou armazenar o áudio da resposta.

O Aplicativo não acessa o microfone em segundo plano e não coleta áudio continuamente. O usuário deve verificar as permissões exibidas pelo Android e as políticas do mecanismo de reconhecimento de voz escolhido no dispositivo.

## 4. Provedores de inteligência artificial

Para gerar respostas, o Aplicativo pode enviar a mensagem atual, o prompt do agente e até as últimas dez mensagens da conversa aos seguintes serviços, conforme o agente e a configuração utilizada:

- **Google Gemini API**, por meio de `generativelanguage.googleapis.com`;
- **OpenCode**, por meio de `opencode.ai`, usando o modelo configurado no Aplicativo;
- **OpenAI API**, por meio de `api.openai.com`, caso essa integração seja ativada ou utilizada em uma versão do Aplicativo.

Os dados enviados podem incluir o conteúdo integral das mensagens selecionadas, prompts de sistema e descrições de agentes. Esses provedores podem tratar os dados de acordo com seus próprios termos e políticas de privacidade. Consulte, antes de usar o serviço, as políticas vigentes de Google, OpenCode e OpenAI sobre retenção, segurança e utilização de dados enviados à API.

O responsável pelo Aplicativo não deve enviar dados sensíveis ou informações de terceiros sem autorização. As respostas são geradas automaticamente, podem estar incorretas e não constituem aconselhamento profissional.

## 5. Finalidades e bases legais

Tratamos os dados para:

1. fornecer o chat e as funcionalidades de criação e gerenciamento de agentes;
2. encaminhar solicitações aos provedores de IA escolhidos;
3. armazenar o histórico localmente para que você possa consultar a conversa;
4. converter entrada e saída de texto em voz quando solicitado;
5. manter a segurança, prevenir abuso e diagnosticar falhas técnicas;
6. cumprir obrigações legais e responder a solicitações legítimas de autoridades.

Quando a LGPD for aplicável, as bases legais poderão incluir execução do serviço solicitado pelo usuário, legítimo interesse em segurança e funcionamento, cumprimento de obrigação legal e consentimento quando exigido, especialmente para a permissão de microfone. Na União Europeia, as bases correspondentes poderão incluir execução de contrato, interesse legítimo, consentimento e obrigação legal.

## 6. Compartilhamento

Não vendemos dados pessoais e não compartilhamos conversas para publicidade comportamental. Podemos compartilhar ou permitir acesso aos dados estritamente necessário com:

- provedores de IA indicados na seção 4;
- provedores de infraestrutura e conectividade envolvidos na transmissão das requisições;
- autoridades públicas, quando houver obrigação legal, ordem judicial ou necessidade de proteger direitos e segurança;
- sucessores ou assessores em uma operação societária, sujeitos às obrigações legais aplicáveis.

O Aplicativo não contém, na versão analisada, SDK de anúncios, Google Analytics, Firebase Analytics ou login social ativo. Se isso mudar, esta política e a declaração **Data safety** do Google Play serão atualizadas antes da distribuição da versão correspondente.

## 7. Transferências internacionais

Os provedores de IA e infraestrutura podem processar dados em servidores localizados fora do Brasil ou do país de residência do usuário. Essas transferências são necessárias para fornecer o serviço de IA. Quando aplicável, serão adotadas salvaguardas exigidas pela LGPD, GDPR ou legislação local, incluindo mecanismos contratuais ou outras garantias reconhecidas.

## 8. Retenção e exclusão

- **Histórico e agentes:** permanecem no armazenamento local até que você os exclua, limpe os dados do Aplicativo ou desinstale o Aplicativo.
- **Dados em trânsito para IA:** são enviados para processar a solicitação e ficam sujeitos aos prazos e práticas do respectivo provedor de IA.
- **Dados técnicos:** podem ser mantidos pelo desenvolvedor ou provedores pelo tempo necessário para segurança, suporte, prevenção de fraude e cumprimento legal.
- **Backups Android:** podem manter dados locais conforme as configurações de backup do sistema, mesmo após a exclusão local, até que o sistema substitua ou elimine o backup.

Como não há conta de usuário no código analisado, não existe exclusão remota vinculada a uma conta. Para solicitar orientação ou exclusão de dados sob controle do desenvolvedor, envie uma solicitação ao e-mail indicado na seção 1.

## 9. Segurança

Utilizamos HTTPS/TLS para as comunicações de rede e controles de acesso dos provedores utilizados. Os históricos são armazenados no armazenamento privado do Aplicativo, protegido pelo modelo de segurança do Android. Nenhum método de transmissão ou armazenamento é absolutamente seguro; portanto, não podemos garantir segurança total.

**Chaves de API:** o desenvolvedor não deve distribuir chaves privadas em código público, APK ou repositório. Chaves expostas devem ser revogadas e substituídas imediatamente. O usuário nunca deve inserir uma chave pessoal em local acessível a terceiros.

## 10. Direitos do usuário

Dependendo da legislação aplicável, você pode solicitar confirmação e acesso aos dados, correção, anonimização, eliminação, portabilidade, informação sobre compartilhamento, oposição ou revisão de tratamentos automatizados e revogação do consentimento, quando aplicável.

Para exercer seus direitos, envie um e-mail para `SUBSTITUA_PELO_SEU_EMAIL`, informando o pedido e um meio seguro de contato. Poderemos solicitar informações razoáveis para confirmar a identidade e proteger os dados contra solicitações fraudulentas. Também é possível reclamar à autoridade competente, como a ANPD no Brasil ou a autoridade de proteção de dados da sua região.

## 11. Crianças

O Aplicativo não é direcionado a crianças menores de 13 anos, ou à idade mínima aplicável na jurisdição do usuário. Não coletamos intencionalmente dados pessoais de crianças. Pais ou responsáveis que identifiquem um envio indevido devem entrar em contato pelo e-mail da seção 1 para solicitar a exclusão.

## 12. Permissões Android

A versão analisada declara:

- `INTERNET`: comunicação com os serviços de IA;
- `ACCESS_NETWORK_STATE`: verificação do estado da conexão;
- `RECORD_AUDIO`: entrada por voz, somente mediante ação e autorização do usuário.

Não são declaradas permissões de localização, câmera, contatos, SMS, telefone ou armazenamento externo. O Aplicativo pode funcionar sem microfone para os recursos de texto.

## 13. Cookies e rastreamento

O Aplicativo não utiliza cookies de navegador nem tecnologias próprias de rastreamento para publicidade na versão analisada. As requisições de rede podem conter informações técnicas padrão de protocolo, como IP e cabeçalhos necessários à comunicação. As políticas dos provedores externos podem tratar identificadores e logs conforme seus próprios documentos.

## 14. Serviços e links de terceiros

O Aplicativo utiliza bibliotecas Android, Kotlin, Retrofit, OkHttp, Moshi e Room para funcionamento técnico. Bibliotecas podem coletar dados somente conforme suas funções e configurações; não foram identificados SDKs ativos de anúncios ou analytics no código analisado.

As políticas dos serviços externos podem ser alteradas sem controle do desenvolvedor. O usuário deve consultar os documentos atuais de cada provedor antes de enviar conteúdo.

## 15. Alterações desta política

Podemos atualizar esta Política para refletir alterações no Aplicativo, nos provedores, na legislação ou nas práticas de tratamento. A versão atualizada será publicada no mesmo endereço, com nova data de vigência. Alterações relevantes poderão ser comunicadas dentro do Aplicativo ou por outro meio razoável.

## 16. Legislação e contato

Esta política será interpretada conforme a legislação aplicável ao tratamento de dados e ao local de residência do usuário, sem limitar direitos obrigatórios previstos em lei.

**E-mail de privacidade:** `SUBSTITUA_PELO_SEU_EMAIL`  
**Responsável:** Murino IBR  
**Última atualização:** 27 de setembro de 2026

---

## Checklist antes do envio ao Google Play Console

- [ ] Substituir o e-mail e a identificação/endereço do responsável.
- [ ] Publicar esta política em uma URL HTTPS pública; um repositório privado do GitHub não serve como página pública para usuários.
- [ ] Confirmar no Play Console que a seção **Data safety** corresponde exatamente à versão distribuída.
- [ ] Confirmar quais provedores de IA estão realmente ativos em produção.
- [ ] Revogar e substituir as chaves de API que foram incluídas no código ou em commits, pois credenciais expostas não devem permanecer válidas.
- [ ] Confirmar no dispositivo o comportamento real do reconhecimento de voz e informar o provedor efetivamente usado.
- [ ] Fazer revisão jurídica, especialmente se o Aplicativo for disponibilizado para menores, na União Europeia ou para usuários sujeitos à LGPD.
