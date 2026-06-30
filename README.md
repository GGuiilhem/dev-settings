# Dev Settings

Android app para controlar atalhos de Opcoes do desenvolvedor em aparelhos Android/Samsung.

> Importante: baixar e instalar o APK nao e suficiente para usar os controles principais. Depois de instalar, e necessario conceder uma permissao especial uma vez via ADB/PowerShell.

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

## Instalar pelo APK pronto

1. Baixe o APK pela aba **Releases** do GitHub.
2. Instale o APK no celular.
3. Ative a Depuracao USB no celular.
4. Conecte o celular ao computador e aceite a autorizacao de depuracao.
5. No PowerShell, execute:

```powershell
adb shell pm grant com.gguilhem.devsettings android.permission.WRITE_SECURE_SETTINGS
```

Sem esse comando, o app pode abrir, mas nao consegue alterar Modo desenvolvedor, ADB e Animacoes.

Se preferir, use o script do projeto:

```powershell
.\scripts\grant-write-secure-settings.ps1
```

O script tambem exige que o `adb` esteja instalado e acessivel no `PATH`.

## Compilar o APK pelo codigo

No Windows:

```powershell
.\gradlew.bat assembleDebug
```

Linux/macOS:

```bash
./gradlew assembleDebug
```

APK gerado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Depois de instalar o APK compilado, a permissao especial ainda precisa ser concedida:

```powershell
adb shell pm grant com.gguilhem.devsettings android.permission.WRITE_SECURE_SETTINGS
```

## Gerar APK automaticamente no GitHub

O reposititorio inclui GitHub Actions para gerar o APK:

- Em todo push/PR, o workflow de build gera um artifact chamado `DevSettings-debug-apk`.
- Ao criar uma tag `v*`, o workflow de release publica o APK na aba **Releases**.

Exemplo:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

## Observacao sobre local ficticio

Android nao permite que um app comum selecione diretamente outro app como provedor de local ficticio. Por isso, o app abre a tela nativa de Opcoes do desenvolvedor para a selecao manual.
