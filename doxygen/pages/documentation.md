# Documentation Generation {#documentation-generation}

JVM Explorer uses Doxygen to generate API documentation from the Java source
tree and selected Markdown pages.

## Local build

From the repository root, install Doxygen and Graphviz, then run:

```bash
doxygen doxygen/Doxyfile
```

The generated HTML documentation is written to:

```text
docs-gen/html
```

Open `docs-gen/html/index.html` in a browser to view the result.

## Continuous integration

- GitHub Actions provides a manual `Doxygen` workflow. It installs Doxygen and
  Graphviz, generates documentation, then uploads `docs-gen/html` as the
  `doxygen-docs` artifact.
- Jenkins provides a `Generate Doxygen Docs` stage. When the build agent has
  Doxygen installed, it archives `doxygen-docs.zip`.

## Main page

`README.md` is configured as the generated documentation home page through
`USE_MDFILE_AS_MAINPAGE = README.md`.
