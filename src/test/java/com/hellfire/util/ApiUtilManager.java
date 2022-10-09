package com.hellfire.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hellfire.constants.ConfigConstants;
import com.hellfire.constants.Entity;
import com.hellfire.constants.FilePaths;
import com.hellfire.enums.ApiContext;
import com.hellfire.stepdefs.TestManagerContext;
import com.hellfire.utils.DateUtils;
import com.hellfire.utils.YamlReaderUtils;
import io.restassured.mapper.ObjectMapperDeserializationContext;
import io.restassured.mapper.ObjectMapperSerializationContext;
import netscape.javascript.JSObject;
import org.assertj.core.util.Strings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContextManager;
import sun.applet.resources.MsgAppletViewer;

import java.io.*;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

/**
 * @author sitanshu pati
 */
public class ApiUtilManager {

    private static final Logger logger = LoggerFactory.getLogger(ApiUtilManager.class);
    public String request;

    public String env  = System.getProperty(ConfigConstants.ENV_TYPE).split("_")[0];

    public String getRequestBody(TestManagerContext testManagerContext, String customer) throws URISyntaxException, IOException, ParseException {
        Timestamp timestamp =  new Timestamp(System.currentTimeMillis());
        ObjectMapper mapper = new ObjectMapper();
        String api = (String) testManagerContext.getScenarioContext().getContext(ApiContext.API_NAME);
        ObjectNode defaults =getDefaults(api);
        String requestTemplate = defaults.get("request").toPrettyString();
        logger.info("Request Template: \n"+requestTemplate);
        JSONObject payload = new JSONObject(requestTemplate);
        if (!Strings.isNullOrEmpty(customer)) {
            ObjectReader updater = mapper.readerForUpdating(defaults.get(Entity.REQUEST));
            ObjectMapper jsonWriter = new ObjectMapper();
            JSONObject data = readDataFile(customer, api);
            logger.info("Data: \n" +data.toString());
            request = mapData(payload, data);
            logger.info("Final Request Body: \n"+request);
        }else {
            request = mapper.readTree(mapper.writeValueAsString(defaults.get(Entity.REQUEST))).toPrettyString();
        }

        return request
                .replaceAll("\\{RandomString}", getRandomAlphaString())
                .replaceAll(
                        "\\{ID}",
                        (String) testManagerContext.getScenarioContext().getContext(ApiContext.ID))
                .replaceAll("\\{RandomUUID}", getUniqueCorrelationId())
                .replaceAll("\\{currentdate}", DateUtils.getTodayDateInString());
    }



    private String mapData(JSONObject payload, JSONObject data) {
        String payloadTxt = payload.toString();
        for (String key: data.keySet())
        {
            if (payloadTxt.contains("("+key+")"))
            {
                String replacement = data.get(key).toString();
                payloadTxt = payloadTxt.replace("("+key+")",replacement);
            }
        }
        payloadTxt = payloadTxt.replaceAll("\"\\(.*?\\)\"", "null");
        return removeEmptyJsonElements(new JSONTokener(payloadTxt).nextValue());
    }

