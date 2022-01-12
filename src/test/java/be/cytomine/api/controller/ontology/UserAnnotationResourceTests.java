package be.cytomine.api.controller.ontology;

import be.cytomine.BasicInstanceBuilder;
import be.cytomine.CytomineCoreApplication;
import be.cytomine.domain.image.AbstractImage;
import be.cytomine.domain.image.AbstractSlice;
import be.cytomine.domain.image.ImageInstance;
import be.cytomine.domain.image.SliceInstance;
import be.cytomine.domain.ontology.AnnotationTerm;
import be.cytomine.domain.ontology.SharedAnnotation;
import be.cytomine.domain.ontology.Term;
import be.cytomine.domain.ontology.UserAnnotation;
import be.cytomine.domain.project.Project;
import be.cytomine.domain.security.User;
import be.cytomine.exceptions.CytomineMethodNotYetImplementedException;
import be.cytomine.service.CommandService;
import be.cytomine.service.ontology.UserAnnotationService;
import be.cytomine.utils.JsonObject;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mortbay.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CytomineCoreApplication.class)
@AutoConfigureMockMvc
@WithMockUser(username = "superadmin")
public class UserAnnotationResourceTests {

    @Autowired
    private EntityManager em;

    @Autowired
    private BasicInstanceBuilder builder;

    @Autowired
    private CommandService commandService;

    @Autowired
    private MockMvc restUserAnnotationControllerMockMvc;

    @Autowired
    private UserAnnotationService userAnnotationService;

    private static WireMockServer wireMockServer = new WireMockServer(8888);


    @BeforeAll
    public static void beforeAll() {
        wireMockServer.start();
    }

    @AfterAll
    public static void afterAll() {
        try {
            wireMockServer.stop();
        } catch (Exception e) {}
    }

