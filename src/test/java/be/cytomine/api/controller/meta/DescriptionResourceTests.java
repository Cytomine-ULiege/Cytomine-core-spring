package be.cytomine.api.controller.meta;

/*
* Copyright (c) 2009-2022. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.domain.meta.Description;
import be.cytomine.repository.meta.DescriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
public class DescriptionResourceTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restDescriptionControllerMockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DescriptionRepository descriptionRepository;

    @Test
    @Transactional
    public void list_all_description() throws Exception {
        Description description = builder.given_a_description(builder.given_a_project());
        restDescriptionControllerMockMvc.perform(get("/api/description.json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.domainIdent=='" + description.getDomainIdent() + "')]").exists());
    }


    @Test
    @Transactional
    public void get_an_description() throws Exception {
        Description description = builder.given_a_description(builder.given_a_project());
        restDescriptionControllerMockMvc.perform(get("/api/domain/{domainClassName}/{domainIdent}/description.json", description.getDomainClassName(), description.getDomainIdent()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(description.getId().intValue()))
        ;
    }

    @Test
    @Transactional
    public void get_an_description_does_not_exists() throws Exception {
        Description description = builder.given_a_not_persisted_description(builder.given_a_project());
        restDescriptionControllerMockMvc.perform(get("/api/domain/{domainClassName}/{domainIdent}/description.json", description.getDomainClassName(), description.getDomainIdent()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void add_valid_description() throws Exception {
        Description description = builder.given_a_not_persisted_description(builder.given_a_project());
        restDescriptionControllerMockMvc.perform(post("/api/domain/{domainClassName}/{domainIdent}/description.json", description.getDomainClassName(), description.getDomainIdent())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(description.toJSON()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.descriptionID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddDescriptionCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.description.id").exists());

    }


    @Test
    @Transactional
    public void edit_valid_description() throws Exception {
        Description description = builder.given_a_not_persisted_description(builder.given_a_project());
        builder.persistAndReturn(description);
        restDescriptionControllerMockMvc.perform(put("/api/domain/{domainClassName}/{domainIdent}/description.json", description.getDomainClassName(), description.getDomainIdent())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(description.toJsonObject().withChange("data", "v2").toJsonString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.descriptionID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditDescriptionCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.description.id").exists())
                .andExpect(jsonPath("$.description.data").value("v2"));

    }
    
    @Test
    @Transactional
    public void delete_description() throws Exception {
        Description description = builder.given_a_not_persisted_description(builder.given_a_project());
        builder.persistAndReturn(description);
        restDescriptionControllerMockMvc.perform(delete("/api/domain/{domainClassName}/{domainIdent}/description.json", description.getDomainClassName(), description.getDomainIdent())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        
    }
}
