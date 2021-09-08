package com.github.cheewail.petstore.server.rest;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ServerTest {

    @Autowired
    private Integer port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void listPets() {
        ResponseEntity<String> responseEntity = restTemplate
                .getForEntity("http://localhost:" + port + "/pets", String.class);

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals("application/json", responseEntity.getHeaders().get("content-type").get(0));
        assertEquals(true, responseEntity.getBody().contains("Alpha"));
        assertEquals(true, responseEntity.getBody().contains("Beta"));
        assertEquals(true, responseEntity.getBody().contains("Tag1"));
        assertEquals(true, responseEntity.getBody().contains("Tag2"));
    }

    @Test
    public void showPetById() throws JSONException {
        ResponseEntity<String> responseEntity = restTemplate
                .getForEntity("http://localhost:" + port + "/pets/1", String.class);

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals("application/json", responseEntity.getHeaders().get("content-type").get(0));
        assertEquals(true, responseEntity.getBody().contains("Alpha"));
        assertEquals(true, responseEntity.getBody().contains("Tag1"));
    }

    @Test
    public void showPetByIdNotFound() {
        ResponseEntity<String> responseEntity = restTemplate
                .getForEntity("http://localhost:" + port + "/pets/999", String.class);

        assertEquals(404, responseEntity.getStatusCodeValue());
        assertEquals(false, responseEntity.getHeaders().toString().contains("application/json"));
        assertEquals("0", responseEntity.getHeaders().get("content-length").get(0));
        assertEquals(null, responseEntity.getBody());
    }

    @Test
    public void createPets() {
        ResponseEntity<String> responseEntity = restTemplate
                .postForEntity("http://localhost:" + port + "/pets", null, String.class);

        assertEquals(201, responseEntity.getStatusCodeValue());
        assertEquals(false, responseEntity.getHeaders().toString().contains("application/json"));
        assertEquals("0", responseEntity.getHeaders().get("content-length").get(0));
        assertEquals(null, responseEntity.getBody());
    }

    @Test
    public void health() {
        ResponseEntity<String> responseEntity = restTemplate
                .getForEntity("http://localhost:" + port + "/health", String.class);

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals(true, responseEntity.getHeaders().toString().contains("application/json"));
    }
}


