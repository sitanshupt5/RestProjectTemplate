package com.hellfire.httpservicemanager;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import io.cucumber.messages.internal.com.google.common.base.Strings;
import io.restassured.path.json.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.restassured.module.jsv.JsonSchemaValidator;

import java.util.List;
import java.util.Map;

/**
 * @author sitanshu pati
 */
public class HttpServiceAssertion {
    private HttpResponseManager httpResponseManager;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HttpServiceAssertion(HttpResponseManager http) {
        httpResponseManager = http;
    }

    public void statusCodeIs(final int expectedStatusCode) {
        int actualStatusCode = httpResponseManager.getResponse().getStatusCode();
        String assertionReason = "Expected status code should match the actual status code.";
        assertThat(assertionReason, actualStatusCode, is(equalTo(expectedStatusCode)));
    }

    public void statusCodeIsNot(int statusCode) {
        int actualStatusCode = httpResponseManager.getResponse().getStatusCode();
        String assertionReason = "Expected status code should not match the actual status code.";
        assertThat(assertionReason, actualStatusCode, is(not(equalTo(statusCode))));
    }

    public void bodyContainsPropertyWithValue(String propertyPath, String expectedPropertyValue) {
        JsonPath actualResponse = httpResponseManager.getResponse().jsonPath();
        String propertyValueFromResponse = null;
        if (!Strings.isNullOrEmpty(actualResponse.getString(propertyPath))) {
            propertyValueFromResponse = actualResponse.getString(propertyPath).replaceAll("\\[","").replaceAll("\\]","");
        }
        String assertionReason = String.format("Response field '%s' value is not equal yo '%s' value.", propertyPath,expectedPropertyValue);
        assertThat(assertionReason, propertyValueFromResponse, is(equalTo(expectedPropertyValue)));
    }

    public void bodyDoesNotContainPath(String path) {
        JsonPath responsePath = httpResponseManager.getResponse().body().path(path);
        assertThat(responsePath, nullValue());
    }

    public void bodyContainsKey(String key) {
        String propertyVal = httpResponseManager.getResponse().jsonPath().getString(key);
        assertThat("Key not found", !(Strings.isNullOrEmpty(propertyVal)));
    }

    public void validateTheJsonResponseSchema(String schemaPath) {
        httpResponseManager.getResponse().then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
    }

    public String getJsonPathValue(String jsonPath) {
        JsonPath actualResponse = httpResponseManager.getResponse().jsonPath();
        return actualResponse.getString(jsonPath);
    }

    public List<Object> getListOfJsonPathValue(String jsonPath) {
        JsonPath actualResponse = httpResponseManager.getResponse().jsonPath();
        return actualResponse.getList(jsonPath);
    }

    public List<Map<String, Object>> getListOfJsonPathValue1(String jsonPath) {
        JsonPath actualResponse = httpResponseManager.getResponse().jsonPath();
        return actualResponse.getList(jsonPath);
    }

    public List<Map<String, String>> getResponseAsList() { return httpResponseManager.getResponse().as(List.class);}

    public List<Map<String, String>> getResponseAsList1() { return httpResponseManager.getResponse().as(List.class);}
}
