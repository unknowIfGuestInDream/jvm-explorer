# Java Doxygen Configuration {#java-doxygen-configuration}

The Doxygen configuration is optimized for this Java project with the following
settings.

## Java output mode

`OPTIMIZE_OUTPUT_JAVA = YES` enables Java-oriented output naming and navigation.

## Source mapping

`EXTENSION_MAPPING = java=Java` explicitly maps `.java` files to Doxygen's Java
parser.

## Javadoc behavior

`JAVADOC_AUTOBRIEF = YES` treats the first sentence of a Javadoc block as the
brief description, matching common Java documentation style.

## Complete extraction

The following settings make generated documentation complete even when a class
does not yet have hand-written Javadoc:

- `EXTRACT_ALL = YES`
- `EXTRACT_PRIVATE = YES`
- `EXTRACT_PACKAGE = YES`
- `EXTRACT_STATIC = YES`
- `EXTRACT_LOCAL_CLASSES = YES`
- `EXTRACT_LOCAL_METHODS = YES`

This keeps every class and member visible in the generated API reference while
allowing richer Javadoc comments to be added incrementally.
