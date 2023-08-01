# Demo Server

Springboot demo application using HTTPs and SSL commands.

## Build

    ./gradlew clean build

## Run

    ./gradlew bootRun


## OpenSSL commands

Generate a private key using genrsa
```
openssl genrsa -des3 -out server.key 2048
```

Generate a CSR (Certifcate Signing Request)
```
openssl req -new  -subj "/C=MX/CN=localhost" \
    -addext "subjectAltName = DNS:localhost, IP:127.0.0.1" \
    -key server.key -out server.csr
```

Self signing the CSR
```
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
```

Convert from .crt to .pem format
```
openssl x509 -in server.crt -out server.pem -outform PEM
```

Concatenate private key and signed certificate into one PEM file
```
cat server.pem server.key > server.fullchain.pem
```

Generate PKCS12 keystore with alias of the server url
```
openssl pkcs12 -export -in server.fullchain.pem -out server.p12 -name server-cert -noiter -nomaciter
```

## Keytool commands
Convert p12 into jks keystore format (Java)
```
keytool -importkeystore -srckeystore server.fullchain.p12 -srcstoretype pkcs12 -srcalias server-cert -srcstorepass password -destkeystore server.jks -deststoretype jks
```

Import server certificate into client truststore
```
keytool -import -alias server-cert -file server.crt -keypass password -keystore client.jks -storepass password
```

Convert JKS to PKCS12 format
```
keytool -importkeystore -srckeystore client.jks -destkeystore client.p12 -deststoretype pkcs12
```

## Curl with Https

     curl -k --cert server.crt --key server.key https://localhost:8080/hello
