# Neo4J

This is the technical documentation for our Neo4J driver
- [Driver](Driver/Driver.md) outlines the technical details of the driver.
- [Preprocessor](Preprocessor/Preprocessor.md) is about the maven preprocessor plugin that is necessary for the Namespace parts of the driver ONLY.

# How to contribute

To everyone contributing. Please read the following carefully before contributing:

- This documentation is intended for both the developers, as well as the users of the project. Please carefully separate the information for both of these groups.

## Technical details

This documentation is written in [markdown](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet). It includes code commentary using regular code ('''java), and tooling for drawing graphs ('''puml -> PlantUML, '''dot -> GraphViz). For all supported diagrams see [here](https://shd101wyy.github.io/markdown-preview-enhanced/#/diagrams). An extended tooling support can also be used to render [latex](https://shd101wyy.github.io/markdown-preview-enhanced/#/code-chunk).

Please note that the tooling is only supported with markdown-preview-enhanced whch is only available on **Atom** or **VisualStudioCode**.
To use full graph support you need to install [GrapzViz](https://www.graphviz.org/) as well as a [latex distribution](https://shd101wyy.github.io/markdown-preview-enhanced/#/extra?id=install-latex-distribution).

Notes for CodeChunk (latex etc.):
- You have to enable scripts in the settings before this works
- The code is only executed when you use shift + enter (current snippet); ctrl + shift + enter (all snippets in file) or run the play button in the preview.[^1]

### bring it to work with VisualStudioCode
- [Tex Live](http://www.tug.org/texlive/)
  - instructions
  - includes links for [download](http://mirror.ctan.org/systems/texlive/tlnet/install-tl-windows.exe)
  installation took a real long time
- [Install pdf2svg](https://shd101wyy.github.io/markdown-preview-enhanced/#/extra?id=install-svg2pdf)
  - [pdf2svg-windows](https://github.com/jalios/pdf2svg-windows)
    - just download the zip file
    - extract the zip
    - put it wherever you want it to be (e.g.: I:/programs/pdf2vsg)
    - add **pdf2svg.exe** to **Path** I:\programs\pdf2svg\dist-64bits\pdf2svg.exe
    - restart computer
- VisualStudioCode Extensions
  - [MPE] [markdown-preview-enhanced](https://shd101wyy.github.io/markdown-preview-enhanced/#/vscode-installation)

*[MPE]: Markdown Preview Enhanced
*[PASS]: Plan Analysis using Self-learining Solutions and simulations
*[vEMOOS]: virtual Expo in the Middle Of Our Street
