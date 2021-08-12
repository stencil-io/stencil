# build
mvn package -Pnative

docker build -f src/main/docker/Dockerfile.native -t zoe-demo-app:1 .

# run
docker run -i --rm -p 8080:8080 zoe-demo-app:1

# run via maven debug
mvn compile quarkus:dev

# mongo - start mongo and mongo express 
docker-compose -f zoe-dev-tools/demo-app/src/main/resources/docker/stack.yml up -d

# mongo

http://localhost:8091/

# mongo shell
docker exec -it <CONTAINER_ID> bash

# create db via shell
mongosh
use admin
db.auth("root", "example")
db.createUser( { user: "appUser", pwd: "123456", roles: [ { role: "readWrite", db: "test-assets" } ] } )


## Extensions

* quarkus-zoe-sc: Static content: Release can be compiled. One service that enables a "GET" and returns "Site".
* quarkus-zoe-ide: Example of UI Integration. 
* quarkus-zoe-ide-services: Provides backend for zoe-ide.

## Extension configurations

```
quarkus:

  zoe-ide:
    service-path: /portal
    server-path: /q/zoe-ide-services

  zoe-ide-services:
    service-path: /q/zoe-ide-services
    db:
      db-name: test-assets
      connection-url: mongodb://appUser:123456@localhost:27017/test-assets?authSource=test-assets
    repo:
      repo-name: test-assets

  zoe-sc:
    service-path: /q/site
    site-json: site.json
    
```
    