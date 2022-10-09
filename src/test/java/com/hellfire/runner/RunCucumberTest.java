package com.hellfire.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * @author sitanshu pati
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/resources/features"},
        glue = {"com.microservice.test.accelerator.stepdefs"},
        tags = "@apitest",
        plugin = {"pretty", "html:target/cucumber/cucumber-report.html", "json:target/cucumber.json",
                "io.qameta.allure.cucumber6jvm.AllureCucumber6Jvm"
        })
public class RunCucumberTest {
}
