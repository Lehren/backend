package org.fsg1.fmms.backend.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.fsg1.fmms.backend.services.CurriculumService;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Mockito.*;

public class CurriculumEndpointTest extends JerseyTest {

    private static RequestSpecification spec;
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private CurriculumService service;

    @BeforeClass
    public static void initSpec() {
        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri("http://localhost:9998/")
                .addFilter(new ResponseLoggingFilter())//log request and response for better debugging. You can also only log if a requests fails.
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Override
    public ResourceConfig configure() {
        MockitoAnnotations.initMocks(this);
        return new ResourceConfig()
                .register(CurriculumEndpoint.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(service).to(CurriculumService.class);
                    }
                });
    }

    @Test
    public void testGetSESemesters() throws SQLException, IOException {
        ObjectNode result = mapper.createObjectNode();
        ArrayNode resultSemesterArray = mapper.createArrayNode();
        result.set("semesters", resultSemesterArray);

        final ObjectNode semesterResult = mapper.createObjectNode();
        semesterResult.put("semester", 1);
        final ArrayNode modules = mapper.createArrayNode();
        semesterResult.set("modules", modules);
        resultSemesterArray.add(semesterResult);

        ObjectNode module = mapper.createObjectNode();
        module.put("module_code", "JAV1");
        module.put("credits", 5);
        module.put("module_name", "Programming in Java");
        modules.add(module);

        when(service.execute(1))
                .thenReturn(result);
        given()
                .spec(spec)
                .get("curriculum/1/semesters")
                .then()
                .statusCode(200)
                .body("semesters", iterableWithSize(1))
                .body("semesters[0].semester", equalTo(1))
                .body("semesters.modules", iterableWithSize(1))
                .root("semesters[0].modules.find { it.module_code == 'JAV1' }")
                .body("credits", equalTo(5))
                .body("module_name", equalTo("Programming in Java"));
        verify(service, times(1)).execute(1);
        verify(service, times(1)).execute(anyInt());
        reset(service);
    }

    @Test
    public void testGetBISemesters() throws SQLException, IOException {
        ObjectNode result = mapper.createObjectNode();
        ArrayNode resultSemesterArray = mapper.createArrayNode();
        result.set("semesters", resultSemesterArray);

        final ObjectNode semesterResult = mapper.createObjectNode();
        semesterResult.put("semester", 1);
        final ArrayNode modules = mapper.createArrayNode();
        semesterResult.set("modules", modules);
        resultSemesterArray.add(semesterResult);

        ObjectNode module = mapper.createObjectNode();
        module.put("module_code", "BUA");
        module.put("credits", 4);
        module.put("module_name", "Business Administration");
        modules.add(module);

        when(service.execute(2))
                .thenReturn(result);
        given()
                .spec(spec)
                .get("curriculum/2/semesters")
                .then()
                .statusCode(200)
                .body("semesters", iterableWithSize(1))
                .body("semesters[0].semester", equalTo(1))
                .body("semesters.modules", iterableWithSize(1))
                .root("semesters[0].modules.find { it.module_code == 'BUA' }")
                .body("credits", equalTo(4))
                .body("module_name", equalTo("Business Administration"));
        verify(service, times(1)).execute(2);
        verify(service, times(1)).execute(anyInt());
        reset(service);
    }

    @Test
    public void testGetEmptySemester() throws SQLException, IOException {
        when(service.execute(anyInt()))
                .thenReturn((ObjectNode) mapper.createObjectNode().set("semesters", mapper.createArrayNode()));
        given()
                .spec(spec)
                .get("curriculum/1Fuio/semesters")
                .then()
                .statusCode(404);

        given()
                .spec(spec)
                .get("curriculum/5/semesters")
                .then()
                .statusCode(200);
        verify(service, times(1)).execute(5);
        verify(service, times(1)).execute(anyInt());
        reset(service);


    }

    @Test
    public void testExpectServerError() throws IOException, SQLException {
        when(service.execute(anyInt()))
                .thenReturn(null);
        given()
                .spec(spec)
                .get("curriculum/1/semesters")
                .then()
                .statusCode(500);
    }
}
