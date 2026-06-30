# Dev Settings

Android app simples para alternar alguns atalhos de Opcoes do desenvolvedor em aparelhos Android/Samsung.

## Recursos

- Alterna Modo desenvolvedor.
- Ao desligar Modo desenvolvedor, desliga tambem Depuracao USB e Depuracao Wi-Fi.
- Mostra/oculta Depuracao USB, Depuracao Wi-Fi e Animacoes conforme o estado real do Modo desenvolvedor.
- Ajusta escalas de animacao para `0x`, `0.5x` ou `1x`.
- Atualiza o estado automaticamente enquanto o app esta aberto.
- Abre o seletor nativo de App de local ficticio.
- Abre as Opcoes do desenvolvedor completas.
- Tema claro/escuro conforme o tema do sistema.

## Pacote

```text
com.gguilhem.devsettings
```

## Build

```bash
./gradlew assembleDebug
```

No Windows:

```powershell
.\gradlew.bat assembleDebug
```

APK gerado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Instalar e conceder permissao

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant com.gguilhem.devsettings android.permission.WRITE_SECURE_SETTINGS
```

Sem `WRITE_SECURE_SETTINGS`, o app abre, mas nao consegue alterar as chaves globais do Android.

## Observacao sobre local ficticio

Android nao permite que um app comum selecione diretamente outro app como provedor de local ficticio. Por isso, o app abre a tela nativa de Opcoes do desenvolvedor para a selecao manual.
