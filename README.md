# Dev Settings

Aplicativo Android para controlar atalhos de Opções do desenvolvedor em aparelhos Android/Samsung.

> Importante: baixar e instalar o APK não é suficiente para usar os controles principais. Depois de instalar, é necessário conceder uma permissão especial uma vez via ADB/PowerShell.

## Recursos

- Alterna o Modo desenvolvedor.
- Ao desligar o Modo desenvolvedor, também desliga a Depuração USB e a Depuração Wi-Fi.
- Mostra/oculta Depuração USB, Depuração Wi-Fi e Animações conforme o estado real do Modo desenvolvedor.
- Ajusta as escalas de animação para `0x`, `0.5x` ou `1x`.
- Atualiza o estado automaticamente enquanto o app está aberto.
- Abre o seletor nativo de app de local fictício.
- Abre as Opções do desenvolvedor completas.
- Acompanha o tema claro/escuro do sistema.

## Pacote

```text
com.gguilhem.devsettings
```

## Instalar pelo APK pronto

1. Baixe o APK pela aba **Releases** do GitHub.
2. Instale o APK no celular.
3. Ative a Depuração USB no celular.
4. Conecte o celular ao computador e aceite a autorização de depuração.
5. No PowerShell, execute:

```powershell
adb shell pm grant com.gguilhem.devsettings android.permission.WRITE_SECURE_SETTINGS
```

Sem esse comando, o app pode abrir, mas não consegue alterar Modo desenvolvedor, ADB e Animações.

Se preferir, use o script do projeto:

```powershell
.\scripts\grant-write-secure-settings.ps1
```

O script também exige que o `adb` esteja instalado e acessível no `PATH`.

## Compilar o APK pelo código

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

Depois de instalar o APK compilado, a permissão especial ainda precisa ser concedida:

```powershell
adb shell pm grant com.gguilhem.devsettings android.permission.WRITE_SECURE_SETTINGS
```

## Gerar APK automaticamente no GitHub

O repositório inclui GitHub Actions para gerar o APK:

- Em todo push/PR, o workflow de build gera um artefato chamado `DevSettings-debug-apk`.
- Ao criar uma tag `v*`, o workflow de release publica o APK na aba **Releases**.

Exemplo:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

## Observação sobre local fictício

Android não permite que um app comum selecione diretamente outro app como provedor de local fictício. Por isso, o app abre a tela nativa de Opções do desenvolvedor para a seleção manual.
