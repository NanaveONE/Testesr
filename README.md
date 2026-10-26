# SKYrus Live Editor — interface original extraída do ZIP

Aplicativo Android independente (`com.skyrus.liveeditor`, Android 12 / API 31+).

**O editor NÃO é uma réplica redesenhada:** os recursos originais em `app/src/main/res/` foram copiados do arquivo `SKYrus-SKYmu-Principal (1) (8).zip` fornecido. Os **39 arquivos de layout XML originais** estão disponíveis na lista de cenas (telas completas e componentes), e a tela inicial, menu, pesquisa, itens de jogos, configurações, controle, drivers e outras telas são **infladas desses arquivos XML reais**, com as imagens, cores, estilos e ícones do SKYrus.

## Para testar

1. Abra o projeto no Android Studio ou envie todo o diretório para um repositório GitHub.
2. No GitHub, vá a **Actions → Build SKYrus Live Editor APK → Run workflow** e baixe o artefato `SKYrus-Live-Editor-debug-apk` da execução bem-sucedida.
3. Instale o APK no Android 12+. Não desinstala nem substitui o SKYrus.
4. Selecione uma tela na lista. Em **Editar**, toque em um elemento real do Android e arraste para mover; use os sliders para tamanho/opacidade. Em **Prévia**, toque nos comandos ligados à navegação. Componentes adicionais do ZIP também podem ser selecionados na lista como telas isoladas. Exporte/importa as transformações como JSON.

## Correspondência com o SKYrus

- `res/layout/main_activity.xml`: tela inicial, exatamente a definição do projeto enviado.
- `res/layout/dialog_home_menu.xml`, `dialog_home_search.xml`: menu e pesquisa.
- `res/layout/app_item_grid.xml`, `app_item_linear.xml`: itens de biblioteca.
- `res/layout/settings_activity.xml`, `titlebar.xml`: base da interface de configurações.
- `res/xml/{app,emulation,credits,content,input,overlay,frame_generation}_preferences.xml`: definições das preferências reais.
- `res/layout/{controller_activity,gpu_driver_activity,on_screen_edit_activity,emu_activity,activity_performance_overlay_editor}.xml`: outras telas selecionáveis.
- Todos os recursos originais foram preservados, incluindo variações para orientação paisagem, noite, drawables e strings.

### Limitações importantes

- As classes **Kotlin e código nativo** do emulador não estão executando aqui. Dados de jogos na biblioteca são demonstrativos; não são jogos instalados, e o aplicativo não emula Switch.
- As preferências personalizadas do emulador, que dependem de serviços/código Kotlin, são substituídas **somente nos XMLs gerados `preview_*_preferences.xml`** por preferências AndroidX para exibir os textos, ícones, linhas e categorias reais. Os XMLs originais continuam intactos.
- As views customizadas (`OnScreenControllerView`, `AlignmentGridView`, `FixedRatioSurfaceView`, `DragHandleView`) têm **stubs visuais**, pois sua implementação original depende do emulador. A geometria do restante da tela segue o layout real, mas os controles dentro dessas views ainda não têm toda a renderização do SKYrus.
- Elementos gerados dinamicamente pela lógica Kotlin não são 100% equivalentes. O emulador não compartilha seu estado com este editor; nenhuma configuração do SKYrus é modificada ao editar no aplicativo separado.
- A exportação JSON é um mapa de deslocamento, escala e opacidade por tela/ID de view. **Ainda é necessário adicionar suporte de importação ao SKYrus para que as edições apareçam no emulador real.** É possível modificar o aplicativo de origem mais tarde sem tornar este editor dependente dele.
- Não houve compilação nem teste em aparelho nesta sessão. O workflow está presente para CI, mas sua conclusão depende de uma execução real no GitHub.

## Origem e licença

`SOURCE-ORIGIN.txt` inclui o SHA256 do ZIP utilizado. Os recursos originais do projeto continuam sujeitos às suas licenças e atribuições. Não há alterações nos submódulos, kernel, GPU nem nas bibliotecas nativas do SKYrus.
