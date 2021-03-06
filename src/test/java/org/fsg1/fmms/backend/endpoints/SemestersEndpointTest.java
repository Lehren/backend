package org.fsg1.fmms.backend.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.fsg1.fmms.backend.exceptions.AppExceptionMapper;
import org.fsg1.fmms.backend.exceptions.EntityNotFoundException;
import org.fsg1.fmms.backend.services.SemestersService;
import org.fsg1.fmms.backend.services.Service;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SemestersEndpointTest extends JerseyTest {

    private static RequestSpecification spec;
    private ObjectMapper mapper = new ObjectMapper();
    @Mock
    private SemestersService service;

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
        return new ResourceConfig()
                .register(SemestersEndpoint.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(service).to(Service.class);
                    }
                })
                .register(AppExceptionMapper.class);
    }

    @Test
    public void testGetSemesters() throws Exception {
        JsonNode node = mapper.readTree(Files.readAllBytes(Paths.get("src/test/resources/json/semesterMultipleModules.json")));

        when(service.get(eq(service.getQueryCurriculumSemestersString()), eq("semesters"), eq(1)))
                .thenReturn(node);
        given()
                .spec(spec)
                .get("curriculum/1/semesters")
                .then()
                .statusCode(200)
                .header("Content-Type", MediaType.APPLICATION_JSON);
        verify(service, times(2)).get(eq(service.getQueryCurriculumSemestersString()), eq("semesters"), eq(1));
    }

    @Test
    public void testGetEmptySemester() throws Exception {
        when(service.get(eq(service.getQueryCurriculumSemestersString()), eq("semesters"), eq(5)))
                .thenThrow(new EntityNotFoundException());

        given()
                .spec(spec)
                .get("curriculum/5/semesters")
                .then()
                .statusCode(404);
        verify(service, times(2)).get(eq(service.getQueryCurriculumSemestersString()), eq("semesters"), eq(5));
    }

    @Test
    public void testExpectServerError() throws Exception {
        given()
                .spec(spec)
                .get("curriculum/1/semesters")
                .then()
                .statusCode(500);

        given()
                .spec(spec)
                .get("curriculum/1/semester/1")
                .then()
                .statusCode(500);
    }

    @Test
    public void testGetCompleteSemester() throws Exception {
        JsonNode node = mapper.readTree(Files.readAllBytes(Paths.get("src/test/resources/json/completeSemester.json")));

        when(service.get(eq(service.getQueryCompleteSemester()), eq("complete_semester"), eq(1), eq(1), eq(1), eq(1)))
                .thenReturn(node);
        given()
                .spec(spec)
                .get("curriculum/1/semester/1")
                .then()
                .statusCode(200)
                .header("Content-Type", MediaType.APPLICATION_JSON);
        verify(service, times(2)).get(eq(service.getQueryCompleteSemester()), eq("complete_semester"), eq(1), eq(1), eq(1), eq(1));
    }

    @Test
    public void testGetNoSemester() throws Exception {
        when(service.get(eq(service.getQueryCompleteSemester()), eq("complete_semester"), eq(1), eq(1), eq(1), eq(1)))
                .thenThrow(new EntityNotFoundException());

        given()
                .spec(spec)
                .get("curriculum/1/semester/1")
                .then()
                .statusCode(404);
        verify(service, times(2)).get(eq(service.getQueryCompleteSemester()), eq("complete_semester"), eq(1), eq(1), eq(1), eq(1));
    }
}
