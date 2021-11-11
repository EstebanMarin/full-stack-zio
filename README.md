# full-stack-zio-app

full-stack-zio

## Features

1. TODO list - MongoDB
1. Small Flappy Bird game with scoreboard - Postgres
1. Auth example (register, singin, signout, secured endpoint) - Postgres
1. API documentation - Swagger
1. Small chat with websockets
1. GraphQL example

### backend

1. scala
1. ZIO
1. cats-core
1. http4s
1. pureconfig
1. circe
1. swagger
1. reactivemongo
1. doobie
1. flyway
1. caliban

### frontend

1. scalajs
1. slinky (react)
1. diode
1. bootstrap
1. circe

## Production

### docker

1. `sbt stage`
1. `docker-compose up -d web`
1. open `http://localhost:8080` in browser


## DEV

### required services

- docker\
run `docker-compose up -d mongo postgres`

### js

`fastOptJS::webpack`\
`~fastOptJS`\
open `js/src/main/resources/index-dev.html` in browser

### server

`reStart`\
http://localhost:8080/

### js + server (dev conf)

Run server normally `reStart`.\
Run js: `fastOptJS::webpack` and `fastOptJS`.\
Open `js/src/main/resources/index-dev.html` in browser.\
When server changed run `reStart`.\
When js changed run `fastOptJS`.

### standalone

1. `sbt stage`
1. set `MONGO_URL_FULL_STACK_ZIO` env variable\
example: `MONGO_URL_FULL_STACK_ZIO=mongodb://test:test@localhost:27017/test`
1. set `DATABASE_URL_FULL_STACK_ZIO` env variable\
example: `DATABASE_URL_FULL_STACK_ZIO="jdbc:postgresql://localhost:5432/fullstackzio?user=test&password=test"`
1. run `./target/universal/stage/bin/app`
1. open `http://localhost:8080` in browser