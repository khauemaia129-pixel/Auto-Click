# Auto Clicker Mod (Fabric 1.21.1) - Farm de Slime / Magma Cube

Mod client-side que ataca os mobs escolhidos em **rajadas com tempos aleatórios** e religa o Auto Sell do servidor quando o inventário trava cheio.

> O código não foi compilado nem testado em jogo por quem gerou. Se o build falhar, veja "Problemas comuns".

---

## 1. O que o mod faz

### Ciclo de farm
1. Fica **esperando** um tempo aleatório (padrão 30 a 120 s).
2. Se houver mob selecionado dentro da distância, faz uma **rajada**: segura a tecla (Shift), espera um instante, e bate em intervalos aleatórios por 2 a 5 s.
3. Solta a tecla e volta a esperar um novo tempo aleatório.
4. **Se não houver mob, não ataca**: confere de novo a cada 1-2 s até aparecer.
5. Tudo é sorteado de novo a cada rajada/golpe: espera, duração da rajada, intervalo entre golpes, atraso antes do primeiro golpe.

### Auto Sell automático (sem ler o chat)
- Olha os 36 slots do inventário.
- Se estiver **cheio e parado** (nenhum item entrou nem saiu) por **15 s** (configurável), espera um **atraso aleatório de 5 a 50 s** (configurável) e então envia `/vender auto`.
- Se durante o atraso entrar ou sair item (ou abrir espaço), cancela o envio.
- Se o inventário enche e esvazia o tempo todo (Auto Sell funcionando), nada é enviado.
- Envia **uma vez** e só envia de novo depois que o inventário voltar a ter espaço.
- Aparece um aviso local na tela quando envia (não vai para o chat do servidor).

### Câmera (aba "Config", coluna direita)
- **Olhar para o mob**: antes de cada golpe a câmera gira aos poucos até o mob (passo proporcional ao erro, velocidade com variação e um pouco de ruído). Só bate quando a mira estiver a menos de 6° do mob. Slider **Câmera (°/t)** = velocidade máxima de giro por tick.
- **Mexer câmera esperando**: durante a espera entre rajadas, a cada 3 a 15 s a câmera faz um movimento pequeno e suave (principalmente horizontal). Slider **Amplitude (°)** = tamanho máximo. Não mexe com uma tela aberta.

### Proteções (aba "Proteção", cada uma liga/desliga)
- **Não religar se cair rápido**: se o Auto Sell travar de novo menos de **3 min** (configurável) depois de você ter religado, o mod entende que alguém desligou (moderador) e **não envia o comando**. Mostra um aviso na tela. O farm continua.
- **Sair se for puxado**: se a posição mudar mais que **3 blocos** (configurável) de um tick para o outro, o mod **desliga e desconecta do servidor**. Queda longa e elytra não disparam, porque o deslocamento é comparado com a velocidade do jogador. Trocar de dimensão e respawn são ignorados.

- **Alertar se citarem meu nick**: lê as mensagens de chat que o cliente recebe (só leitura, não envia nada). Se seu nick aparecer (palavra inteira, sem diferenciar maiúscula), avisa:
  - texto vermelho na tela repetido a cada 1 s por 10 s, com o trecho da mensagem;
  - 6 bipes (som de level up) no começo;
  - com **Pausar farm ao alertar** ligado, o mod desliga (solta o Shift). **M** retoma.
  - No máximo 1 alerta a cada 30 s. Mensagens suas são ignoradas. Só dispara com o mod ligado.

- **Disfarçar cliente (vanilla)**: ao entrar no servidor, o cliente informa a "marca" (brand). O Fabric informa `fabric`; com essa opção o mod informa `vanilla`. Vale **no próximo login** (reconecte depois de mudar). Vem ligada por padrão. Aparece como "vanilla" no F3.

### Outros
- Mobs padrão: **Slime** e **Magma Cube** (dá para marcar outros).
- Lista de ignorar (Player, Villager, Iron Golem etc.). Só tem efeito sobre algo que também esteja selecionado.
- Tecla segurada: SHIFT, CTRL, ALT ou NONE. Ela só fica pressionada durante a rajada.
- Aviso na tela ao ligar/desligar com **M**.

