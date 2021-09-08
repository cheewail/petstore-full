package com.github.cheewail.petstore.server.repository;

import com.github.cheewail.petstore.server.entity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<PetEntity, Long> {

}
