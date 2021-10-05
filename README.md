[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

# The Stencil CMS


# Local Development Environment

## Prerequisites Java 11+, Maven 3.6.2+
It's recommended to install them using [SDKMAN](https://sdkman.io/install)
```
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk version
sdk install java 11.0.7-zulu
sdk install maven
```

## Creating and Running Maven Project
```
mvn archetype:generate                                  \
  -DarchetypeGroupId=io.github.jocelynmutso             \
 // -DarchetypeArtifactId=zoe-maven-archetype           \
 // -DgroupId=io.placeholder.test                       \
  -DartifactId=test-project

cd test-project

mvn clean compile quarkus:dev

```

## Quarkus extensions TODO



