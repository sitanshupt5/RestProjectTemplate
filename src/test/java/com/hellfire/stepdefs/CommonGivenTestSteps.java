package com.hellfire.stepdefs;

import com.hellfire.constants.Entity;
import com.hellfire.constants.Headers;
import com.hellfire.enums.ApiContext;
import com.hellfire.httpservicemanager.RestRequestManager;
import com.hellfire.util.ApiUtilManager;
import io.cucumber.java.en.Given;
import io.restassured.http.ContentType;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * @author sitanshu pati
 */
public class CommonGivenTestSteps {
    public RestRequestManager restRequestManager;
    TestManagerContext testManagerContext;

    private static final Logger logger = LoggerFactory.getLogger(CommonGivenTestSteps.class);

    public CommonGivenTestSteps(TestManagerContext context) {
        testManagerContext = context;
        restRequestManager = testManagerContext.getRestRequest();
    }

    @Given("I have API {string}")
    public void iHaveAPI(String apiName) throws IOException, URISyntaxException {
        testManagerContext.getScenarioContext().setContext(ApiContext.API_NAME, apiName);
        logger.info("Api Name set in scenario context:" +(String) testManagerContext.getScenarioContext().getContext(ApiContext.API_NAME));
        ApiUtilManager apiUtilManager = new ApiUtilManager();
        String basePath = apiUtilManager.getBasePath((String) testManagerContext.getScenarioContext().getContext(ApiContext.API_NAME));
        testManagerContext.getScenarioContext().setContext(ApiContext.BASE_PATH, basePath);
        logger.info("Base path set as "+basePath+" in scenario context");
        apiUtilManager.setEntityHostURI(apiName, testManagerContext);
        restRequestManager.clearRequestBody();
        logger.info("Existing request body has been cleared");
    }

    @Given("^I set the Content-Type as (.+)$")
    public void setContentType(String contentType) {
        restRequestManager.contentType(ContentType.valueOf(contentType).withCharset("utf-8"));
        logger.info("Content-Type has been set as: "+contentType);
    }

    @Given("I set request body for {string}")
    public void iSetRequestBodyAs(String customer) throws ParseException, IOException, URISyntaxException {
        ApiUtilManager apiUtilManager = new ApiUtilManager();
        iSetRequestHeader();
        restRequestManager.setRequestBody(apiUtilManager.getRequestBody(testManagerContext, customer));
        testManagerContext.getScenarioContext().setContext(ApiContext.REQUEST_BODY, restRequestManager.getRequestBody());
        logger.info("Request body is set in the scenario context");
    }

    @Given("I set request headers")
    private void iSetRequestHeader() throws IOException, URISyntaxException {
        ApiUtilManager apiUtilManager = new ApiUtilManager();
        restRequestManager.setRequestHeader(apiUtilManager.getHeader((String) testManagerContext.getScenarioContext().getContext(ApiContext.API_NAME) ,
                (String) testManagerContext.getScenarioContext().getContext(ApiContext.ID)));
        String access_token = (String) testManagerContext.getScenarioContext().getContext(ApiContext.ACCESS_TOKEN);
        String apiName = (String) testManagerContext.getScenarioContext().getContext(ApiContext.API_NAME);
        Boolean isAuthAPI = apiName.contains(Entity.AUTH_API);

        if (!Strings.isNullOrEmpty(access_token)& isAuthAPI & (restRequestManager.getHeader().containsKey("Authorization"))){
            restRequestManager.setRequestHeader("Authorization", "Bearer" +access_token);
        }
    }

    @Given("I set header {string} with a value of {string}")
    public void iProvideTheHeaderWithValueOf(String name, String value) {
        restRequestManager.setRequestHeader(name,value);
        testManagerContext.getScenarioContext().setContext(ApiContext.BRAND_HEADER, restRequestManager.getHeader().get(Headers.COMMON_HEADERS));
    }

    @Given("I set parameter {string} with a value of {string}")
    public void iSetParameterWithAValueOf(String name, String value) {
        restRequestManager.setRequestParam(name,value);
    }

    @Given("I set the new correlationId in header")
    public void iSetTheNewCorrelationIdHeader() {
        ApiUtilManager apiUtilManager = new ApiUtilManager();
        restRequestManager.setRequestHeader("x-txn-correlation-id", apiUtilManager.getUniqueCorrelationId());
    }


}
