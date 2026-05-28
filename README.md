# Gestione Pagamenti REST Client

Simple Spring Boot project that manages payments using REST APIs and RabbitMQ for asynchronous message handling.

## Features

* REST client integration
* RabbitMQ messaging
* Asynchronous payment processing
* JSON support
* Spring Boot backend

## Tech Stack

* Java
* Spring Boot
* RabbitMQ
* Maven

## Run the Project

```
git clone -b rabbitmq https://github.com/Shiinoqt/gestione-pagamenti-restclient.git
```

Enter the folder:

```
cd gestione-pagamenti-restclient
```

Start RabbitMQ with Docker:

```
docker run -d --hostname rabbitmq --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
```

Run the application:

```
mvn spring-boot:run
```

## Configuration

Edit:

```
src/main/resources/application.properties
```

Example:

```
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
```

