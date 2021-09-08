package com.github.cheewail.petstore.server.impl;

import com.github.cheewail.petstore.api.ApiResponse;
import com.github.cheewail.petstore.api.PetsApi;
import com.github.cheewail.petstore.server.entity.PetEntity;
import com.github.cheewail.petstore.api.model.Pet;
import com.github.cheewail.petstore.server.service.PetService;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MyPetsApiImpl implements PetsApi {

    private static final Logger logger = LoggerFactory.getLogger(MyPetsApiImpl.class);

    @Autowired
    private PetService petService;

    @Autowired
    private ModelMapper modelMapper;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    public Single<ApiResponse<Void>> createPets() {
        return petService.createPets(convertToEntity(new Pet()))
                .map(e -> new ApiResponse<>(201));
    }

    public Single<ApiResponse<List<Pet>>> listPets(Integer limit) {
        return petService.listPets(limit)
                .map(e -> e.stream().map(this::convertToDto).collect(Collectors.toList()))
                .map(list -> new ApiResponse<>(200, list));
    }

    public Single<ApiResponse<Pet>>showPetById(String petId) {
        try {
            return petService.showPetById(Long.valueOf(petId))
                    .map(pet -> pet.isPresent() ?
                            new ApiResponse<>(200, convertToDto(pet.get())) :
                            new ApiResponse<>(404));
        } catch (Exception e) {
            if (e instanceof NumberFormatException) {
                logger.info("NumberFormatException: " + petId);
            }
            return Single.just(new ApiResponse<>(500));
        }
    }

    private Pet convertToDto(PetEntity petEntity) {
        Pet pet = modelMapper.map(petEntity, Pet.class);
        return pet;
    }

    private PetEntity convertToEntity(Pet pet) {
        PetEntity petEntity = modelMapper.map(pet, PetEntity.class);
        return petEntity;
    }

}