    @Test
    @Transactional
    public void get_a_user_annotation() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        builder.given_an_annotation_term(userAnnotation);
        em.refresh(userAnnotation);
        restUserAnnotationControllerMockMvc.perform(get("/api/userannotation/{id}.json", userAnnotation.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userAnnotation.getId().intValue()))
                .andExpect(jsonPath("$.class").value("be.cytomine.domain.ontology.UserAnnotation"))
                .andExpect(jsonPath("$.created").value(userAnnotation.getCreated().getTime()))
                .andExpect(jsonPath("$.location").value(userAnnotation.getWktLocation()))
                .andExpect(jsonPath("$.image").value(userAnnotation.getImage().getId().intValue()))
                .andExpect(jsonPath("$.project").value(userAnnotation.getProject().getId().intValue()))
                .andExpect(jsonPath("$.user").value(userAnnotation.getUser().getId()))
                .andExpect(jsonPath("$.centroid.x").exists())
                .andExpect(jsonPath("$.centroid.y").exists())
                .andExpect(jsonPath("$.term", hasSize(equalTo(1))))
                .andExpect(jsonPath("$.term[0]").value(userAnnotation.getTerms().get(0).getId().intValue()))
        ;

//
//        {
//            "class": "be.cytomine.ontology.UserAnnotation",
//                "id": 6897878,
//                "created": "1631093769980",
//                "updated": null,
//                "deleted": null,
//                "location": "POLYGON ((1244.7739820199815 1890.375, 1240.9930251315277 1851.986300483326, 1229.7954545190048 1815.0728571604468, 1211.6115865413822 1781.053232956964, 1187.1402170474087 1751.2347829525913, 1157.321767043036 1726.7634134586178, 1123.302142839553 1708.5795454809952, 1086.388699516674 1697.3819748684723, 1048 1693.6010179800185, 1009.6113004833259 1697.3819748684723, 972.6978571604469 1708.5795454809952, 938.6782329569638 1726.7634134586178, 908.8597829525912 1751.2347829525913, 884.388413458618 1781.053232956964, 866.2045454809951 1815.072857160447, 855.0069748684722 1851.986300483326, 851.2260179800185 1890.375, 855.0069748684722 1928.763699516674, 866.2045454809951 1965.677142839553, 884.388413458618 1999.696767043036, 908.8597829525913 2029.5152170474087, 938.6782329569639 2053.986586541382, 972.697857160447 2072.170454519005, 1009.611300483326 2083.3680251315277, 1048 2087.1489820199813, 1086.388699516674 2083.3680251315277, 1123.302142839553 2072.170454519005, 1157.3217670430363 2053.986586541382, 1187.1402170474087 2029.5152170474087, 1211.6115865413822 1999.696767043036, 1229.7954545190048 1965.677142839553, 1240.9930251315277 1928.763699516674, 1244.7739820199815 1890.375))",
//                "image": 6836067,
//                "geometryCompression": 0,
//                "project": 6399468,
//                "container": 6399468,
//                "user": 6399285,
//                "nbComments": 0,
//                "area": 30094.84913286161,
//                "perimeterUnit": "mm",
//                "areaUnit": "micron²",
//                "perimeter": 0.615956769903102,
//                "centroid": {
//            "x": 1047.9999999999998,
//                    "y": 1890.3750000000002
//        },
//            "term": [],
//            "similarity": null,
//                "rate": null,
//                "idTerm": null,
//                "idExpectedTerm": null,
//                "cropURL": "https://demo.cytomine.com/api/userannotation/6897878/crop.jpg",
//                "smallCropURL": "https://demo.cytomine.com/api/userannotation/6897878/crop.png?maxSize=256",
//                "url": "https://demo.cytomine.com/api/userannotation/6897878/crop.jpg",
//                "imageURL": "https://demo.cytomine.com/#/project/6399468/image/6836067/annotation/6897878",
//                "reviewed": false
//        }

    }

    @Test
    @Transactional
    public void get_a_user_annotation_not_exists() throws Exception {
        restUserAnnotationControllerMockMvc.perform(get("/api/userannotation/{id}.json", 0))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    @Transactional
    public void list_annotations_light() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        restUserAnnotationControllerMockMvc.perform(get("/api/userannotation.json", userAnnotation.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection[?(@.id=='"+userAnnotation.getId()+"')]").exists());
    }


    @Test
    @Transactional
    public void count_annotations_by_user() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        restUserAnnotationControllerMockMvc.perform(get("/api/user/{idUser}/userannotation/count.json", userAnnotation.getUser().getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThan(0)));

        User newUser = builder.given_a_user();
        restUserAnnotationControllerMockMvc.perform(get("/api/user/{idUser}/userannotation/count.json", newUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @Transactional
    public void count_annotations_by_project() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        restUserAnnotationControllerMockMvc.perform(get("/api/user/{idUser}/userannotation/count.json", userAnnotation.getUser().getId())
                        .param("project", userAnnotation.getProject().getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThan(0)));

        Project projectWithoutAnnotation = builder.given_a_project();
        restUserAnnotationControllerMockMvc.perform(get("/api/user/{idUser}/userannotation/count.json", userAnnotation.getUser().getId())
                        .param("project", projectWithoutAnnotation.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }


    @Test
    @Transactional
    public void count_annotations_by_project_with_dates() throws Exception {
        UserAnnotation oldUserAnnotation = builder.given_a_user_annotation();
        oldUserAnnotation.setCreated(DateUtils.addDays(new Date(), -1));

        UserAnnotation newUserAnnotation =
                builder.persistAndReturn(builder.given_a_not_persisted_user_annotation(oldUserAnnotation.getProject()));


        restUserAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId())
                        .param("startDate", String.valueOf(oldUserAnnotation.getCreated().getTime()))
                        .param("endDate", String.valueOf(newUserAnnotation.getCreated().getTime())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));

        restUserAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId())
                        .param("startDate", String.valueOf(DateUtils.addSeconds(oldUserAnnotation.getCreated(),-1).getTime())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));

        restUserAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId())
                        .param("endDate", String.valueOf(DateUtils.addSeconds(newUserAnnotation.getCreated(),1).getTime())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));

        restUserAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId())
                        .param("startDate", String.valueOf(DateUtils.addSeconds(newUserAnnotation.getCreated(),-1).getTime())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));

        restUserAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId())
                        .param("endDate", String.valueOf(DateUtils.addSeconds(oldUserAnnotation.getCreated(),1).getTime())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));

        restUserAnnotationControllerMockMvc.perform(get("/api/project/{idProject}/userannotation/count.json", oldUserAnnotation.getProject().getId())
                        .param("endDate", String.valueOf(DateUtils.addDays(oldUserAnnotation.getCreated(), -2).getTime())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }


    @Test
    @Transactional
    public void download_user_annotation_document() throws Exception {
        throw new CytomineMethodNotYetImplementedException("todo...");
    }

    @Test
    @Transactional
    public void add_valid_user_annotation() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_not_persisted_user_annotation();
        restUserAnnotationControllerMockMvc.perform(post("/api/userannotation.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userAnnotation.toJSON()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.userannotationID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddUserAnnotationCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.annotation.id").exists());

    }


    @Test
    @Transactional
    public void add_user_annotation_with_not_valid_location() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_not_persisted_user_annotation();
        JsonObject jsonObject = userAnnotation.toJsonObject();
        jsonObject.put("location",
                "POLYGON ((225.73582220103702 306.89723126347087, 225.73582220103702 307.93556995227914, 226.08028300710947 307.93556995227914, 226.08028300710947 306.89723126347087, 225.73582220103702 306.89723126347087))"
                ); // too small
        restUserAnnotationControllerMockMvc.perform(post("/api/userannotation.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toJsonString()))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    public void add_valid_user_annotation_without_project() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_not_persisted_user_annotation();
        JsonObject jsonObject = userAnnotation.toJsonObject();
        jsonObject.remove("project");
        restUserAnnotationControllerMockMvc.perform(post("/api/userannotation.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toJsonString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annotation.project").value(userAnnotation.getProject().getId()));
        // => project is retrieve from slice/image
    }

    @Test
    @Transactional
    public void add_valid_user_annotation_with_terms() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_not_persisted_user_annotation();
        Term term1 = builder.given_a_term(userAnnotation.getProject().getOntology());
        Term term2 = builder.given_a_term(userAnnotation.getProject().getOntology());
        JsonObject jsonObject = userAnnotation.toJsonObject();
        jsonObject.put("term", Arrays.asList(term1.getId(), term2.getId()));
        restUserAnnotationControllerMockMvc.perform(post("/api/userannotation.json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toJsonString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annotation.term", hasSize(2)));
    }


    @Test
    @Transactional
    public void edit_valid_user_annotation() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        restUserAnnotationControllerMockMvc.perform(put("/api/userannotation/{id}.json", userAnnotation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userAnnotation.toJSON()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.userannotationID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.EditUserAnnotationCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.annotation.id").exists());

    }


    @Test
    @Transactional
    public void delete_user_annotation() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        restUserAnnotationControllerMockMvc.perform(delete("/api/userannotation/{id}.json", userAnnotation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userAnnotation.toJSON()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.userannotationID").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.DeleteUserAnnotationCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.annotation.id").exists());

    }


    @Test
    @Transactional
    public void delete_user_annotation_not_exist_fails() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        restUserAnnotationControllerMockMvc.perform(delete("/api/userannotation/{id}.json", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userAnnotation.toJSON()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").exists());

    }


    @Test
    @javax.transaction.Transactional
    public void get_user_annotation_crop() throws Exception {
        UserAnnotation annotation = given_a_user_annotation_with_valid_image_server(builder);

        configureFor("localhost", 8888);

        byte[] mockResponse = UUID.randomUUID().toString().getBytes(); // we don't care about the response content, we just check that core build a valid ims url and return the content

        String url = "/slice/crop.png?fif=%2Fdata%2Fimages%2F"+builder.given_superadmin().getId() +"%2F1636379100999%2FCMU-2%2FCMU-2.mrxs&mimeType=openslide%2Fmrxs&topLeftX=1&topLeftY=50&width=49&height=49&location=POLYGON+%28%281+1%2C+50+10%2C+50+50%2C+10+50%2C+1+1%29%29&imageWidth=109240&imageHeight=220696&type=crop";
        stubFor(get(urlEqualTo(url))
                .willReturn(
                        aResponse().withBody(mockResponse)
                )
        );

        MvcResult mvcResult = restUserAnnotationControllerMockMvc.perform(get("/api/userannotation/{id}/crop.png", annotation.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<LoggedRequest> all = wireMockServer.findAll(RequestPatternBuilder.allRequests());
        AssertionsForClassTypes.assertThat(mvcResult.getResponse().getContentAsByteArray()).isEqualTo(mockResponse);
    }

    @Test
    @javax.transaction.Transactional
    public void get_user_annotation_crop_mask() throws Exception {
        UserAnnotation annotation = given_a_user_annotation_with_valid_image_server(builder);

        configureFor("localhost", 8888);

        byte[] mockResponse = UUID.randomUUID().toString().getBytes(); // we don't care about the response content, we just check that core build a valid ims url and return the content

        String url = "/slice/crop.png?fif=%2Fdata%2Fimages%2F"+builder.given_superadmin().getId() +"%2F1636379100999%2FCMU-2%2FCMU-2.mrxs&mimeType=openslide%2Fmrxs&topLeftX=1&topLeftY=50&width=49&height=49&location=POLYGON+%28%281+1%2C+50+10%2C+50+50%2C+10+50%2C+1+1%29%29&imageWidth=109240&imageHeight=220696&type=mask";
        stubFor(get(urlEqualTo(url))
                .willReturn(
                        aResponse().withBody(mockResponse)
                )
        );

        MvcResult mvcResult = restUserAnnotationControllerMockMvc.perform(get("/api/userannotation/{id}/mask.png", annotation.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<LoggedRequest> all = wireMockServer.findAll(RequestPatternBuilder.allRequests());
        AssertionsForClassTypes.assertThat(mvcResult.getResponse().getContentAsByteArray()).isEqualTo(mockResponse);
    }


    @Test
    @javax.transaction.Transactional
    public void get_user_annotation_alpha_mask() throws Exception {
        UserAnnotation annotation = given_a_user_annotation_with_valid_image_server(builder);

        configureFor("localhost", 8888);

        byte[] mockResponse = UUID.randomUUID().toString().getBytes(); // we don't care about the response content, we just check that core build a valid ims url and return the content

        String url = "/slice/crop.png?fif=%2Fdata%2Fimages%2F"+builder.given_superadmin().getId() +"%2F1636379100999%2FCMU-2%2FCMU-2.mrxs&mimeType=openslide%2Fmrxs&topLeftX=1&topLeftY=50&width=49&height=49&location=POLYGON+%28%281+1%2C+50+10%2C+50+50%2C+10+50%2C+1+1%29%29&imageWidth=109240&imageHeight=220696&type=alphaMask";
        stubFor(get(urlEqualTo(url))
                .willReturn(
                        aResponse().withBody(mockResponse)
                )
        );

        MvcResult mvcResult = restUserAnnotationControllerMockMvc.perform(get("/api/userannotation/{id}/alphamask.png", annotation.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<LoggedRequest> all = wireMockServer.findAll(RequestPatternBuilder.allRequests());
        AssertionsForClassTypes.assertThat(mvcResult.getResponse().getContentAsByteArray()).isEqualTo(mockResponse);
    }

    public static UserAnnotation given_a_user_annotation_with_valid_image_server(BasicInstanceBuilder builder) throws ParseException {
        AbstractImage image = builder.given_an_abstract_image();
        image.setWidth(109240);
        image.setHeight(220696);
        image.getUploadedFile().getImageServer().setBasePath("/data/images");
        image.getUploadedFile().getImageServer().setUrl("http://localhost:8888");
        image.getUploadedFile().setFilename("1636379100999/CMU-2/CMU-2.mrxs");
        image.getUploadedFile().setContentType("openslide/mrxs");
        ImageInstance imageInstance = builder.given_an_image_instance(image, builder.given_a_project());
        imageInstance.setInstanceFilename("CMU-2");
        AbstractSlice slice = builder.given_an_abstract_slice(image, 0, 0, 0);
        slice.setUploadedFile(image.getUploadedFile());
        SliceInstance sliceInstance = builder.given_a_slice_instance(imageInstance, slice);
        UserAnnotation userAnnotation
                = builder.given_a_user_annotation(
                        sliceInstance,
                "POLYGON((1 1,50 10,50 50,10 50,1 1))", builder.given_superadmin(), null);
        return userAnnotation;
    }


    @Test
    @Transactional
    public void create_comments_for_annotation() throws Exception {
        SharedAnnotation annotation = builder.given_a_shared_annotation();

        JsonObject jsonObject = annotation.toJsonObject();
        jsonObject.put("subject", "subject for test mail");
        jsonObject.put("message", "message for test mail");
        jsonObject.put("users", List.of(builder.given_superadmin().getId()));

        restUserAnnotationControllerMockMvc.perform(post("/api/userannotation/{id}/comment.json", annotation.getAnnotationIdent())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toJsonString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.printMessage").value(true))
                .andExpect(jsonPath("$.callback").exists())
                .andExpect(jsonPath("$.callback.method").value("be.cytomine.AddSharedAnnotationCommand"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.command").exists())
                .andExpect(jsonPath("$.sharedannotation.id").exists());
    }

    @Test
    @Transactional
    public void get_comment_for_annotation() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        SharedAnnotation comment = builder.given_a_shared_annotation(userAnnotation);

        restUserAnnotationControllerMockMvc.perform(get("/api/userannotation/{annotation}/comment/{id}.json", userAnnotation.getId(), comment.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()));
    }

    @Test
    @Transactional
    public void list_comment_for_annotation() throws Exception {
        UserAnnotation userAnnotation = builder.given_a_user_annotation();
        SharedAnnotation comment = builder.given_a_shared_annotation(userAnnotation);

        restUserAnnotationControllerMockMvc.perform(get("/api/userannotation/{annotation}/comment.json", userAnnotation.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(equalTo(1)));
    }

}