### Valores padrão
| Item | Padrão |
|---|---|
| Mobs | Slime, Magma Cube |
| Distância | 8 blocos |
| Golpe (intervalo) | 8 a 16 ticks (0,4 a 0,8 s) |
| Rajada | 2 a 5 s |
| Espera entre rajadas | 30 a 120 s |
| Tecla segurada | ligada, SHIFT |
| Auto Sell automático | ligado, cheio por 15 s |
| Atraso antes de enviar | 5 a 50 s (sorteado) |
| Não religar se cair rápido | ligado, 3 min |
| Sair se for puxado | ligado, 3 blocos |
| Olhar para o mob | ligado, até 8°/tick |
| Mexer câmera esperando | ligado, até 12° |
| Alertar se citarem meu nick | ligado, pausa o farm |
| Comando | `/vender auto` (fixo no código: `SELL_COMMAND` em `AutoClickerConfig.java`) |

### Limitações
- As configurações **não são salvas**: voltam ao padrão ao fechar o jogo.
- Não lê o chat, não usa `/ap`, não tem estatísticas nem log em arquivo.
- Se `/vender auto` for um **liga/desliga** no seu servidor e o inventário travar por outro motivo (item que não vende), o envio pode **desligar** o Auto Sell em vez de ligar. Por isso ele envia só uma vez por travamento.
- Se o inventário está cheio de pilhas 64 e o Auto Sell está desligado, a detecção funciona. Se itens ainda estão entrando (empilhando), o contador de 15 s recomeça.
- O ataque é enviado direto ao mob mais próximo, sem mover a câmera.
- A detecção de teleporte olha só a posição do seu personagem. Se algo legítimo te mover mais que o limite num tick (ex: portal sem trocar de dimensão, ender pearl, plugin do servidor), ele vai desconectar. Desligue a opção ou aumente a distância se isso acontecer.
- O alerta de chat não avisa fora do Minecraft: precisa do jogo aberto e com som. Mensagens do servidor que tragam seu nick (ex: aviso de venda) também disparam; se acontecer, desligue a opção.
- Com "Olhar para o mob" desligado, o movimento da câmera esperando vira um desvio aleatório sem correção.
- O disfarce de cliente só troca a marca. O servidor ainda pode perceber mods por outros meios (canais de rede do Fabric API, sondagem de nomes de teclas/traduções por plugins, comportamento). Não é garantia de invisibilidade.
- Distância de ataque padrão é 8 blocos, mas o alcance de ataque do Minecraft é cerca de 3 blocos. Golpes além disso não acertam no servidor. Prefira 3 a 3,5.
- Não há indicador na tela de "batendo/esperando".

---

## 2. Como instalar

Requisitos: Minecraft Java **1.21.1**, **Fabric Loader** 0.16.x, **Fabric API** `0.102.0+1.21.1`, Java 21.

1. Instale o Fabric Loader para 1.21.1 (fabricmc.net/use) e abra o perfil Fabric no launcher.
2. Coloque o `.jar` do Fabric API na pasta `mods`.
3. Coloque o `.jar` deste mod (`auto-clicker-mod-1.0.0.jar`) na mesma pasta.
4. Abra o jogo com o perfil Fabric.

Pasta `mods`: Windows `%appdata%\.minecraft\mods` | Linux `~/.minecraft/mods` | macOS `~/Library/Application Support/minecraft/mods`.

O mod é **só client**.

---

## 3. Como usar

| Tecla | Ação |
|---|---|
| **K** | Abre a configuração |
| **M** | Liga/desliga |

Dá para mudar em Opções > Controles > "Auto Clicker Mod".

1. Entre no mundo e aperte **K**.
2. Aba **Mobs**: confira Slime e Magma Cube marcados.
3. Aba **Config** (esquerda):
   - **Distância**: alcance em blocos.
   - **Golpe mín/máx (t)**: intervalo entre golpes (20 ticks = 1 s).
   - **Rajada mín/máx (s)**: quanto tempo bate em cada rajada.
   - **Espera mín/máx (s)**: pausa entre rajadas.
4. Aba **Config** (direita):
   - **Segurar tecla** + botão **Tecla** (clique alterna SHIFT/CTRL/ALT/NONE).
   - **Olhar para o mob** + **Câmera (°/t)**.
   - **Mexer câmera esperando** + **Amplitude (°)**.
