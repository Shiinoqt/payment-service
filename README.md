# payment-service

Spring Boot payment service that manages payment records, persists them in MySQL, exposes HTTP endpoints, and integrates with RabbitMQ to consume payment requests and publish payment status updates back to the order domain.

## Overview

This repository is a Java 17 Spring Boot application built with Maven and prepared for Docker-based deployment. Its Maven configuration includes Spring Boot 4.0.6, Spring Data JPA, JDBC, Web MVC, validation, Actuator, AMQP, MySQL, MapStruct, dotenv support, and Log4j2 logging.

## Stack

The repository uses Java 17, Maven Wrapper, Spring Boot, Spring Data JPA, Spring Web MVC, Spring AMQP, MySQL, RabbitMQ, MapStruct, Lombok, and Log4j2. The included `Dockerfile` packages the application so it can be run in a containerized environment.

## Project structure

```text
.
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src
    ├── main
    │   ├── java/com/its/gestionepagamentirestclient
    │   │   ├── config
    │   │   ├── controller
    │   │   ├── dto
    │   │   ├── exception
    │   │   ├── logging
    │   │   ├── mapper
    │   │   ├── model
    │   │   ├── repository
    │   │   ├── security
    │   │   ├── service
    │   │   └── utility
    │   └── resources
    └── test
```
## Messaging flow

This application is intended to consume payment-related requests, process payment outcomes, and publish status updates for downstream order handling. The repository includes dedicated configuration and service classes for AMQP messaging, plus DTOs for payment request and response payloads.

A simplified flow looks like this:

1. The order service sends a payment-related request.
2. The payment service receives the message through RabbitMQ.
3. The payment service stores or updates payment data in MySQL.
4. The payment service publishes a payment result or status event.
5. The order service consumes the result and updates the related order.