    private String removeEmptyJsonElements(Object object) {
        if (object instanceof JSONArray) {
            JSONArray array = (JSONArray) object;
            Iterator<Object> elements = array.iterator();
            int num = 0;
            while(elements.hasNext()) {
                if (array.get(num).toString().equals("{}"))
                   array.remove(num);
                else
                    removeEmptyJsonElements(elements.next());
                num++;
            }
        }else if (object instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) object;
            JSONArray names =jsonObject.names();
            Iterator<Object> els = names.iterator();
            while(els.hasNext())
            {
                String key = els.next().toString();
                if (jsonObject.isEmpty()||jsonObject.isNull(key))
                    jsonObject.remove(key);
                else
                    removeEmptyJsonElements(jsonObject.get(key));
            }
            JSONArray nmes = jsonObject.names();
            if (nmes!=null)
            {
                Iterator<Object> keys = nmes.iterator();
                while(keys.hasNext())
                {
                    String key = keys.next().toString();
                    if (jsonObject.get(key) instanceof JSONObject)
                    {
                        if (jsonObject.get(key).toString().equals("{}"))
                            jsonObject.remove(key);
                    }else {
                        removeEmptyJsonElements(jsonObject.get(key));
                    }
                }
            }
        }
        return object.toString();
    }

    public Object getData(String customer, String api) {
        String filePath = FilePaths.TEST_DATA_FILE_PATH
                .replaceAll(Entity.API_PATH, api.toLowerCase())
                .replaceAll(Entity.ENV_TYPE, env);
        YamlReaderUtils yamlReaderUtils = new YamlReaderUtils(ClassLoader.getSystemResourceAsStream(filePath));
        return yamlReaderUtils.getValue(customer);
    }

    public String getJsonData(String api) {
        String filePath =
                FilePaths.TEST_DATA_FILE_PATH
                .replaceAll(Entity.API_PATH, api.toLowerCase())
                .replaceAll(Entity.ENV_TYPE, env);
        String result = "";
        try {
            BufferedReader br  =  new BufferedReader(new FileReader(System.getProperty("user.dir")+filePath));
            StringBuffer sb = new StringBuffer();
            String line = br.readLine();
            while(line!=null) {
                sb.append(line);
                line = br.readLine();
            }
            result = sb.toString();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public Map<String, String> getParams(String api) throws IOException, URISyntaxException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode defaults = getDefaults(api);
        return mapper.convertValue(
                defaults.get(Entity.PARAMS), new TypeReference<Map<String, String>>() {});
    }

    public Map<String, Object> getSchema(String schemaKey, String api) {
        String filePath =
                FilePaths.SCHEMA_MAPPING
                .replaceAll(Entity.API_PATH, api.toLowerCase())
                .replaceAll(Entity.ENV_TYPE, env);
        YamlReaderUtils yamlReaderUtils = new YamlReaderUtils(ClassLoader.getSystemResourceAsStream(filePath));
        return yamlReaderUtils.getYamlObj(schemaKey);
    }

    public String getValue(String schemaKey, String api) {
        String filePath =
                FilePaths.SCHEMA_MAPPING
                        .replaceAll(Entity.API_PATH, api.toLowerCase())
                        .replaceAll(Entity.ENV_TYPE, env);
        YamlReaderUtils yamlReaderUtils = new YamlReaderUtils(ClassLoader.getSystemResourceAsStream(filePath));
        return (String)yamlReaderUtils.getValue(schemaKey);
    }

    public List<String> getListValue(String schemaKey, String api) {
        String filePath =
                FilePaths.SCHEMA_MAPPING
                        .replaceAll(Entity.API_PATH, api.toLowerCase())
                        .replaceAll(Entity.ENV_TYPE, env);
        YamlReaderUtils yamlReaderUtils = new YamlReaderUtils(ClassLoader.getSystemResourceAsStream(filePath));
        return (List<String>)yamlReaderUtils.getList(schemaKey);
    }

    public Map<String, String> getHeader(String api, String customerId) throws IOException, URISyntaxException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode defaults = getDefaults(api);
        return mapper.convertValue(
                defaults.get(Entity.HEADER), new TypeReference<Map<String, String>>() {});
    }

    public String getBasePath(String api) throws IOException, URISyntaxException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode defaults = getDefaults(api);
        return getJsonNodeValue(defaults, Entity.BASE_PATH);
    }

    public void setEntityHostURI(String api, TestManagerContext testManagerContext)
            throws IOException, URISyntaxException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode defaults = getDefaults(api);
        String entity_host_uri = testManagerContext.configManager.getEnvProperty(ConfigConstants.ENTITY_HOST_URI);

        if(!Strings.isNullOrEmpty(entity_host_uri)) {
            testManagerContext.configManager.put(ConfigConstants.ENTITY_HOST_URI, "");
        }

        entity_host_uri = getJsonNodeValue(defaults, Entity.HOST_URI);
        if (!Strings.isNullOrEmpty(entity_host_uri))
        {
            testManagerContext.configManager.put(ConfigConstants.ENTITY_HOST_URI, entity_host_uri);
        }

        logger.info("Entity Host URI set as: "+entity_host_uri);
    }

    public String getJsonNodeValue(JsonNode data, String nodeKey) {
        String value = null;
        JsonNode node = data;
        if (data.isArray()) {
            return null;
        } else if (data.size() ==1) {
            node = data.get(0);
        }else {
            node = data.get(0);
        }

        if (nodeKey.contains("/")) {
            value = node.at(nodeKey) == null? null: node.at(nodeKey).asText();
        }else {
            value = node.get(nodeKey) == null ? null :node.get(nodeKey).asText();
        }
        return value;
    }

    public String getJsonNodeValue(String jsonString, String nodeKey) throws JsonProcessingException {
        String value = null;
        ObjectMapper mapper  = new ObjectMapper();
        JsonNode data = mapper.readTree(jsonString);
        JsonNode node = data;
        if (data.isArray()) {
            return null;
        } else if (data.size() ==1) {
            node = data.get(0);
        }else {
            node = data.get(0);
        }

        if (nodeKey.contains("/")) {
            value = node.at(nodeKey) == null? null: node.at(nodeKey).asText();
        }else {
            value = node.get(nodeKey) == null ? null :node.get(nodeKey).asText();
        }
        return value;
    }

    public String getUniqueCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public String getRandomAlphaString() {
        int length = 10;
        String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"+"abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i =0; i<length; i++) {
            sb.append(candidateChars.charAt((random.nextInt(candidateChars.length()))));
        }
        return sb.toString();
    }

    public JSONObject readDataFile(String dataset, String api) throws IOException, URISyntaxException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader updater = mapper.readerForUpdating(getDefaults(api).get(Entity.REQUEST));
        ObjectMapper jsonWriter = new ObjectMapper();
        JSONObject data = new JSONObject(updater.readTree(jsonWriter.writeValueAsString(getData(dataset, api))));
        return data;
    }

    public ObjectNode getDefaults(String api) throws URISyntaxException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode defaults =
                mapper.
                        readValue(
                        new File(
                                getClass()
                                .getResource(
                                        FilePaths.API_PATH_REQUEST_JSON
                                        .replaceAll(Entity.API_PATH, api.toLowerCase())
                                        .replaceAll(Entity.ENV_TYPE, env))
                                .toURI()),
                        ObjectNode.class);
        return defaults;
    }
}
