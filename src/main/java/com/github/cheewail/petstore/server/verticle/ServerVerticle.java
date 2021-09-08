package com.github.cheewail.petstore.server.verticle;

import com.github.cheewail.petstore.server.service.PetService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ServerVerticle extends AbstractVerticle {
    static final Logger logger = LoggerFactory.getLogger(ServerVerticle.class);

    static final String CREATEPETS_SERVICE_ID = "createPets";
    static final String LISTPETS_SERVICE_ID = "listPets";
    static final String SHOWPETBYID_SERVICE_ID = "showPetById";

    @Autowired
    private Integer defaultPort;

    @Autowired
    private PetService petService;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start();
        logger.info("ServerVerticle starting...");

        createApiTempFile("petstore.yaml")
                .onSuccess(file -> {
                    RouterBuilder.create(vertx, file)
                            .onSuccess(routerBuilder -> {
                                routerBuilder.operation("createPets").handler(this::createPets);
                                routerBuilder.operation("listPets").handler(this::listPets);
                                routerBuilder.operation("showPetById").handler(this::showPetById);

                                Router router = routerBuilder.createRouter();
                                healthCheckBuilder(router);
                                vertx.createHttpServer()
                                        .requestHandler(router)
                                        .listen(config().getInteger("http.port", defaultPort), result -> {
                                            if (result.succeeded()) {
                                                startPromise.complete();
                                                logger.info("ServerVerticle listening on port {}", defaultPort);
                                            } else {
                                                startPromise.fail(result.cause());
                                                logger.info("ServerVerticle failed to start.");
                                            }
                                        });
                            })
                            .onFailure(err -> {
                                logger.info(err.toString());
                            });
                })
                .onFailure(e -> {

                });
    }

    // Health Check
    private void healthCheckBuilder(Router router) {
        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);

        healthCheckHandler.register("eventbus", promise ->
                vertx.eventBus().request("health.frontend", "ping")
                        .onSuccess(msg -> {
                            promise.complete(Status.OK());
                        })
                        .onFailure(err -> {
                            promise.complete(Status.KO());
                        }));

        vertx.eventBus().consumer("health.backend").handler(message -> {
            message.reply("pong");
        });

        healthCheckHandler.register("database", promise ->
                petService.showPetById(1L).subscribe(
                        result -> {
                            promise.complete(Status.OK());
                        },
                        error -> {
                            promise.complete(Status.KO());
                        }
                )
        );

        router.get("/health").handler(healthCheckHandler);
    }

    // Publisher for createPets
    private void createPets(RoutingContext routingContext) {
        RequestParameters requestParameters = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        vertx.eventBus()
                .<JsonObject>request(CREATEPETS_SERVICE_ID, null, result -> {
                    if (result.succeeded()) {
                        httpResponse(routingContext, result);
                    } else {
                        routingContext.response()
                                .setStatusCode(500)
                                .end();
                    }
                });
    }

    // Publisher for listPets
    private void listPets(RoutingContext routingContext) {
        RequestParameters requestParameters = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        vertx.eventBus()
                .<JsonObject>request(LISTPETS_SERVICE_ID, requestParameters.toJson().getJsonObject("query"), result -> {
                    if (result.succeeded()) {
                        httpResponse(routingContext, result);
                    } else {
                        routingContext.response()
                                .setStatusCode(500)
                                .end();
                    }
                });
    }

    // Publisher for showPetById
    private void showPetById(RoutingContext routingContext) {
        RequestParameters requestParameters = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        vertx.eventBus()
                .<JsonObject>request(SHOWPETBYID_SERVICE_ID, requestParameters.toJson().getJsonObject("path"), result -> {
                    if (result.succeeded()) {
                        httpResponse(routingContext, result);
                    } else {
                        routingContext.response()
                                .setStatusCode(500)
                                .end();
                    }
                });
    }

    // Helper method
    private void httpResponse(RoutingContext routingContext, AsyncResult<Message<JsonObject>> result) {
        int statusCode = result.result().body().getInteger("status");
        String httpBody = getHttpBody(result.result().body());
        switch(statusCode) {
            case 204:
            case 404:
                routingContext.response()
                        .setStatusCode(statusCode)
                        .end();
                break;
            // case 200, 201, etc
            default:
                if (httpBody != null) {
                    routingContext.response()
                            .putHeader("content-type", "application/json")
                            .setStatusCode(statusCode)
                            .end(httpBody);
                } else {
                    routingContext.response()
                            .setStatusCode(statusCode)
                            .end();
                }
        }
    }

    // Helper method
    private String getHttpBody(JsonObject json) {
        if (json.isEmpty()) return null;

        Boolean isJsonArray = json.getBoolean("isJsonArray");
        if (isJsonArray != null && isJsonArray.equals(true)) {
            return json.getJsonArray("body").isEmpty()? null : json.getJsonArray("body").encodePrettily();
        } else {
            return json.getJsonObject("body").isEmpty() ? null : json.getJsonObject("body").encodePrettily();
        }
    }

    // Helper method
    Future<String> createApiTempFile(String file) {
        Promise<String> promise = Promise.promise();
        vertx.fileSystem().createTempDirectory("")
                .onSuccess(dir -> {
                    File targetFile = new File(dir+"/"+file);
                    try {
                        FileUtils.copyInputStreamToFile(
                                this.getClass().getClassLoader().getResourceAsStream(file),
                                targetFile);
                        promise.complete(targetFile.getAbsolutePath());
                    } catch (IOException e) {
                        promise.fail(e);
                    }
                });
        return promise.future();
    }

}
