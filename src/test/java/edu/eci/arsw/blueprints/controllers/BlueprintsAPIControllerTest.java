package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BlueprintsAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllBlueprintsReturns200AndApiResponse() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("execute ok"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetBlueprintByAuthorAndNameReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints/john/house"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.author").value("john"))
                .andExpect(jsonPath("$.data.name").value("house"));
    }

    @Test
    void testGetBlueprintNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints/unknown/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testCreateBlueprintReturns201Created() throws Exception {
        String jsonPayload = """
                {
                    "author": "mario",
                    "name": "castle",
                    "points": [{"x": 10, "y": 20}]
                }
                """;

        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.author").value("mario"))
                .andExpect(jsonPath("$.data.name").value("castle"));
    }

    @Test
    void testAddPointReturns202Accepted() throws Exception {
        String pointPayload = """
                {
                    "x": 99,
                    "y": 88
                }
                """;

        mockMvc.perform(put("/api/v1/blueprints/john/house/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pointPayload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(202))
                .andExpect(jsonPath("$.data.points", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    void testCreateBlueprintInvalidDataReturns400BadRequest() throws Exception {
        String invalidPayload = """
                {
                    "author": "",
                    "name": ""
                }
                """;

        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
