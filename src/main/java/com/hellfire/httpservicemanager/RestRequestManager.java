package com.hellfire.httpservicemanager;

import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sitanshu pati
 */
public class RestRequestManager {

    private HttpRequestManager httpRequestManager;
    public RestRequestManager(HttpRequestManager httpRequestManager) { this.httpRequestManager = httpRequestManager; }

    private String requestBody;
    private Map<String, String> header = new HashMap<>();
    private Map<String, String> param = new HashMap<>();
    private Map<String, String> formParam = new HashMap<>();
    private Map<String, String> queryParam = new HashMap<>();

    //This is used to reset the body and can be in after scenario hooks.
    public void initNewSpecification() {
        this.requestBody = null;
    }

    public RequestSpecification specification() {
        return httpRequestManager.value();
    }

    public void formParam(String parameterName, String parameterValue) {
        specification().formParam(parameterName, parameterValue);
    }

    public void multiPart(String contentBody, String controlName) {
        specification().multiPart(controlName, contentBody);
    }

    public void multiPart(byte[] contentBody, String controlName) {
        specification().multiPart(controlName, "file.txt", contentBody);
    }

    public void multiPart(String contentBody, String controlName, String mimiType) {
        specification().multiPart(controlName, contentBody, mimiType);
    }

    public void multiPart(byte[] contentBody, String controlName, String mimiType) {
        specification().multiPart(controlName, "file.txt", contentBody, mimiType);
    }

    public void body(String body) {specification().body(body);}

    public void body(byte[] body) {specification().body(body);}

    public void  contentType(String contentType) {
        RequestSpecification specification = specification();
        specification.contentType(contentType);
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody =requestBody;
    }

    public void clearRequestBody() {
        this.requestBody =null;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setRequestHeader(Map<String, String> header) {
        clearRequestHeader();
        this.header.putAll(header);
    }

    public void setRequestHeader(String key, String value) {
        this.header.put(key, value);
    }

    public void clearRequestHeader() {
        this.header.clear();
    }

    public void setRequestParam(String key, String value) {
        this.param.put(key, value);
    }

    public void setRequestParam(Map<String, String> params) {
        this.param.putAll(params);
    }

    public Map<String, String> getParam() {
        return this.param;
    }

    public void clearRequestParam() {
        this.formParam.clear();
    }

    public Map<String, String> getFormParam() {
        return formParam;
    }

    public void setRequestFormParam(String key, String value) {
        this.formParam.put(key, value);
    }

    public void setRequestFormParam(Map<String, String> params) {
        this.formParam.putAll(params);
    }

    public void clearRequestFormParam() {
        this.formParam.clear();
    }

    public Map<String, String> getQueryParam() {
        return this.queryParam;
    }

    public void setQueryParam(String key, String value) {
        this.queryParam.put(key, value);
    }

    public void setQueryParam(Map<String, String> params) {
        this.queryParam.putAll(params);
    }

    public void clearRequestQueryParam() {
        this.queryParam.clear();
    }




}
