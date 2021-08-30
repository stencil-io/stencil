# build
mvn package -Pnative

docker build -f src/main/docker/Dockerfile.native -t demo-app:1 .

# run
docker run -i --rm -p 8080:8080 demo-app:1

# run via maven debug
mvn compile quarkus:dev

# mongo - start mongo and mongo express 
docker-compose -f dev-tools/demo-app/src/main/resources/docker/stack-pg.yml up -d

# mongo

http://localhost:8091/

# mongo shell
docker exec -it <CONTAINER_ID> bash

# create db via shell



## Extensions

* quarkus-stencil-sc: Static content: Release can be compiled. One service that enables a "GET" and returns "Site".
* quarkus-stencil-composer: Example of UI Integration. 
* quarkus-stencil-composer-pg: Provides backend.

## Extension configurations

```
quarkus:

  stencil-composer:
    service-path: /portal
    server-path: /q/stencil-composer-services

  stencil-composer-pg:
    service-path: /q/stencil-composer-services

    repo:
      repo-name: test-assets

  stencil-sc:
    service-path: /q/site
    site-json: site.json
    
```
    