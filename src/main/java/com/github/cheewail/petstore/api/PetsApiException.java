package com.github.cheewail.petstore.api;

import com.github.cheewail.petstore.api.MainApiException;

public final class PetsApiException extends MainApiException {
    public PetsApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }

    public static final PetsApiException Pets_createPets_201_Exception = new PetsApiException(201, "Null response");
    public static final PetsApiException Pets_listPets_200_Exception = new PetsApiException(200, "A paged array of pets");
    public static final PetsApiException Pets_showPetById_200_Exception = new PetsApiException(200, "Expected response to a valid request");
    

}