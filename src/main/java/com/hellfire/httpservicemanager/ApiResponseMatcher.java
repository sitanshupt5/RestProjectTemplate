package com.hellfire.httpservicemanager;

import io.restassured.response.Response;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author sitanshu pati
 */
public class ApiResponseMatcher {
    public static Matcher<Response> aHeader(final String header, final Matcher<?> matcher)
    {
        return new TypeSafeMatcher<Response>() {
            @Override
            protected boolean matchesSafely(Response response) {
                String actualHeaderValue = response.getHeader(header);
                return matcher.matches(actualHeaderValue);
            }

            @Override
            public void describeTo(Description description) {

            }
            @Override
            protected void describeMismatchSafely(Response item, Description mismatchDescription){

            }
        };
    }

    public static Matcher<Response> aStatusCode(final Matcher<Integer> statusCodeMatches) {
        return new TypeSafeMatcher<Response>() {
            @Override
            protected boolean matchesSafely(Response response) {
                return statusCodeMatches.matches(response.statusCode());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has statusCode").appendDescriptionOf(statusCodeMatches);
            }

            @Override
            protected void describeMismatchSafely(Response item, Description mismatchDescription) {
                mismatchDescription.appendText("statusCode<").appendValue(item.statusCode()+ "").appendText(">");
            }
        };
    }
}
