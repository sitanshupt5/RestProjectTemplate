package com.hellfire.stepdefs;

import static com.github.automatedowl.tools.AllureEnvironmentWriter.allureEnvironmentWriter;
import com.google.common.collect.ImmutableMap;
import com.hellfire.httpservicemanager.ConfigManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.cucumber.messages.internal.com.google.common.collect.ImmutableMap.builder;

/**
 * @author sitanshu pati
 */
public class Hooks {
    private static final Logger CURL_LOG = LoggerFactory.getLogger("curl");
    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private TestManagerContext testManagerContext;

    String logFolder = "apilogs";

    public Hooks(TestManagerContext context) {
        this.testManagerContext = context;
    }

    @Before()
    public void beforeScenario(Scenario scenario) throws FileNotFoundException {
        if (System.getProperty("AllureEnv")==null) {
            allureEnvironmentWriter(ImmutableMap.<String, String>builder()
                    .put("Environment", ConfigManager.getSystemPropertyOrSetDefault("env.type","uatenv"))
                    .put("User", ConfigManager.getSystemPropertyOrSetDefault("user.name", "Automation"))
                    .put("project", String .valueOf(Paths.get("").toAbsolutePath().getParent()))
                    .build());
            System.setProperty("AllureEnv", "TRUE");
        }
        logger.info("Report file path setup completed");
        CURL_LOG.info("##SCENARIO:{}", scenario.getName());
        PrintStream printStream = null;
        File fileWriter;
        String scenarioID = scenario.getId();
        String[] fileName = scenarioID.split("[.;:/]");
        String dir = logFolder+"/"+fileName[4]+"/"+fileName[5];
        File featureDirectory = new File(dir);
        if (!featureDirectory.exists()) featureDirectory.mkdirs();
        fileWriter = new File(dir+"/"+fileName[5]+fileName[7]+".log");
        if (!fileWriter.exists()) {
            try{
                fileWriter.createNewFile();
            }catch (IOException e) {
                logger.warn("file not created:" +fileWriter.getPath());
            }
        }
        logger.info("log file path setup completed");
        try{
            printStream = new PrintStream(new FileOutputStream(fileWriter), true);
            printStream.append("Scenario Name:"+scenario.getName() +System.lineSeparator());
            testManagerContext.getHttpRequest().restConfig.seDefaultStream(printStream);
            testManagerContext.getHttpRequest().initNewSpecification();
            logger.info("Initial request specification setup completed. More specs to be added further.");
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("file not found");
        }
    }

    @After()
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            String scenarioID = scenario.getId();
            String[] fileName = scenarioID.split("[.;:/]");
            File log = new File(logFolder
            +"/"
            +fileName[12]
            +"/"
            +fileName[13]
            +"/"
            +fileName[13]
            +fileName[15]
            +".log");
            byte[] byteData = new byte[0];
            try{
                byteData = Files.readAllBytes(log.toPath());
            }catch (IOException e) {
                e.printStackTrace();
            }
            scenario.attach(byteData, "text/plain", "");
        }
        testManagerContext.getSoftAssertions().assertAll();
    }
}
