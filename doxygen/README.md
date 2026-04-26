# Doxygen documentation

This folder contains the [Doxygen](https://www.doxygen.nl/) configuration used to
generate API reference documentation for JVM Explorer.

## Layout

- `Doxyfile` &mdash; main Doxygen configuration. Tuned for Java sources, with
  `EXTRACT_ALL = YES`, Graphviz/`dot` diagrams enabled and `README.md` used as
  the documentation main page.

## Building locally

Install Doxygen and Graphviz, then from the repository root run:

```bash
doxygen doxygen/Doxyfile
```

The HTML output is written to `docs-gen/html/`. Open `docs-gen/html/index.html`
in a browser to view the generated documentation.

## Continuous integration

- A manually-triggered GitHub Actions workflow lives at
  [`.github/workflows/doxygen.yml`](../.github/workflows/doxygen.yml). It can be
  launched from the **Actions** tab via *Run workflow* and uploads the generated
  HTML as a build artifact named `doxygen-docs`.
- The Jenkins pipeline (`Jenkinsfile`) also includes a `Generate Doxygen Docs`
  stage that produces and archives `doxygen-docs.zip` when Doxygen is available
  on the build agent.
