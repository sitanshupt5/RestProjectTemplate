package com.hellfire.stepdefs;

import com.hellfire.enums.ApiContext;
import com.hellfire.httpservicemanager.HttpRequestManager;
import com.hellfire.httpservicemanager.HttpServiceAssertion;
import com.hellfire.httpservicemanager.RestRequestManager;
import com.hellfire.util.ApiUtilManager;
import com.hellfire.utils.JsonUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import org.json.JSONObject;
import org.testng.util.Strings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author sitanshu pati
 */
public class CommonThenTestSteps {

    HttpServiceAssertion httpServiceAssertion;
    TestManagerContext testManagerContext;
    RestRequestManager restRequestManager;
    HttpRequestManager httpRequestManager;
    ApiUtilManager apiUtilManager;

    public CommonThenTestSteps(TestManagerContext context) {
        testManagerContext = context;
        this.httpServiceAssertion = new HttpServiceAssertion(testManagerContext.getHttpResponse());
        restRequestManager = testManagerContext.getRestRequest();
        httpRequestManager = testManagerContext.getHttpRequest();
        apiUtilManager = new ApiUtilManager();
    }

    @Then("I verify response code is {int}")
    public void iVerifyResponseCodeIs(int statusCode) {
        httpServiceAssertion.statusCodeIs(statusCode);
    }

    @Then("I verify the fields in response")
    public void iVerifyTheFieldsInResponse(DataTable table) {
        table
                .asMap(String.class, String.class)
                .entrySet()
                .stream()
                .skip(1)
                .forEach(
                        (entry) -> {
                            httpServiceAssertion.bodyContainsPropertyWithValue(
                                    (String) entry.getKey(),
                                    JsonUtil.getNodeValue(
                                            testManagerContext.getScenarioContext().getContext(ApiContext.REQUEST_BODY).toString(),
                                            (String) entry.getValue()));
                        });
    }

    @Then("I verify {string} in Response")
    public void iVerifyInResponse(String customer) {
        String request = (String) testManagerContext.getScenarioContext().getContext(ApiContext.REQUEST_BODY);
        Map<String, Object> map = apiUtilManager.getSchema(customer, (String)testManagerContext.getScenarioContext().getContext(ApiContext.API_NAME));
        map.forEach((key, value) -> {
            httpServiceAssertion.bodyContainsPropertyWithValue(key,
                    JsonUtil.getNodeValue(request, (String) value));
        });
    }

    @Then("I clear the request body")
    public void iClearTheRequestBody() {
        restRequestManager.clearRequestBody();
        httpRequestManager.body("");
    }

    @Then("I clear the request headers")
    public void iClearTheRequestHeaders() {
        restRequestManager.clearRequestHeader();
    }

    @Then("I clear the query parameters")
    public void iClearTheQueryParamters() {
        restRequestManager.clearRequestQueryParam();
    }

    @Then("I verify response code is {string}")
    public void iVerifyResponseCodeIs(String statusCode) {
        httpServiceAssertion.statusCodeIs(Integer.valueOf(statusCode));
    }

    @Then("I verify attributes in response using {string} for {string}")
    public void i_verify_attributes_in_response(String validationData, String api, DataTable dataTable) throws IOException, URISyntaxException {
        Map<String, String> attributes = dataTable.asMap(String.class, String.class);
        if (!Strings.isNullOrEmpty(validationData)) {
            JSONObject expectedResults = apiUtilManager.readDataFile(validationData,api);
            for (Map.Entry<String, String> attribute: attributes.entrySet())
            {
                if (!expectedResults.get(attribute.getKey()).equals(null)) {
                    String expectedResult = expectedResults.getString(attribute.getKey());
                    httpServiceAssertion.bodyContainsPropertyWithValue(attribute.getValue(),
                            expectedResult);
                }else{
                    httpServiceAssertion.bodyContainsPropertyWithValue(attribute.getValue(),
                            null);
                }
            }
        }
    }
}