5. Aba **Proteção**:
   - Esquerda (Auto Sell): **Auto Sell automático**, **Cheio por (s)**, **Atraso mín/máx (s)**.
   - Direita (Moderador): **Não religar se cair rápido** + **Janela (min)**, **Sair se for puxado** + **Distância (bl)**.
   - Esquerda embaixo (Chat): **Alertar se citarem meu nick**, **Pausar farm ao alertar**.
   - Direita embaixo (Cliente): **Disfarçar cliente (vanilla)** (vale no próximo login).
6. Feche e aperte **M** perto dos slimes.

Dica: Golpe máx maior que o mín dá mais variação. Se mín e máx estiverem invertidos, o mod usa o menor e o maior mesmo assim.

---

## 4. Como compilar

Requisitos: **JDK 21** e internet. O zip **não inclui o `gradlew`**; use o template oficial:

1. Em fabricmc.net/develop/template, gere um projeto para **Minecraft 1.21.1** e extraia.
2. Mantenha do template a pasta `gradle/` e os arquivos `gradlew` e `gradlew.bat`.
3. Apague o `src/` do template e copie deste zip: `src/`, `build.gradle`, `settings.gradle`, `gradle.properties`.
4. Na raiz do projeto:
   ```bash
   # Linux/macOS
   chmod +x gradlew
   ./gradlew clean build
   # Windows
   gradlew.bat clean build
   ```
5. O jar fica em `build/libs/`. Use `auto-clicker-mod-1.0.0.jar` (não o `-sources`).

Para testar sem instalar: `./gradlew runClient`.

Versões (gradle.properties): `minecraft_version=1.21.1`, `yarn_mappings=1.21.1+build.3`, `loader_version=0.16.9`, `fabric_version=0.102.0+1.21.1`. Se o Gradle reclamar de versão inexistente, use a mais recente de 1.21.1 em fabricmc.net/develop.

---

## 5. Estrutura

```
src/main/java/com/eric/mods/
├── AutoClickerMod.java            entrada principal
├── AutoClickerModClient.java      keybinds (M, K) e tick do cliente
├── AutoClickerConfig.java         configurações e valores padrão
├── AutoClickerEventListener.java  ciclo de rajadas, câmera, alvo, tecla segurada, monitor de inventário, teleporte
├── ChatAlert.java                 alerta quando citarem seu nick
├── mixin/ClientBrandMixin.java    disfarça a marca do cliente (vanilla)
└── ui/
    ├── AutoClickerScreen.java     tela com 3 abas
    ├── CheckboxWidget.java
    ├── NumericInputWidget.java    slider
    └── DropdownWidget.java        botão que alterna opções
src/main/resources/ fabric.mod.json, auto-clicker-mod.mixins.json e lang/ (en_us, pt_br)
```

Mudanças rápidas:
- **Outro comando do Auto Sell**: `SELL_COMMAND` em `AutoClickerConfig.java`.
- **Novo mob**: adicione o id em maiúsculo em `MOB_TYPES` (`AutoClickerEventListener.java`), ex: `"VEX"`.
- **Valores padrão**: campos no topo de `AutoClickerConfig.java`.

---

## 6. Problemas comuns

| Problema | O que fazer |
|---|---|
| Mod não aparece | Perfil Fabric, Fabric API em `mods`, Java 21. Veja `logs/latest.log`. |
| Erro de versão no Gradle | Ajuste `gradle.properties` (seção 4). |
| Erro de compilação em classe do Minecraft | Os nomes são Yarn 1.21.1. Confira `yarn_mappings` e rode `./gradlew clean build`. |
| Não ataca | Mod ligado (M)? Mob selecionado e dentro da distância? Pode estar na fase de espera (até 2 min por padrão). |
| Shift não segura | "Segurar tecla" marcado e tecla diferente de NONE. Só segura durante a rajada. |
| Alerta de chat falso | Algum aviso do servidor cita seu nick. Desligue "Alertar se citarem meu nick". |
| Desconectou sozinho | Foi detectado teleporte maior que o limite. Veja `logs/latest.log` e ajuste ou desligue "Sair se for puxado". |
| Não enviou `/vender auto` | Inventário precisa estar 100% cheio e sem mudar por 15 s, e "Auto Sell automático" ligado. |

Licença: MIT
