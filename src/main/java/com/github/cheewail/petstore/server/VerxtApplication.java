package com.github.cheewail.petstore.server;

import com.github.cheewail.petstore.server.verticle.ServerVerticle;
import com.github.cheewail.petstore.server.verticle.PetsApiVerticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class VerxtApplication {
    static final Logger logger = LoggerFactory.getLogger(VerxtApplication.class);

    @Autowired
    private ServerVerticle serverVerticle;

    @Autowired
    private PetsApiVerticle petsApiVerticle;

    public static void main(String[] args) {
        logger.info("Starting VerxtApplication...");
        SpringApplication.run(VerxtApplication.class, args);
    }

    @PostConstruct
    public void deployVerticle() {
        logger.info("Deploy verticle...");
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(serverVerticle);
        vertx.deployVerticle(petsApiVerticle);
    }
}
