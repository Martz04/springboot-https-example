# Demo Server

Springboot demo application using HTTPs.

## Build

    ./gradlew clean build

## Run

    ./gradlew bootRun

## Curl with Https

     curl -k --cert server.crt --key server.key https://localhost:8080/hello