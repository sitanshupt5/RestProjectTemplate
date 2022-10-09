package com.hellfire.httpservicemanager;

import com.hellfire.constants.ConfigConstants;
import com.hellfire.httpoperations.HttpOperations;
import io.cucumber.messages.internal.com.google.common.base.Strings;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author sitanshu pati
 */
public class HttpResponseManager {

    private  static final Logger logger = LoggerFactory.getLogger(HttpResponseManager.class);

    private final Pattern replacePlaceHolderPattern = Pattern.compile("\\{\\{(.*)\\}\\}",Pattern.DOTALL);

    private HttpRequestManager httpRequestManager;
    private RestRequestManager restRequestManager;
    private ConfigManager world;
    private Response response;
    private boolean expectException;
    private RuntimeException exception;
    private String responsePrefix;

    public HttpResponseManager(ConfigManager configManager, HttpRequestManager httpRequestManager
            , RestRequestManager restRequestManager) {
        world = configManager;
        this.httpRequestManager = httpRequestManager;
        this.restRequestManager = restRequestManager;
    }

    public Response doRequest(String httpMethod, final String url) throws Exception {
        final HttpOperations method  = HttpOperations.parse(httpMethod);

        try{
            response = doRequest(url, method).call();
            logger.info("\n"+response.statusCode()+"\n"+response.getHeaders().toString());
            logger.info(response.asPrettyString());
        }catch (RuntimeException e) {
            if (!expectException){
                throw e;
            }
            exception = e;
        }

        httpRequestManager.initNewSpecification();
        restRequestManager.clearRequestFormParam();
        restRequestManager.clearRequestParam();
        return response;
    }

    private void authenticate() {
        Optional<String> type = Optional.ofNullable(world.getEnvProperty(ConfigConstants.AUTH_TYPE));
        if (!type.isPresent()) {
            return;
        }
        if (type.get().equalsIgnoreCase("BASIC")) {
            authBasic();
        }
    }

    private Callable<Response> doRequest(final String url, final HttpOperations method)
    {
        return () -> {
            final Matcher matcher = replacePlaceHolderPattern.matcher(url);
            authenticate();
            String base_uri = world.getEnvProperty(ConfigConstants.ENTITY_HOST_URI);
            Boolean proxy_disabled = world.getBoolean(ConfigConstants.ENTITY_PROXY_SET);

            if (!Strings.isNullOrEmpty(base_uri)) {
                httpRequestManager.specification.baseUri(base_uri);
            }

            if (!proxy_disabled) {
                String proxy = null;
                String entity_proxy = world.getEnvProperty(ConfigConstants.ENTITY_PROXY);
                Optional<String> default_proxy = Optional.ofNullable(world.getEnvProperty(ConfigConstants.PROXY));
                if (!Strings.isNullOrEmpty(entity_proxy)) {
                    proxy = entity_proxy;
                }else if (default_proxy.isPresent()) {
                    proxy = default_proxy.get();
                }

                URI uri;
                if (!Strings.isNullOrEmpty(proxy)) {
                    try{
                        uri = new URI(proxy);
                        httpRequestManager.specification.proxy(uri);
                    }catch (URISyntaxException e){

                    }
                }
            }

            String base_path = url;
            while(matcher.find()) {
                final String key = matcher.group(1);
                String replacement = world.getEnvProperty(key);
                if (!Strings.isNullOrEmpty(replacement)) {
                    base_path = url.replaceAll("\\{\\{"+key+"\\}\\}", replacement);
                }else {
                    logger.error("Placeholder value not found for key: {}", key);
                    logger.debug("Changing value of key: {} to Blank value", key);
                    base_path = url.replaceAll("\\{\\{"+key+"\\}\\}", "");
                }
            }

            final RequestSpecification requestSpec = httpRequestManager.value();
            requestSpec.log().all(true);
            logger.info("Request URL: "+this.world.getEnvProperty("host_uri")+base_path);
            beforeRequest(requestSpec);
            return method.doRequest(requestSpec, base_path);
        };
    }

    public Response getResponse() {return this.response;}

    public void setResponse(Response response) {
        this.response = response;
    }

    private void authBasic() {
        java.util.Optional<String> $username = java.util.Optional.of(world.getEnvProperty(ConfigConstants.USERNAME));
        java.util.Optional<String> $password = java.util.Optional.of(world.getEnvProperty(ConfigConstants.PASSWORD));
        if ($username.isPresent() && $password.isPresent()) {
            httpRequestManager.basicAuthentication($username.get(), $password.get());
        }
    }

    public void setExpectException(boolean expectException) {
        this.expectException = expectException;
    }

    public RuntimeException getException() {return exception;}

    public void setException(RuntimeException exception)
    {
        this.exception = exception;
    }

    public String getResponsePrefix() {
        return responsePrefix;
    }

    public void setResponsePrefix(String responsePrefix) {
        this.responsePrefix = responsePrefix;
    }

    public void beforeRequest(RequestSpecification requestSpecification) {
        String requestBody = restRequestManager.getRequestBody();
        Map<String, String> header = restRequestManager.getHeader();
        Map<String, String> param = restRequestManager.getParam();
        Map<String, String> formParams = restRequestManager.getFormParam();
        Map<String, String> queryParam = restRequestManager.getQueryParam();

        if (requestBody!= null)
        {
            requestSpecification.body(requestBody);
        }
        if (formParams!=null)
        {
            formParams.forEach((k,v) -> {
                requestSpecification.formParams(k,v);
                logger.info("Form params ->" +k+" : "+v);
            });
        }
        header.forEach((k,v) -> {
            requestSpecification.header(k,v);
            logger.info("Request Header ->" +k+" : "+v);
        });

        param.forEach((k,v) -> {
            requestSpecification.param(k,v);
            logger.info("Params ->" +k+" : "+v);
        });

        queryParam.forEach((k,v) -> {
            requestSpecification.queryParam(k,v);
            logger.info("Query Params ->" +k+" : "+v);
        });

        logger.info("Request Body: \n"+requestBody+ "\n");

    }

    public void afterRequest() {
        restRequestManager.clearRequestBody();
    }

    public long getResponseTime(){
        return response.getTime();
    }

    public String getJsonPathValue(String jsonPath) {
        JsonPath actualResponse = this.response.jsonPath();
        return actualResponse.getString(jsonPath);
    }
}
