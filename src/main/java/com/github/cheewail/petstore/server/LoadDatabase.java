package com.github.cheewail.petstore.server;

import com.github.cheewail.petstore.server.entity.PetEntity;
import com.github.cheewail.petstore.server.repository.PetRepository;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(PetRepository petRepository) {
        return args -> {
            petRepository.save(new PetEntity("Alpha", "Tag1"));
            petRepository.save(new PetEntity("Beta", "Tag2"));
            petRepository.findAll().forEach(pet -> log.info("Loaded: " + pet.toString()));
        };
    }
}
