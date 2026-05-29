package com.its.gestionepagamentirestclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestionepagamentirestclientApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionepagamentirestclientApplication.class, args);
    }

}
