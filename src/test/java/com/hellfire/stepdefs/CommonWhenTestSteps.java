package com.hellfire.stepdefs;

import com.hellfire.enums.ApiContext;
import com.hellfire.httpservicemanager.HttpResponseManager;
import com.hellfire.httpservicemanager.RestRequestManager;
import com.hellfire.util.ApiUtilManager;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author sitanshu pati
 */
public class CommonWhenTestSteps {

    HttpResponseManager httpResponseManager;
    TestManagerContext testManagerContext;
    RestRequestManager restRequestManager;

    public CommonWhenTestSteps(TestManagerContext context) {
        testManagerContext = context;
        httpResponseManager = testManagerContext.getHttpResponse();
        restRequestManager = testManagerContext.getRestRequest();
    }

    @When("The client performs {string} request on API {string}")
    public void perform_Http_Request(String httpMethod, String url) throws Exception {
        httpResponseManager.setResponsePrefix("");
        ApiUtilManager apiUtilManager = new ApiUtilManager();
        httpResponseManager.setResponse(httpResponseManager.doRequest(httpMethod, apiUtilManager.getBasePath(url)));
    }

    @When("I call method {string}")
    public void iCallMethod(String httpMethod) throws Exception {
        httpResponseManager.setResponsePrefix("");
        String basePath = (String) testManagerContext.getScenarioContext().getContext(ApiContext.BASE_PATH);
        httpResponseManager.setResponse(httpResponseManager.doRequest(httpMethod, basePath));
    }

    @When("I get the response")
    public void iGetTheResponse() {
        testManagerContext
                .getScenarioContext()
                .setContext(ApiContext.RESPONSE_BODY, httpResponseManager.getResponse().asString());
    }
}
