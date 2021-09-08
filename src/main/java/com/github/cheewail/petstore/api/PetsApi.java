package com.github.cheewail.petstore.api;

import com.github.cheewail.petstore.api.model.Pet;
import io.reactivex.Single;

import java.util.List;

public interface PetsApi {
    //createPets
    public Single<ApiResponse<Void>> createPets();
    //listPets
    public Single<ApiResponse<List<Pet>>> listPets(Integer limit);
    //showPetById
    public Single<ApiResponse<Pet>> showPetById(String petId);
}
