package be.cytomine.api.controller.meta;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.meta.Property;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.repository.meta.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
public class PropertyResourceTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private MockMvc restPropertyControllerMockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PropertyRepository propertyRepository;

    
    @Test
    @Transactional
    public void list_all_property_for_project() throws Exception {
        Property property = builder.given_a_property(builder.given_a_project());
        restPropertyControllerMockMvc.perform(get("/api/project/{project}/property.json", property.getDomainIdent()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.domainIdent=='" + property.getDomainIdent() + "')]").exists());
    }

    @Test
    @Transactional
    public void list_all_property_for_project_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_a_project());
        restPropertyControllerMockMvc.perform(get("/api/project/{project}/property.json", 0L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void list_all_property_for_annotation() throws Exception {
        Property property = builder.given_a_property(builder.given_a_algo_annotation());
        restPropertyControllerMockMvc.perform(get("/api/annotation/{annotation}/property.json", property.getDomainIdent()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.domainIdent=='" + property.getDomainIdent() + "')]").exists());
    }

    @Test
    @Transactional
    public void list_all_property_for_annotation_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_a_algo_annotation());
        restPropertyControllerMockMvc.perform(get("/api/annotation/{annotation}/property.json", 0L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void list_all_property_for_image() throws Exception {
        Property property = builder.given_a_property(builder.given_an_image_instance());
        restPropertyControllerMockMvc.perform(get("/api/imageinstance/{image}/property.json", property.getDomainIdent()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.domainIdent=='" + property.getDomainIdent() + "')]").exists());
    }

    @Test
    @Transactional
    public void list_all_property_for_image_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_an_image_instance());
        restPropertyControllerMockMvc.perform(get("/api/imageinstance/{image}/property.json", 0L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void list_all_property_for_domain() throws Exception {
        Property property = builder.given_a_property(builder.given_an_image_instance());
        restPropertyControllerMockMvc.perform(get("/api/domain/{domainClassName}/{domainIdent}/property.json", 
                        property.getDomainClassName(), property.getDomainIdent()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.domainIdent=='" + property.getDomainIdent() + "')]").exists());
    }

    @Test
    @Transactional
    public void list_all_property_for_domain_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_an_image_instance());
        restPropertyControllerMockMvc.perform(get("/api/domain/{domainClassName}/{domainIdent}/property.json",
                        property.getDomainClassName(), 0))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void list_keys_annotation_with_project_filter() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        Property property = builder.given_a_property(userAnnotation);
        restPropertyControllerMockMvc.perform(get("/api/annotation/property/key.json")
                        .param("idProject", userAnnotation.getProject().getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[0]").value(property.getKey()));
    }

    @Test
    @Transactional
    public void list_keys_annotation_with_image_filter() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        Property property = builder.given_a_property(userAnnotation);
        restPropertyControllerMockMvc.perform(get("/api/annotation/property/key.json")
                        .param("idImage", userAnnotation.getImage().getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[0]").value(property.getKey()));
    }

    @Test
    @Transactional
    public void list_keys_annotation_with_image_filter_with_user() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        Property property = builder.given_a_property(userAnnotation);
        restPropertyControllerMockMvc.perform(get("/api/annotation/property/key.json")
                        .param("idImage", userAnnotation.getImage().getId().toString())
                        .param("user", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[?(@.key=='" + property.getKey() + "')]").exists())
                .andExpect(jsonPath("$.collection[?(@.key=='" + property.getKey() + "')].user").value(builder.given_superadmin().getId().intValue()));
    }


    @Test
    @Transactional
    public void list_keys_imageinstance_with_project_filter() throws Exception {
        ImageInstance imageInstance = builder.given_an_image_instance();
        Property property = builder.given_a_property(imageInstance);
        restPropertyControllerMockMvc.perform(get("/api/imageinstance/property/key.json")
                        .param("idProject", imageInstance.getProject().getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.collection[0]").value("key"));
    }


    @Test
    @Transactional
    public void list_annotation_position() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        Property property = builder.given_a_property(userAnnotation);
        restPropertyControllerMockMvc.perform(get("/api/user/{user}/imageinstance/{image}/annotationposition.json",
                        userAnnotation.getUser().getId(), userAnnotation.getImage().getId()
                )
                        .param("key", property.getKey()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection", hasSize(greaterThan(0))));
    }



    @Test
    @Transactional
    public void show_property_for_project() throws Exception {
        Property property = builder.given_a_property(builder.given_a_project());
        restPropertyControllerMockMvc.perform(get("/api/project/{project}/key/{key}/property.json", property.getDomainIdent(), property.getKey()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(property.getKey()));
    }

    @Test
    @Transactional
    public void show_property_for_project_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_a_project());
        restPropertyControllerMockMvc.perform(get("/api/project/{project}/key/{key}/property.json", 0L, "xxx"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    @Transactional
    public void show_property_for_project_key_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_a_project());
        restPropertyControllerMockMvc.perform(get("/api/project/{project}/key/{key}/property.json", property.getDomainIdent(), "xxx"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    @Transactional
    public void show_property_for_annotation() throws Exception {
        Property property = builder.given_a_property(builder.given_a_algo_annotation());
        restPropertyControllerMockMvc.perform(get("/api/annotation/{annotation}/key/{key}/property.json", property.getDomainIdent(), property.getKey()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(property.getKey()));
    }

    @Test
    @Transactional
    public void show_property_for_annotation_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_a_algo_annotation());
        restPropertyControllerMockMvc.perform(get("/api/annotation/{annotation}/key/{key}/property.json", 0L, "xxx"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    @Transactional
    public void show_property_for_annotation_key_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_a_algo_annotation());
        restPropertyControllerMockMvc.perform(get("/api/annotation/{annotation}/key/{key}/property.json", property.getDomainIdent(), "xxx"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void show_property_for_image() throws Exception {
        Property property = builder.given_a_property(builder.given_an_image_instance());
        restPropertyControllerMockMvc.perform(get("/api/imageinstance/{imageinstance}/key/{key}/property.json", property.getDomainIdent(), property.getKey()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(property.getKey()));
    }

    @Test
    @Transactional
    public void show_property_for_image_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_an_image_instance());
        restPropertyControllerMockMvc.perform(get("/api/imageinstance/{imageinstance}/key/{key}/property.json", 0L, "xxx"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void show_property_for_image_key_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_an_image_instance());
        restPropertyControllerMockMvc.perform(get("/api/imageinstance/{imageInstance}/key/{key}/property.json", property.getDomainIdent(), "xxx"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void show_property_for_domain() throws Exception {
        Property property = builder.given_a_property(builder.given_an_image_instance());
        restPropertyControllerMockMvc.perform(get("/api/domain/{domainClassName}/{domainIdent}/key/{key}/property.json",
                        property.getDomainClassName(), property.getDomainIdent(), property.getKey()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(property.getKey()));
    }

    @Test
    @Transactional
    public void show_property_for_domain_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_an_image_instance());
        restPropertyControllerMockMvc.perform(get("/api/domain/{domainClassName}/{domainIdent}/key/{key}/property.json",
                        property.getDomainClassName(), 0, "xxx"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void show_property_for_domain_key_not_exists() throws Exception {
        Property property = builder.given_a_property(builder.given_an_image_instance());
        restPropertyControllerMockMvc.perform(get("/api/domain/{domainClassName}/{domainIdent}/key/{key}/property.json",
                        property.getDomainClassName(), property.getDomainIdent(), "xxx"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    


    @Test
    @Transactional
    public void add_valid_property() throws Exception {
        Property property = builder.given_a_not_persisted_property(builder.given_a_project(), "key", "value");
        restPropertyControllerMockMvc.perform(post("/api/domain/{domainClassName}/{domainIdent}/property.json", property.getDomainClassName(), property.getDomainIdent())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(property.toJSON()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.propertyID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddPropertyCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.property.id").exists());
    }


    @Test
    @Transactional
    public void add_valid_property_other_path() throws Exception {
        Property property = builder.given_a_not_persisted_property(builder.given_a_project(), "key", "value");
        restPropertyControllerMockMvc.perform(post("/api/property.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(property.toJSON()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.propertyID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddPropertyCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.property.id").exists());
    }


    @Test
    @Transactional
    public void edit_valid_property() throws Exception {
        Property property = builder.given_a_not_persisted_property(builder.given_a_project(), "key", "value");
        builder.persistAndReturn(property);
        restPropertyControllerMockMvc.perform(put("/api/domain/{domainClassName}/{domainIdent}/property/{id}.json", property.getDomainClassName(), property.getDomainIdent(), property.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(property.toJsonObject().withChange("value", "v2").toJsonString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.propertyID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditPropertyCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.property.id").exists())
                .andExpect(jsonPath("$.property.value").value("v2"));

    }
    
    @Test
    @Transactional
    public void delete_property() throws Exception {
        Property property = builder.given_a_not_persisted_property(builder.given_a_project(), "key", "value");
        builder.persistAndReturn(property);
        restPropertyControllerMockMvc.perform(delete("/api/domain/{domainClassName}/{domainIdent}/property/{id}.json", property.getDomainClassName(), property.getDomainIdent(), property.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        
    }
}
