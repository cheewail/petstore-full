package com.github.cheewail.petstore.server.service;

import com.github.cheewail.petstore.server.entity.PetEntity;
import com.github.cheewail.petstore.server.repository.PetRepository;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    public Single<List<PetEntity>> listPets(Integer limit) {
        return Single.just(petRepository.findAll());
    }

    public Single<PetEntity> createPets(PetEntity petEntity) {
        return Single.just(petRepository.save(petEntity));
    }

    public Single<Optional<PetEntity>> showPetById(Long id) {
        return Single.just(petRepository.findById(id));
    }

}
