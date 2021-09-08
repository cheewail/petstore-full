package com.github.cheewail.petstore.server.verticle;

import com.github.cheewail.petstore.api.ApiResponse;
import com.github.cheewail.petstore.api.MainApiException;
import com.github.cheewail.petstore.api.PetsApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PetsApiVerticle extends AbstractVerticle {
    static final Logger logger = LoggerFactory.getLogger(PetsApiVerticle.class);

    static final String CREATEPETS_SERVICE_ID = "createPets";
    static final String LISTPETS_SERVICE_ID = "listPets";
    static final String SHOWPETBYID_SERVICE_ID = "showPetById";
    
    @Autowired
    private PetsApi service;

    @Override
    public void start() {
        logger.info("PetsApiVerticle starting...");

        // Consumer for createPets
        vertx.eventBus().<JsonObject>consumer(CREATEPETS_SERVICE_ID).handler(message -> {
            try {
                String serviceId = "createPets";
                service.createPets().subscribe(
                    result -> {
                        message.reply(encodeToJsonObject(result));
                    },
                    error -> {
                        manageError(message, error, "createPets");
                    });
            } catch (Exception e) {
                logUnexpectedError("createPets", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        // Consumer for listPets
        vertx.eventBus().<JsonObject>consumer(LISTPETS_SERVICE_ID).handler(message -> {
            try {
                String serviceId = "listPets";
                String limitParam = message.body().getString("limit");
                Integer limit = (limitParam == null) ? null : Json.decodeValue(limitParam, Integer.class);
                service.listPets(limit).subscribe(
                    result -> {
                        message.reply(encodeToJsonObject(result));
                    },
                    error -> {
                        manageError(message, error, "listPets");
                    });
            } catch (Exception e) {
                logUnexpectedError("listPets", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        // Consumer for showPetById
        vertx.eventBus().<JsonObject>consumer(SHOWPETBYID_SERVICE_ID).handler(message -> {
            try {
                String serviceId = "showPetById";
                String petIdParam = message.body().getString("petId");
                if(petIdParam == null) {
                    manageError(message, new MainApiException(400, "petId is required"), serviceId);
                    return;
                }
                String petId = petIdParam;
                service.showPetById(petId).subscribe(
                    result -> {
                        message.reply(encodeToJsonObject(result));
                    },
                    error -> {
                        manageError(message, error, "showPetById");
                    });
            } catch (Exception e) {
                logUnexpectedError("showPetById", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        // Health Check
        vertx.eventBus().consumer("health.frontend").handler(message -> {
            message.reply("pong");
        });

    }

    // Enrich the JsonObject to be sent over Event Bus
    public static <T> JsonObject encodeToJsonObject(ApiResponse<T> response) {
        JsonObject json = new JsonObject();
        int status;
        if (response != null) {
            if (response.getStatusCode() == 404) {
                json.put("status", 404);
                json.put("body", new JsonObject());
                return json;
            }
            if (response.hasData()) {
                status = response.getStatusCode() == 0 ? 200 : response.getStatusCode();
                if (response.getData() instanceof List) {
                    json.put("body", new JsonArray(Json.encode(response.getData())));
                    json.put("isJsonArray", true);
                } else {
                    json.put("body", JsonObject.mapFrom(response.getData()));
                }
            } else {
                status = response.getStatusCode() == 0 ? 204 : response.getStatusCode();
                json.put("body", new JsonObject());
            }
        } else {
            status = 204;
            json.put("body", new JsonObject());
        }
        json.put("status", status);
        return json;
    }

    private void manageError(Message<JsonObject> message, Throwable cause, String serviceName) {
        int code = MainApiException.INTERNAL_SERVER_ERROR.getStatusCode();
        String statusMessage = MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage();
        if (cause instanceof MainApiException) {
            code = ((MainApiException)cause).getStatusCode();
            statusMessage = ((MainApiException)cause).getStatusMessage();
        } else {
            logUnexpectedError(serviceName, cause);
        }

        message.fail(code, statusMessage);
    }

    private void logUnexpectedError(String serviceName, Throwable cause) {
        logger.error("Unexpected error in "+ serviceName, cause);
    }

}
